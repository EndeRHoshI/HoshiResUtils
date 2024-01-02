package drawable

import java.io.File
import java.util.regex.Pattern

private var fileNumBeforeDelete = 0
private var fileNumAfterDelete = 0
private var deleteNum = 0

/**
 * Drawable 资源的管理
 *
 * 注意，系统会先到匹配的文件夹下去找 drawable，找不到，往更高的找，一直找到最高，也找不到，会再去找 drawable-nodpi，如果还是找不到，就会一直往下找
 * 详见文章：https://www.jianshu.com/p/0eb2824d6011
 */
fun main() {
    val resPath = "D:\\WorkSpace\\work-main\\Ddpai_app\\res\\" // 在这里输入项目或者 res 文件夹的路径，注意文件夹的结尾都要加斜杠
    val allDrawableMap = mutableMapOf<String, List<String>>() // 在外部创建一个列表，key -> 对应的文件夹，value -> 对应的图片资源文件路径列表
    findAllDrawableFiles(resPath, allDrawableMap)
    fileNumBeforeDelete = allDrawableMap.flatMap { it.value }.size

    val folderNameList = allDrawableMap.keys
    println("共找到 $fileNumBeforeDelete 个 drawable 文件，分别属于以下文件夹：$folderNameList")

    getDensityQualifierList().sortedByDescending { it.priority }.forEach { densityQualifier ->
        val baseDpi = densityQualifier.strValue // 选取基准尺寸
        val tempBaseDpiFolderNameList = folderNameList.filter { it.contains(baseDpi) }
        if (tempBaseDpiFolderNameList.isEmpty()) {
            println("可能被选为基准 dpi（$baseDpi）的文件夹列表为空，不作处理了")
            return@forEach
        }
        println("可能会被选为基准的文件夹为：$tempBaseDpiFolderNameList")
        val finalBaseDpiFolderNameList = mutableListOf<String>()
        tempBaseDpiFolderNameList.forEach { tempBaseDpiFolderName ->
            val filterName = tempBaseDpiFolderName.getNameWithoutDpiStr() // 删掉基准尺寸字符串，用来过滤非基准的文件夹
            val count = folderNameList.count { folderName -> folderName.getNameWithoutDpiStr() == filterName }
            if (count > 1) {
                finalBaseDpiFolderNameList.add(tempBaseDpiFolderName)
            }
        }
        println("最终被选为基准的文件夹为：$finalBaseDpiFolderNameList")

        if (finalBaseDpiFolderNameList.isEmpty()) {
            println("最终被选为基准 dpi（$baseDpi）文件夹列表为空，不作处理了")
            return@forEach
        }
        handleDrawableDelete(allDrawableMap, finalBaseDpiFolderNameList)
    }

    allDrawableMap.clear()
    findAllDrawableFiles(resPath, allDrawableMap)
    fileNumAfterDelete = allDrawableMap.flatMap { it.value }.size
    println(" ========================= 所有文件都处理完毕，处理前文件 $fileNumBeforeDelete 个，处理后文件 $fileNumAfterDelete 个，共删除文件 $deleteNum 个， ========================= ")
}

fun findAllDrawableFiles(resPath: String, allDrawableMap: MutableMap<String, List<String>>) {
    if (!resPath.endsWith("res\\")) {
        println("请输入一个 res 文件夹路径，这个功能是用来整理 res 文件夹下的 drawable 文件的")
        return
    }
    File(resPath).listFiles()?.forEach {
        // 文件夹名规则：形如 drawable-xxhdpi 的文件夹名
        val folderNamePattern = Pattern.compile("^drawable-\\S*dpi\$")
        val folderName = it.name
        val folderNameMatcher = folderNamePattern.matcher(folderName)
        if (it.isDirectory && folderNameMatcher.find()) {
            val fileList = (it.listFiles()?.toList() ?: emptyList()).map { file -> file.absolutePath }
            allDrawableMap[folderName] = fileList
        }
    }
}

/**
 * 处理 drawable 的删除
 * @param allDrawableMap 所有 drawable 的 map，key -> 基准文件夹名，value -> 基准文件夹下的资源文件路径列表
 */
