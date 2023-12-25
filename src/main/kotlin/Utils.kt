import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

object Utils {

    /**
     * 找到所有 string.xml 文件
     */
    fun findAllStringFiles(projectPath: String, allStringFileList: MutableList<String>): MutableList<String> {
        File(projectPath).listFiles()?.forEach {
            if (it.isDirectory) {
                // 如果是一个文件夹，再往下找
                findAllStringFiles(it.absolutePath, allStringFileList)
            } else {
                // 文件名规则：带有 string.xml 的文件名，string 前或后可以有其它字符，但是一定要 .xml 结尾
                val fileNamePattern = Pattern.compile("^\\S*string\\S*.xml\$")
                val fileNameMatcher = fileNamePattern.matcher(it.name)
                // 文件路径规则：在 values 目录下，包括 values-xxx 目录
                if (fileNameMatcher.find() && it.absolutePath.contains("\\values")) {
                    // 文件名和文件路径匹配成功，添加到列表中
                    // println(it.absolutePath)
                    allStringFileList.add(it.absolutePath)
                }
            }
        }
        return allStringFileList
    }

    fun readStringFromXml(xmlFilePath: String?, folderName: String): List<XmlString> {
        if (xmlFilePath.isNullOrEmpty()) {
            println("目标 Xml 路径为空，请检查")
            return listOf()
        }
        val xmlFile = File(xmlFilePath)
        return readStringFromXml(xmlFile, folderName)
    }

    fun readStringFromXml(xmlFile: File?, folderName: String): List<XmlString> {
        val resultList = mutableListOf<XmlString>()
        if (xmlFile == null || !xmlFile.exists()) {
            println("目标 Xml 文件为空或不存在，请检查")
            return resultList.toList()
        }
        val fileName = xmlFile.name
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder() // 获取解析对象
        val document = builder.parse(xmlFile) // 对象解析文件
        val rootElement = document.documentElement // 获取root节点
        val nodeList = rootElement.getElementsByTagName("string") // 获取父节点下面所有 string 元素节点
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            val nodeAttributes = node.attributes
            val name = nodeAttributes.getNamedItem("name").nodeValue
            val translatable =
                nodeAttributes.getNamedItem("translatable")?.textContent?.toBoolean() ?: true // 如果取不到，直接赋值为 true
            val xmlString = XmlString(folderName, fileName, name, node.textContent, translatable)
            resultList.add(xmlString)
        }
        return resultList.toList()
    }

    fun writeExcel(xmlStringList: List<XmlString>, targetFolderPath: String, targetFileName: String? = null) {
        // 拼装文件名和路径
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val excelName = (targetFileName ?: sdf.format(Date())) + ".xlsx"
        val excelPath = targetFolderPath + excelName
        val excelFile = File(excelPath)
        if (excelFile.exists()) {
            excelFile.delete()
        } else {
            if (!excelFile.parentFile.exists()) {
                excelFile.parentFile.mkdirs()
            }
            File(excelPath).createNewFile()
        }

        var rowIndex = 0 // 行
        var columnIndex = 0 // 列
    }

}