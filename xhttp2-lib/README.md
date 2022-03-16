# 解决XHTTP2 自定义引入不方便的问题

xhttp2-lib资源链接：



## 手动引入模块并实现依赖的方式

1. **下载**

    下载 xhttp2-lib.zib

2. **解压**

    解压 xhttp2-lib 文件夹到与 app 目录同级

3. **引入模块（如果缺失这步，项目左侧目录树不会出现xhttp2-lib模块）**

    在 settings.gradle 中引入 xhttp2-lib 模块 到 工程 中，有两种写法：

    一、连写简洁

    ```groovy
    include ':app', ':xhttp2-lib'
    ```

    二、分写明了

    ```groovy
    include ':xhttp2demo'
    include ':xhttp2-lib'
    ```

    推荐第一种写法，比较简洁

    这时候先别 sync gradle，因为我们只是把 **模块** 引入到 **工程** 里了，还没有**实现**（implementation） 模块的依赖，做完下一步再 sync

4. **实现依赖（如果缺失这步，app项目将会找不到xhttp2-lib中的方法）**

    在 app 级别的 build.gradle 中的 dependencies 里进行模块依赖，然后 Sync gradle

    ```groovy
    // XHttp2
    implementation project(':xhttp2-lib')
    ```

ps：版本声明：我是在基于xuexiangjys的Xhttp2的2021-12-19的最新版本V2.0.4基础上进行修改的，基本上一些常见的bug已经修复，可以放心引入。

============ 这是一道华丽的分割线 ============

**起因**

写这篇文章的起因是有位同学来问我如何实现 **天气预报**这个功能，我原话说：“要想短时间内做出一个尽善尽美的天气预报软件，不太现实”。但是我可以给你演示一下天气预报的原理。

我预计是15min即可，最长半个小时。

事实是，单纯的引入一个 xhttp2-lib 花了半个多小时，其中各种问题，有网络不给力、有版本不兼容、依赖无法下载......当时我是非常尴尬的，脚不自觉的从拖鞋上挪了下来，因为宁可脚趾头抠坏地板砖，也不能抠坏我刚花了18块钱买的新拖鞋！当然，还是很感谢这位同学的宽容，能够耐心的看完我的这波xjb操作。

最后，xhttp2-lib 引入了，但是在我进行初始化 Xhttp2 的时候，一直提示找不到 Disposable。Cannot access io.reactivex.disposables.Disposable。由于我久疏战阵，一时间竟忘记这是什么原因导致的了。

![image-20211219183207355](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219182926331.png)

回看了一下 xuexiangjys 写的文档才发现，是没有实现一些依赖的引入

![image-20211219184531968](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219184531968.png)

看到这里，我陷入了沉思，为啥这里还是需要引入这么多的依赖？明明已经引入 xhttp2-lib 了呀，lib 里面自带这些依赖，我使用时还需要再次引入？这就好比我去买杯奶茶，老板仅仅给我递过来一杯奶茶，然后告诉我：“先生，不好意思，要想使用吸管，需要单独收费哦。”

我就是产生了这样的疑问，为啥不一起配套给我算到总账里呢？有必要让我再进行一次支付吗？？灵魂拷问啊，我就买杯奶茶，有必要二次支付吗？？？

这里就涉及到 gradle 一个基本知识点了，implementation 的作用是什么？

一般的回答是：implementation  用于引入依赖。

这个回答不完全正确

完善一些的回答应该是：implementation  用于引入依赖，且仅在当前模块生效，即其他模块无法分享使用这个依赖。

要想实现“**买杯奶茶自带吸管**”的这个功能，其实很简单，就是把 xhttp2-lib 里面的 两个相关 implementation 改为 api 即可。

![image-20211219192639527](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219192639527.png)

**implementation 与 api 的区别**

[官方文档原文](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation)

![image-20211219192946372](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219192946372.png)

翻译：第一段英文不用看，第二段英文大概意思就是两组单词配对：

api -> exported 

implementation -> internal

看看翻译就行了，我就不多解释了。

![](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219193540992.png)



![image-20211219193601742](F:\BiliBili\blog\image\解决XHTTP2 自定义引入不方便的问题\image-20211219193601742.png)

在将这两个依赖设置为 api 暴露出来之后，我又回头看了看其他两个依赖，gson 和 okhttp，同样是在 xhttp2-lib 里就有进行 implementation，然而 app 里面貌似是没有用到的，故此我将其删除。

这样一个更加方便手动引入的 xhttp2-lib 就完成了，我们使用时只需要操作开头那四步即可，其他的不用进行考虑。为首尾呼应，我将其粘贴在下方：

## 手动引入模块并实现依赖的方式

1. **下载**

    下载 xhttp2-lib.zib

2. **解压**

    解压 xhttp2-lib 文件夹到与 app 目录同级

3. **引入模块（如果缺失这步，项目左侧目录树不会出现xhttp2-lib模块）**

    在 settings.gradle 中引入 xhttp2-lib 模块 到 工程 中，有两种写法：

    一、连写简洁

    ```groovy
    include ':app', ':xhttp2-lib'
    ```

    二、分写明了

    ```groovy
    include ':xhttp2demo'
    include ':xhttp2-lib'
    ```

    推荐第一种写法，比较简洁

    这时候先别 sync gradle，因为我们只是把 **模块** 引入到 **工程** 里了，还没有**实现**（implementation） 模块的依赖，做完下一步再 sync

4. **实现依赖（如果缺失这步，app项目将会找不到xhttp2-lib中的方法）**

    在 app 级别的 build.gradle 中的 dependencies 里进行模块依赖，然后 Sync gradle

    ```groovy
    // XHttp2
    implementation project(':xhttp2-lib')
    ```

