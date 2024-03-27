package string

import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * xml 增量更新工具类
 *
 * 用法：
 * 传入两个 Xml，一个原 Xml，一个目标 Xml，读取原 Xml 里面的所有键值对，然后读取目标 Xml 里面的所有键值对，遍历目标 Xml 的键值对
 * 把原 Xml 中有对应 Key 的数据写入到原 Xml 中，如果原 Xml 中没有对应的 Key，则新增键值对，简单说就是覆盖更新原有的 Xml，然后输出到指定文件中
 *
 * 使用场景：
 * 有时候有新的翻译文件，但是这个文件你不知道和旧版本对比新增或减少了那些，顺序也是错乱的，这时候就可以把新旧的两个 Xml 文件导入，然后输出到另一个 Xml 文件中
 * 再把输出的 Xml 文件中的内容复制，粘贴到之前旧的 Xml 文件中即可
 *
 * Created by lv.qx on 2024/3/26
 */
fun main() {

    val originXmlPath = "D:\\res\\origin_strings.xml" // 在这里输入原 Xml 的路径
    val targetXmlPath = "D:\\res\\target_strings.xml" // 在这里输入目标 Xml 的路径
    val outputXmlPath = "D:\\res\\output_strings.xml" // 在这里输入输出的 Xml 的路径

    val originXmlFile = File(originXmlPath)
    val targetXmlFile = File(targetXmlPath)
    val outputXmlFile = File(outputXmlPath)

    outputXmlFile.delete()
    outputXmlFile.createNewFile()

    val originXmlStringList = getXmlStringList(originXmlFile)
    val targetXmlStringList = getXmlStringList(targetXmlFile)

    val originXmlStringNameList = originXmlStringList.map { it.name }
    val targetXmlStringMap = targetXmlStringList.associateBy { it.name }

    val outputList = mutableListOf<XmlString>()

    // 第一次遍历，把旧 Xml 中的先加到列表里，同时把旧 Xml 中有，且新 Xml 中也有的，value 替换成 newValue 后，也放进列表里，
    originXmlStringList.forEach {
        val name = it.name
        if (targetXmlStringMap.keys.contains(name)) {
            val newValue = targetXmlStringMap[name]?.text
            if (newValue != null) {
                val newXmlString = XmlString("", "", "", it.name, newValue, true, it.check, it.trans)
                outputList.add(newXmlString)
            } else {
                outputList.add(it)
            }
        } else {
            outputList.add(it)
        }
    }

    // 第二次遍历，把新 Xml 中有，而旧 Xml 中没有的放进列表里
    targetXmlStringList.forEach {
        val name = it.name
        if (!originXmlStringNameList.contains(name)) {
            // println(it.name)
            outputList.add(it)
        }
    }

    // 输出 Xml
    // 构造 Document
    val doc = getXmlDocumentByFilePath()
    outputList.forEach {
        val element = doc.createElement("string") // 创建 string 标签
        element.setAttribute("name", "" + it.name) // 添加 name 属性
        val check = it.check
        if (check != -100) {
            element.setAttribute("check", "" + check) // 添加 check 属性
        }
        val trans = it.trans
        if (trans != -100) {
            element.setAttribute("trans", "" + trans) // 添加 trans 属性
        }
        element.textContent = it.getText(true) // 填入内容
        doc.documentElement.appendChild(element) // 将节点放到 root 节点下面
    }
    // 将 Document 中的内容写入文件中
    val tf = TransformerFactory.newInstance()
    val transformer = tf.newTransformer()
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    transformer.setOutputProperty(OutputKeys.INDENT, "yes") // 设置文档的换行与缩进
    val source = DOMSource(doc)

    FileOutputStream(outputXmlFile).use { fos ->
        PrintWriter(fos).use { pw ->
            val result = StreamResult(pw)
            transformer.transform(source, result)
        }
    }

    println("共识别出 ${originXmlStringList.size} 个原 Xml 键值对，${targetXmlStringList.size} 个目标 Xml 键值对，将要输出 ${outputList.size} 个 Xml 键值对")
}

private fun getXmlStringList(file: File): List<XmlString> {
    val resultList = mutableListOf<XmlString>()
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder() // 获取解析对象
    val document = builder.parse(file) // 对象解析文件
    val rootElement = document.documentElement // 获取root节点
    val nodeList = rootElement.getElementsByTagName("string") // 获取父节点下面所有 string 元素节点
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        val nodeAttributes = node.attributes
        val name = nodeAttributes.getNamedItem("name").nodeValue
        val check = nodeAttributes.getNamedItem("check")?.textContent?.toInt() ?: -100
        val trans = nodeAttributes.getNamedItem("trans")?.textContent?.toInt() ?: -100
        val xmlString = XmlString("", "", "", name, node.textContent, true, check, trans)
        resultList.add(xmlString)
    }
    return resultList
}


private fun getXmlDocumentByFilePath(): Document {
    // 获取 doc 对象
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.newDocument()

    // doc 中添加生成 root 节点
    val root = doc.createElement("resources")
    doc.appendChild(root);

    return doc;
}