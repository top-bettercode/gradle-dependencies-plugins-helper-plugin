# GradleDependenciesAndPluginsHelper
[![Build Status](https://travis-ci.org/bestwu/gradle-dependencies-plugins-helper-plugin.svg?branch=master)](https://travis-ci.org/bestwu/gradle-dependencies-plugins-helper-plugin)
[![Version](http://phpstorm.espend.de/badge/10033/version)](https://plugins.jetbrains.com/plugin/10033-gradle-dependencies-and-plugins-helper)
[![Downloads](http://phpstorm.espend.de/badge/10033/downloads)](https://plugins.jetbrains.com/plugin/10033-gradle-dependencies-and-plugins-helper)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

[中文说明](https://bestwu.github.io/2017/09/01/gradle-dependencies-plugins-helper-plugin/)

This is an IntelliJ IDEA plugin for searching dependencies/plugins from [JCentral](https://bintray.com/search)/[GradlePlugins](https://plugins.gradle.org/) inside Gradle projects.

Inspired by [https://github.com/siosio/GradleDependenciesHelperPlugin](https://github.com/siosio/GradleDependenciesHelperPlugin).

## Features

* Use `Smart Type Completion` in dependencies/plugins script block.

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/plugins.gif)

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/dependencies.gif)

* Support *.gradle,*.gradle.kts.

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/plugins.kts.gif)

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/dependencies.kts.gif)

* Use [jcenter API](https://bintray.com/docs/api/) for Gradle dependencies queries, Use [Gradle Plugins](https://plugins.gradle.org) for Gradle plugins queries.
* Support wildcard query * .

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/wildcard.gif)

* Support search by classname in mavenCentral search.

  use "c:"(classname) or "fc:"( fully-qualified classname ) in dependencies script block.
  
  example:
  
      compile("fc:org.junit.Test")
      
      compile("c:Junit")


![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/classname-query.gif)

* Support `Use Maven Index for search` and `Use Nexus for search` options.
* Add specified repository to repositories.Use `Show Intention Actions` action (`Alt + Enter` or ⌥⏎) and choose `Add specified repository to repositories.`

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/add-repo.gif)

## Settings

* Make sure `Smart Type Completion` is on.

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/smart-type-completion.png)

* `Use Maven Index for search` and `Use Nexus for search` is optional.When you can't visit https://api.bintray.com/search/packages/maven or access very slowly,you can use these options to speed up the search.

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/settings.png)

* If `Use Maven Index` (Android Studio not support) is on, you should update `Maven Repositories Index`.

![](https://bestwu.github.io/images/gradle-dependencies-plugins-helper-plugin/settings-maven-repositories.png)
