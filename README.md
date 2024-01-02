# HoshiResUtils
Android 资源管理工具
## 前言
开发中我们经常遇到一些需要批量处理资源的场景，例如统一整理 drawable 图片，导出导入 string.xml 进行多语言翻译，这里写一个 Kotlin 小工具类来进行处理

## drawable 图片资源管理工具
### 使用步骤和开发思路
1. 遍历 res 文件夹内的子文件夹，找出 drawable-xxhdpi 之类的文件夹（drawable 文件夹排除在外）
2. 以目标尺寸作为基准，这里选择用 drawable-xxhdpi，将其它 drawable 文件夹（如 drawable-xhdpi）中有重复的删除，不重复的保留
3. 这样最后所有的 drawable 文件夹内就会得到唯一的一份图片资源
4. 最后可以和 UI 协调，把所有低尺寸文件都出一份 drawable-xxhdpi 的，然后统一放到 drawable-xxhdpi 中
## string.xml 多语言字符串管理工具
### 使用步骤和开发思路
1. 输入项目路径和参数，控制是全量输出、已翻译完全的还是未翻译完全的
2. 直接遍历找到整个项目中符合正则匹配的 `*string*.xml`、`*array*.xml` 的文件
3. 以 string.xml 和 array.xml 作为基准进行判断已翻译还是未翻译，多出的忽略不理
4. 生成一个 excel，每个文件放一个工作表，每种语言一个列，这样从 xml 到 excel 就完成了
5. 输入 excel 路径和参数，控制是全量输出、已翻译的还是未翻译的
6. 读取 excel，逐个工作表去读取，逐行逐列读取，转换回 xml，然后就可以把 xml 放回到项目中