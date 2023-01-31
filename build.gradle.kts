import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
    kotlin("jvm") version "1.7.22"
}

group = "cn.bestwu"
version = "0.1.8"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2022.2.4")
    plugins.set(listOf("java", "Groovy", "gradle", "Kotlin", "junit"))
    intellij.updateSinceUntilBuild.set(false)
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("222")
        changeNotes.set(
            """
    <b>${project.version}</b><br/><br/>
    <ul>
      <li>Search Optimization.</li>
    </ul>
"""
        )
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    publishPlugin {
        token.set(project.findProperty("intellij.publish.token") as String)
        channels.set(listOf("stable"))
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}