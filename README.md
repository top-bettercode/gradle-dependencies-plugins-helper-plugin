#### GradleDependenciesAndPluginsHelper

[中文说明](http://bestwu.cn/2017/09/01/gradle-dependencies-plugins-helper-plugin/)

This is an IntelliJ IDEA plugin for searching dependencies/plugins from [JCentral](https://bintray.com/search)/[GradlePlugins](https://plugins.gradle.org/) inside Gradle projects.

Inspired by [https://github.com/siosio/GradleDependenciesHelperPlugin](https://github.com/siosio/GradleDependenciesHelperPlugin).

#### Features

* Use `Smart Type Completion` in dependencies/plugins script block.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/plugins.gif)

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/dependencies.gif)

* Support *.gradle,*.gradle.kts.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/plugins.kts.gif)

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/dependencies.kts.gif)

* Use [jcenter API](https://bintray.com/docs/api/) for Gradle dependencies queries, Use [Gradle Plugins](https://plugins.gradle.org) for Gradle plugins queries.
* Support wildcard query * etc.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/wildcard.gif)

* Support search by classname in mavenCentral search.

  use "c:"(classname) or "fc:"( fully-qualified classname ) in dependencies script block.
  
  example:
  
      compile("fc:org.junit.Test")
      
      compile("c:Junit")


![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/classname-query.gif)

* Support Maven Index search option and Nexus search option.
* Add specified repository to repositories.Use `Show Intention Actions` action (`Alt + Enter` or ⌥⏎) and choose `Add specified repository to repositories.`

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/add-repo.gif)

### Settings

* Make sure `Smart Type Completion` is on.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/smart-type-completion.png)

* Gradle Dependencies And Plugins Helper configuration.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/settings.png)

* Use Maven Index (Android Studio not support) should update Maven Repositories Index.

![](http://bestwu.cn/images/gradle-dependencies-plugins-helper-plugin/settings-maven-repositories.png)
