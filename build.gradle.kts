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
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
//        jetbrainsRuntime()
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    intellijPlatform {
        intellijIdeaCommunity("2022.3.3")

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.groovy")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("org.jetbrains.kotlin")

//        jetbrainsRuntime()
        instrumentationTools()
        pluginVerifier()
        testFramework(TestFrameworkType.Bundled)
    }

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    buildSearchableOptions.set(false)
    publishing {
        token.set(project.findProperty("intellij.publish.token") as String)
        channels.set(listOf("stable"))
    }

    pluginConfiguration {
        id.set("cn.bestwu.gdph")
        name.set("Gradle Dependencies And Plugins Helper")
        version.set("${project.version}")
        vendor {
            name.set("Peter Wu")
            email.set("piterwu@outlook.com")
            url.set("https://github.com/top-bettercode/gradle-dependencies-plugins-helper-plugin")
        }
        ideaVersion.sinceBuild.set("IC-223.8836.41")
        ideaVersion.untilBuild.set("IC-243.*")
        changeNotes.set(
            """
        <b>${project.version}</b><br/><br/>
        <ul>
          <li>Adaptation to version 2024.2.</li>
        </ul>
    """
        )
    }
    pluginVerification {
        ides {
            recommended()
            ide(IntelliJPlatformType.IntellijIdeaCommunity, "2022.3.3")
            ide(IntelliJPlatformType.IntellijIdeaUltimate, "2022.3.3")
            select {
                types.set(
                    listOf(
                        IntelliJPlatformType.IntellijIdeaCommunity,
                        IntelliJPlatformType.IntellijIdeaUltimate
                    )
                )
                channels.set(listOf(ProductRelease.Channel.RELEASE))
                sinceBuild.set("IC-223.8836.41")
                untilBuild.set("IC-243.*")
            }
        }
    }
}

tasks {
    runIde {
        jvmArgs("-Didea.kotlin.plugin.use.k2=true")
    }
}