fun handleDrawableDelete(allDrawableMap: MutableMap<String, List<String>>, baseDpiFolderList: List<String>) {
    if (baseDpiFolderList.isEmpty()) {
        println("基准 dpi 文件夹列表为空，不应该存在这种情况，请检查以下")
        return
    }
    val baseFolderFileNameMap = mutableMapOf<String, List<String>>() // key -> 基准文件夹名，value -> 基准文件夹下的文件名列表
    baseDpiFolderList.forEach { folderName ->
        val baseDpiFileNameList = allDrawableMap[folderName]?.map { file -> File(file).name }.orEmpty()
        baseFolderFileNameMap[folderName] = baseDpiFileNameList
    }

    // 反向又取出需要处理的目标基准 dpi 的字符串
    val firstBaseDpiFolder = baseDpiFolderList.first()
    val targetBaseDpi = firstBaseDpiFolder.replaceRange(0, firstBaseDpiFolder.lastIndexOf("-") + 1, "")
    println("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ baseDpi = $targetBaseDpi，开始处理 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓")
    val densityQualifierList = getDensityQualifierList()

    allDrawableMap.forEach {
        val currentFolderName = it.key // 取得当前的文件夹名
        val currentDpiStrValue = currentFolderName.substringAfterLast("-") // 取得当前的 dpi 字符串值
        if (baseDpiFolderList.contains(currentFolderName)) {
            // 如果文件夹名即 key 包含基准 api 字符串，说明是基准 api，无需处理
            println("当前处理的 $currentFolderName 文件夹是基准文件夹，无需处理")
        } else {
            val currentDpiPriority = densityQualifierList.firstOrNull { densityQualifier -> // 取得当前 dpi 的优先级
                densityQualifier.strValue == currentDpiStrValue
            }?.priority
            val targetDpiPriority = densityQualifierList.firstOrNull { densityQualifier -> // 取得目标 dpi 的优先级
                densityQualifier.strValue == targetBaseDpi
            }?.priority
            if (currentDpiPriority == null || targetDpiPriority == null) {
                println("当前遍历到的优先级 currentDpiPriority = $currentDpiPriority，targetDpiPriority = $targetDpiPriority，其中有 null，说明不在需要处理的像素密度列表中，不处理了")
                return@forEach
            }
            if (currentDpiPriority > targetDpiPriority) {
                println("当前遍历到的优先级 $currentDpiPriority 比目标基准优先级 $targetDpiPriority 高了，不用处理了")
                return@forEach
                // 举个例子，你要处理基准为 xhdpi 的情况，但是当前遍历到 xxhdpi，这时是不用处理的，因为基准为 xxhdpi 时已经处理过 xhdpi 了
            }

            // 如果文件夹名即 key 不包含基准 api 字符串，说明不是基准 api，要删除掉基准 api 文件夹中已有重名的文件
            println("↓ $currentFolderName 是非基准文件夹，开始处理 ↓")

            val targetFolderName = currentFolderName.replaceAfterLast("-", targetBaseDpi) // 取得目标文件名
            it.value.forEach { drawableFilePath ->
                // 遍历文件夹内所有的 drawable 文件路径
                val fileName = File(drawableFilePath).name
                val fileList = baseFolderFileNameMap[targetFolderName].orEmpty()
                if (fileList.contains(fileName)) {
                    val deleteFile = File(drawableFilePath)
                    if (deleteFile.exists()) {
                        println("发现重名：$fileName，文件存在，删除掉")
                        deleteNum++
                        File(drawableFilePath).delete()
                    } else {
                        println("发现重名：$fileName，但是文件不存在了，可能是处理更高优先级的密度时已经删除掉了，忽略")
                    }
                }
            }
            println("↑ $currentFolderName 文件夹处理完毕 ↑")
        }
    }
    println("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ baseDpi = $targetBaseDpi，处理完毕 ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑")
}

/**
 * 取得像素密度限定符列表，在这里写上你需要处理的像素密度，这里不需要删掉 nodpi 和 anydpi 的，也不需要处理 tv 的
 */
private fun getDensityQualifierList() = listOf(
    DensityQualifier.Ldpi,
    DensityQualifier.Mdpi,
    DensityQualifier.Hdpi,
    DensityQualifier.Xhdpi,
    DensityQualifier.Xxhdpi,
    DensityQualifier.Xxxhdpi,
)

/**
 * 像素密度限定符密封类，列举出所有可能的情况
 */
private sealed class DensityQualifier(
    val strValue: String, // 对应的字符串值
    val priority: Int // 优先级，越大优先级越高
) {
    object Xxhdpi : DensityQualifier("xxhdpi", 99) // 适用于超超高密度 (xxhdpi) 屏幕 (~ 480dpi) 的资源，
    // 现在市面上的设备，一般以这个作为基准就好，这个优先级最高

    object Anydpi : DensityQualifier("anydpi", 20) // 资源优先于任何 dpi 得到使用
    object Xxxhdpi : DensityQualifier("xxxhdpi", 10) // 适用于超超超高密度 (xxxhdpi) 屏幕 (~ 640dpi) 的资源
    object Xhdpi : DensityQualifier("xhdpi", 9) // 适用于加高 (xhdpi) 密度屏幕 (~ 320dpi) 的资源
    object Hdpi : DensityQualifier("hdpi", 8) // 适用于高密度 (hdpi) 屏幕 (~ 240dpi) 的资源
    object Mdpi : DensityQualifier("mdpi", 7) // 适用于中密度 (mdpi) 屏幕 (~ 160dpi) 的资源（这是基准密度）
    object Ldpi : DensityQualifier("ldpi", 6) // 适用于低密度 (ldpi) 屏幕 (~ 120dpi) 的资源
    object Nodpi : DensityQualifier("nodpi", 5) // 适用于所有密度的资源。这些是与密度无关的资源。无论当前屏幕的密度是多少，系统都不会缩放以此限定符标记的资源

    object Tvdpi : DensityQualifier("tvdpi", 4) // 适用于密度介于 mdpi 和 hdpi 之间的屏幕（约 213dpi）的资源。
    // 这不属于“主要”密度组。它主要用于电视，而大多数应用都不需要它。
    // 对于大多数应用而言，提供 mdpi 和 hdpi 资源便已足够，系统将视情况对其进行缩放。
    // 如果您发现有必要提供 tvdpi 资源，应按一个系数来确定其大小，即 1.33*mdpi。
    // 例如，如果某张图片在 mdpi 屏幕上的大小为 100px x 100px，那么它在 tvdpi 屏幕上的大小应该为 133px x 133px
}

/**
 * 取得去掉 dpi 相关字符串的名字
 */
fun String.getNameWithoutDpiStr(): String {
    return this.replaceAfterLast("-", "")
}