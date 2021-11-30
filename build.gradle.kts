plugins {
    kotlin("jvm") version "1.5.32"
    id("org.jetbrains.intellij") version "1.3.0"
}

group = "cn.bestwu"
version = "0.1.8"

val intellijVersion = "2021.2.3"

intellij {
    version.set(intellijVersion)
    plugins.set(listOf("java", "Groovy", "gradle", "Kotlin", "junit"))
}


tasks.withType(org.jetbrains.intellij.tasks.PatchPluginXmlTask::class.java) {
    changeNotes.set(
        """
    <b>${project.version}</b><br/><br/>
    <ul>
      <li>Search Optimization.</li>
    </ul>
"""
    )
}


tasks.withType(org.jetbrains.intellij.tasks.PublishPluginTask::class.java) {
    token.set(project.findProperty("intellij.publish.token") as String)
    channels.set(listOf("stable"))
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
}

