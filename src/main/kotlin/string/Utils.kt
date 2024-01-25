package string

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

object Utils {

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