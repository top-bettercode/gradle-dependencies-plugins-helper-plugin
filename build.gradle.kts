import org.gradle.kotlin.dsl.compileKotlin
import org.gradle.kotlin.dsl.compileTestKotlin
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    kotlin("jvm") version "2.0.10"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "cn.bestwu"
version = "0.1.11"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        pluginVersion.set("${project.version}")
        sinceBuild.set("242.20224")
        changeNotes.set(
            """
    <b>${project.version}</b><br/><br/>
    <ul>
      <li>Adaptation to version 2024.2.</li>
    </ul>
"""
        )
    }

    compileKotlin {
        compilerOptions {
            apiVersion.set(KotlinVersion.KOTLIN_2_0)
            languageVersion.set(KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    compileTestKotlin {
        compilerOptions {
            apiVersion.set(KotlinVersion.KOTLIN_2_0)
            languageVersion.set(KotlinVersion.KOTLIN_2_0)
            jvmTarget.set(JvmTarget.JVM_17)
        }
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

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    intellijPlatform {
        intellijIdeaCommunity("2024.2")

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.groovy")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("org.jetbrains.kotlin")

        instrumentationTools()
        pluginVerifier()
        testFramework(TestFrameworkType.Bundled)
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2024.2.0.1")
            recommended()
            select {
                types.set(listOf(IntelliJPlatformType.IntellijIdeaUltimate))
                channels.set(listOf(ProductRelease.Channel.RELEASE))
                sinceBuild.set("242.20224")
                untilBuild.set("242.*")
            }
        }
    }
}