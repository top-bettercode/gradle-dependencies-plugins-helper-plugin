import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.tasks.PublishTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "0.2.16"
}
group = "cn.bestwu"
version = "0.0.2"
intellij {
    pluginName = "Gradle Dependencies And Plugins Helper"
    updateSinceUntilBuild = false
//    version = "IC-2017.2.1"
    version = "145.258.11"
    setPlugins("Groovy", "gradle", "Kotlin", "maven")
}

tasks.withType(PublishTask::class.java) {
    username(project.property("intellij.publish.username"))
    password(project.property("intellij.publish.password"))
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jsoup:jsoup:1.10.3")

    testCompile(kotlin("test-junit"))
}

