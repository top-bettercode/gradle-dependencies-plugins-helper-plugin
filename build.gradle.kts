plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
}

group = "cn.bestwu"
version = "0.1.8"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

intellij {
    version.set("2022.2.4")
    plugins.set(listOf("java", "Groovy", "gradle", "Kotlin", "junit"))
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set("222")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
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
}

