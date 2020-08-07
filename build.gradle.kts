import org.jetbrains.intellij.tasks.PublishTask

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.21"
}
group = "cn.bestwu"
version = "0.1.8"

val ideaVersion = property("ideaVersion")

intellij {
    updateSinceUntilBuild = false
//    downloadSources = false
    version = ideaVersion as String
    setPlugins("java", "Groovy", "gradle", "Kotlin", "junit")
}

tasks.withType(org.jetbrains.intellij.tasks.PatchPluginXmlTask::class.java) {
    changeNotes("""
    <b>${project.version}</b><br/><br/>
    <ul>
      <li>Search Optimization.</li>
    </ul>
""")
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
}

