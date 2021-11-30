# GradleDependenciesAndPluginsHelper

Gradle plugins/dependencies 坐标自动补全IntelliJ IDEA插件 [插件地址](https://plugins.jetbrains.com/plugin/10033-gradle-dependencies-and-plugins-helper)


### 功能

* 使用IntelliJ IDEA Smart Type Completion功能自动补全Gradle脚本中dependencies及plugins的依赖库坐标

![](doc/images/plugins.gif)

![](doc/images/dependencies.gif)

* 支持Gradle groovy脚本（*.build）及kotlin脚本（*.build.kts）

![](doc/images/plugins.kts.gif)

![](doc/images/dependencies.kts.gif)


* 默认使用[mavenCentral](https://search.maven.org)搜索dependencies，使用[Gradle Plugins](https://plugins.gradle.org)搜索plugins

* 支持通配符*

![](doc/images/wildcard.gif)

* 支持通过类名搜索依赖库坐标
    
    在dependencies区域使用 "c:"(classname) 或 "fc:"( fully-qualified classname )搜索依赖库坐标
    
    例子：
    
        compile("fc:org.junit.Test")
    
        compile("c:Junit")

![](doc/images/classname-query.gif)

* 可选添加特定maven仓库到repositories.Use `Show Intention Actions` action (`Alt + Enter` or ⌥⏎) and choose `Add specified repository to repositories.`

![](doc/images/add-repo.gif)

### 设置

* 使用前确定Smart Type Completion功能开启

![](doc/images/smart-type-completion.png)

* 可选使用 `Use Nexus2 Repository Search`(Nexus2),`Use Artifactory Repository Search` 搜索

![](doc/images/settings.png)

## License

Apache License Version 2.0

## IDE Support

Thanks to JetBrains for offering IDE support to develop this Open Source project.

[![JetBrains](doc/images/jetbrains.png)](https://jb.gg/OpenSource)
