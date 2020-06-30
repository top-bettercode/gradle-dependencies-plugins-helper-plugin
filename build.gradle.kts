import org.jetbrains.intellij.tasks.PublishTask

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.21"
}
group = "cn.bestwu"
version = "0.1.6"

val ideaVersion = property("ideaVersion")

intellij {
    updateSinceUntilBuild = false
//    downloadSources = false
    version = ideaVersion as String
    setPlugins("java", "Groovy", "gradle", "Kotlin", "junit")
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
    implementation("org.jsoup:jsoup:1.13.1")
}

