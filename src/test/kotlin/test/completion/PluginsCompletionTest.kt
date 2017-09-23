/*
 * Copyright (c) 2017 Peter Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.completion

import cn.bestwu.gdph.GradlePluginsSearcher
import test.CodeInsightTestBase


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
        completionCheckResult(gradleFileName, """plugins {
    id 'intellij$caret'
}""", pluginsAfter, "org.jetbrains.intellij${GradlePluginsSearcher.splitRule}$intellijPluginVersion")
    }

    fun testStdPluginVersion() {
        completionCheckResult(gradleFileName, """plugins {
    id 'org.jetbrains.intellij' version '$caret'
}""", pluginsAfter, intellijPluginVersion)
    }

    //build.gradle.kts plugins
    fun testKtsPluginId() {
        completionCheckResult(gradleKtsFileName, """plugins {
    id("intellij$caret")
}""", pluginsKtsAfter, "org.jetbrains.intellij${GradlePluginsSearcher.splitRule}$intellijPluginVersion")
    }

    fun testKtsPluginVersion() {
        completionCheckResult(gradleKtsFileName, """plugins {
    id("org.jetbrains.intellij") version "$caret"
}""", pluginsKtsAfter, intellijPluginVersion)
    }

    //build.gradle.kts plugins kotlin("")
    fun testKtsKotlinPluginId() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("$caret")
}""", pluginsKtsKotlinAfter, "jvm${GradlePluginsSearcher.splitRule}$kotlinVersion")
    }

    fun testKtsKotlinPluginVersion() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm") version "$caret"
}""", pluginsKtsKotlinAfter, kotlinVersion)
    }

    fun testKtsKotlinPluginVersion2() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm","$caret")
}""", """plugins {
    kotlin("jvm","$kotlinVersion")
}""", kotlinVersion)
    }

}