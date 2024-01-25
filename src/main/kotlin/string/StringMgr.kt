package string

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

fun main() {
    val originPath = "D:\\WorkSpace\\work-main\\" // 在这里输入整个项目或者单独某个 res 文件夹的路径，注意文件夹的结尾都要加斜杠
    // val targetPath = "D:\\WorkSpace\\work-main\\Ddpai_app\\res\\" // 在这里输入整个项目或者单独某个 res 文件夹的路径，注意文件夹的结尾都要加斜杠
    val allStringFileList = mutableListOf<String>() // 在外部创建一个列表，遍历时把找到的 string.xml 文件路径放进去
    findAllStringFiles(originPath, allStringFileList)

    println("共找到 " + allStringFileList.size + " 个 string.xml 文件")

    // 去掉前方统一的父目录，然后截掉 res 后一致的部分，得到不一致的部分来进行一下分组
    // 组数对应生成的 Excel 文件的工作表数，组名对应 Excel 工作表名
    val resPathMap = allStringFileList.groupBy { it.substringAfter(originPath).substringBefore("\\res") }
    println("共有 " + resPathMap.size + " 组")

    val excelBook = XSSFWorkbook()
    resPathMap.forEach {
        val folderName = it.key // 取得 folderName，就是组名或者说工作表名
        val resPathList = it.value
        // println(folderName)

        val sheet = excelBook.createSheet(folderName.replace("\\", "_")) // 创建工作表
        var rowIndex = 0 // 行
        var cellIndex = 0 // 列
        val lanCellMap = mutableMapOf<String, Int>() // 语言和列下标的映射

        var row = sheet.createRow(rowIndex++)
        lanCellMap["fileName"] = cellIndex // 存储 fileName 列和列下标的映射
        row.createCell(cellIndex++).setCellValue("fileName")

        lanCellMap["name"] = cellIndex // 存储 name 列和列下标的映射
        row.createCell(cellIndex++).setCellValue("name")

        val allXmlStringList = resPathList.flatMap { xmlPath -> readStringFromXml(xmlPath, folderName) }
        val valueFolderMap = allXmlStringList.groupBy { xmlString -> xmlString.valueFolderName }

        valueFolderMap.forEach { mapEntry ->
            val valueFolderName = mapEntry.key
            if (lanCellMap.keys.contains(valueFolderName)) {
                return@forEach // 如果已经有这一列了，跳过
            }
            lanCellMap[valueFolderName] = cellIndex // 存储语言和列的映射
            row.createCell(cellIndex++).setCellValue(valueFolderName)
        }

        val baseCellName = "values"
        if (valueFolderMap.containsKey(baseCellName)) {
            // 如果有基准列才继续处理，否则是不合法的，不用管了
            val baseList = valueFolderMap[baseCellName] // 取得基准列的各个项，后面用来给其它列找下标
            if (baseList != null) {
                valueFolderMap.forEach { mapEntry ->
                    val valueFolderName = mapEntry.key
                    val xmlStringList = mapEntry.value
                    rowIndex = 1 // 将行数重置回 1
                    xmlStringList.forEach { xmlString ->
                        // 如果是基准列，需要从头到尾进行填入内容
                        if (valueFolderName != baseCellName) {
                            // 如果不是基准列，需要找到对应的行来填入内容
                            rowIndex = baseList.indexOfFirst { baseXmlString -> xmlString.name == baseXmlString.name } + 1
                        }
                        if (rowIndex < 0) {
                            return@forEach
                        }
                        row = sheet.getRow(rowIndex)
                        if (row == null) {
                            row = sheet.createRow(rowIndex)
                        }
                        if (valueFolderName == baseCellName) {
                            rowIndex++
                        }
                        row.createCell(0).setCellValue(xmlString.fileName)
                        row.createCell(1).setCellValue(xmlString.name)

                        val textCellIndex = lanCellMap[valueFolderName]
                        if (textCellIndex != null) {
                            row.createCell(textCellIndex).setCellValue(xmlString.text)
                        }
                    }
                }
            }
        }
    }

    val targetFileName: String? = null // 在这里输入你想要导出的 Excel 的名称，不输入则默认用当前时间格式化作为名称
    val targetFolderPath = "D:\\res\\" // 在这里输入你想要导出为 Excel 的路径，注意文件夹的结尾都要加斜杠// 拼装文件名和路径
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
    FileOutputStream(excelFile).use { excelBook.write(it) }


    val isFullRead = false // 是否全部读取，true 全部读取，false 只读取未翻译的部分

}

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

    // 取得 value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
    val xmlFileParent = xmlFile.parent
    val valueFolderName = xmlFileParent.substring(xmlFileParent.lastIndexOf("\\") + 1, xmlFileParent.length)

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
        val xmlString = XmlString(folderName, fileName, valueFolderName, name, node.textContent, translatable)
        resultList.add(xmlString)
    }
    return resultList.toList()
}