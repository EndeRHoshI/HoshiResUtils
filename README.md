# HoshiStringUtils
Android string.xml 多语言字符串管理工具
## 使用步骤和开发思路
1. 输入项目路径和参数，控制是全量输出、已翻译完全的还是未翻译完全的
2. 直接遍历找到整个项目中符合正则匹配的 `*string*.xml`、`*array*.xml` 的文件
3. 以 string.xml 和 array.xml 作为基准进行判断已翻译还是未翻译，多出的忽略不理
4. 生成一个 excel，每个文件放一个工作表，每种语言一个列，这样从 xml 到 excel 就完成了
5. 输入 excel 路径和参数，控制是全量输出、已翻译的还是未翻译的
6. 读取 excel，逐个工作表去读取，逐行逐列读取，转换回 xml，然后就可以把 xml 放回到项目中