# GradleDependenciesAndPluginsHelper
[![Build Status](https://travis-ci.org/bestwu/gradle-dependencies-plugins-helper-plugin.svg?branch=master)](https://travis-ci.org/bestwu/gradle-dependencies-plugins-helper-plugin)
[![Version](http://phpstorm.espend.de/badge/10033/version)](https://plugins.jetbrains.com/plugin/10033-gradle-dependencies-and-plugins-helper)
[![Downloads](http://phpstorm.espend.de/badge/10033/downloads)](https://plugins.jetbrains.com/plugin/10033-gradle-dependencies-and-plugins-helper)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

[中文说明](README_CN.md)

This is an IntelliJ IDEA plugin for searching dependencies/plugins from [JCentral](https://bintray.com/search)/[GradlePlugins](https://plugins.gradle.org/) inside Gradle projects.

Inspired by [https://github.com/siosio/GradleDependenciesHelperPlugin](https://github.com/siosio/GradleDependenciesHelperPlugin).

## Features

* Use `Smart Type Completion` in dependencies/plugins script block.

![](doc/images/plugins.gif)

![](doc/images/dependencies.gif)

* Support *.gradle,*.gradle.kts.

![](doc/images/plugins.kts.gif)

![](doc/images/dependencies.kts.gif)

* Use [jcenter API](https://bintray.com/docs/api/) for Gradle dependencies queries, Use [Gradle Plugins](https://plugins.gradle.org) for Gradle plugins queries.
* Support wildcard query * .

![](doc/images/wildcard.gif)

* Support search by classname in mavenCentral search.

  use "c:"(classname) or "fc:"( fully-qualified classname ) in dependencies script block.
  
  example:
  
      compile("fc:org.junit.Test")
      
      compile("c:Junit")


![](doc/images/classname-query.gif)

* Add specified repository to repositories.Use `Show Intention Actions` action (`Alt + Enter` or ⌥⏎) and choose `Add specified repository to repositories.`

![](doc/images/add-repo.gif)

## Settings

* Make sure `Smart Type Completion` is on.

![](doc/images/smart-type-completion.png)

* Support `Use AliRepo Search`,`Use Maven Central Repository Search`,`Use Nexus2 Repository Search`(Nexus2),`Use Artifactory Repository Search` options.

![](doc/images/settings.png)

