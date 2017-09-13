package test.completion


/**
 *
 * @author Peter Wu
 * @since
 */
class PluginsCompletionTest : CodeInsightTestBase() {

    companion object {
        private val intellijPluginVersion = "0.2.17"

        private val pluginsAfter = """plugins {
    id 'org.jetbrains.intellij' version '0.2.17'
}"""
        private val pluginsKtsAfter = """plugins {
    id("org.jetbrains.intellij") version "$intellijPluginVersion"
}"""
        private val pluginsKtsKotlinAfter = """plugins {
    kotlin("jvm") version "$kotlinVersion"
}"""
    }

    // gradle.build plugins
    fun testStdPluginId() {
        doCheckResult(gradleFileName, """plugins {
    id 'intellij$caret'
}""", pluginsAfter, "org.jetbrains.intellij:plugin-version:$intellijPluginVersion")
    }

    fun testStdPluginVersion() {
        doCheckResult(gradleFileName, """plugins {
    id 'org.jetbrains.intellij' version '$caret'
}""", pluginsAfter, intellijPluginVersion)
    }

    //build.gradle.kts plugins
    fun testKtsPluginId() {
        doCheckResult(gradleKtsFileName, """plugins {
    id("intellij$caret")
}""", pluginsKtsAfter, "org.jetbrains.intellij:plugin-version:$intellijPluginVersion")
    }

    fun testKtsPluginVersion() {
        doCheckResult(gradleKtsFileName, """plugins {
    id("org.jetbrains.intellij") version "$caret"
}""", pluginsKtsAfter, intellijPluginVersion)
    }

    //build.gradle.kts plugins kotlin("")
    fun testKtsKotlinPluginId() {
        doCheckResult(gradleKtsFileName, """plugins {
    kotlin("$caret")
}""", pluginsKtsKotlinAfter, "jvm:plugin-version:$kotlinVersion")
    }

    fun testKtsKotlinPluginVersion() {
        doCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm") version "$caret"
}""", pluginsKtsKotlinAfter, kotlinVersion)
    }

    fun testKtsKotlinPluginVersion2() {
        doCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm","$caret")
}""", """plugins {
    kotlin("jvm","$kotlinVersion")
}""", kotlinVersion)
    }

}