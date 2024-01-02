package string

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val originPath = "D:\\WorkSpace\\work-string.main\\" // 在这里输入项目或者 res 文件夹的路径，注意文件夹的结尾都要加斜杠
    // val targetPath = "D:\\WorkSpace\\work-string.main\\Ddpai_app\\res\\" // 在这里输入项目或者 res 文件夹的路径，注意文件夹的结尾都要加斜杠
    val allStringFileList = mutableListOf<String>() // 在外部创建一个列表，遍历时把找到的 string.xml 文件路径放进去
    Utils.findAllStringFiles(originPath, allStringFileList)

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

        resPathList.forEach { xmlPath ->
            // 取得 value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
            val xmlFileParent = File(xmlPath).parent
            val valueFolderName = xmlFileParent.substring(xmlFileParent.lastIndexOf("\\") + 1, xmlFileParent.length)
            if (lanCellMap.keys.contains(valueFolderName)) {
                return@forEach // 如果已经有这一列了，跳过
            }
            lanCellMap[valueFolderName] = cellIndex // 存储语言和列的映射
            row.createCell(cellIndex++).setCellValue(valueFolderName)
        }
        resPathList.forEach { xmlPath ->
            // 取得 value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
            val xmlFileParent = File(xmlPath).parent
            val valueFolderName = xmlFileParent.substring(xmlFileParent.lastIndexOf("\\") + 1, xmlFileParent.length)
            val xmlStringList = Utils.readStringFromXml(xmlPath, folderName) // 从对应的 xml 文件路径中读出各个 string 键值对
            rowIndex = 1 // 行数重置回 1
            xmlStringList.forEach { xmlString ->
                row = sheet.getRow(rowIndex)
                if (row == null) {
                    row = sheet.createRow(rowIndex)
                }
                rowIndex++
                row.createCell(0).setCellValue(xmlString.fileName)
                row.createCell(1).setCellValue(xmlString.name)

                val textCellIndex = lanCellMap[valueFolderName]
                if (textCellIndex != null) {
                    row.createCell(textCellIndex).setCellValue(xmlString.text)
                }
            }
        }


    }

    val targetFileName: String? = null
    val targetFolderPath = "D:\\res\\" // 在这里输入你想要导出为 excel 的路径，注意文件夹的结尾都要加斜杠// 拼装文件名和路径
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