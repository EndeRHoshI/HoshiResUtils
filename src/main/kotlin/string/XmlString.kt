package string

/**
 * Xml 文件中，每一个字符串键值对对应的实体类
 */
data class XmlString(
    val folderName: String, // 字符串所属的组名，也就是 Excel 的工作表名
    val fileName: String, // 字符串所在的文件名，也就是形如 string.xml、string_test.xml
    val valueFolderName: String, // value 文件夹名，即 res 的父目录，形如 values、value-zh-rCN 这种
    val name: String, // 字符串的 key
    val text: String, // 字符串的 value
    val translatable: Boolean = true, // translatable 属性读取到的值，不写就默认是 true
)