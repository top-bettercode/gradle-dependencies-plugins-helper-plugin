import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.1.4-3"
    id("org.jetbrains.intellij") version "0.2.17"
}
group = "cn.bestwu"
version = "0.1.1"

val ideaVersion: Any? by project

intellij {
    updateSinceUntilBuild = false
//    downloadSources = false
    version = ideaVersion as String
    setPlugins("Groovy", "gradle", "Kotlin", "maven", "properties", "junit")
}

tasks.withType(PublishTask::class.java) {
    username(project.findProperty("intellij.publish.username"))
    password(project.findProperty("intellij.publish.password"))
    channels("stable")
}

repositories {
    jcenter()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:1.1.4-3")
    compile("org.jsoup:jsoup:1.10.3")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.1.4-3")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    apiVersion = "1.1"
}

