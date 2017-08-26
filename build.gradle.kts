import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "0.2.16"
}
group = "cn.bestwu"
version = "0.0.3"
intellij {
    updateSinceUntilBuild = false
//    version = "IC-2017.2.2"
    version = "145.258.11"
    setPlugins("Groovy", "gradle", "Kotlin", "maven")
}

tasks.withType(PublishTask::class.java) {
    username(project.property("intellij.publish.username"))
    password(project.property("intellij.publish.password"))
    channels("stable")
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jsoup:jsoup:1.10.3")

    testCompile(kotlin("test-junit"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    apiVersion = "1.1"
}

