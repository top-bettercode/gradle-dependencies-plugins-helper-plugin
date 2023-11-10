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

package test.gdph.completion

import cn.bestwu.gdph.search.GradlePluginsSearcher
import test.CodeInsightTestBase


/**
 *
 * @author Peter Wu
 * @since
 */
class PluginsCompletionTest : CodeInsightTestBase() {

    companion object {

        private val pluginsAfter: (String) -> String = {
            """plugins {
    id 'org.jetbrains.intellij' version '$it'
}"""
        }
        private val pluginsKtsAfter: (String) -> String = {
            """plugins {
    id("org.jetbrains.intellij") version "$it"
}"""
        }
        private val pluginsKtsKotlinAfter: (String) -> String = {
            """plugins {
    kotlin("jvm") version "$it"
}"""
        }
    }

    // gradle.build plugins
    fun testStdPluginId() {
        completionCheckResult(gradleFileName, """plugins {
    id 'intellij$caret'
}""", pluginsAfter, "org.jetbrains.intellij${GradlePluginsSearcher.SPLIT_RULE}")
    }

    fun testStdPluginVersion() {
        completionCheckResult(gradleFileName, """plugins {
    id 'org.jetbrains.intellij' version '$caret'
}""", pluginsAfter, "")
    }

    //build.gradle.kts plugins
    fun testKtsPluginId() {
        completionCheckResult(gradleKtsFileName, """plugins {
    id("intellij$caret")
}""", pluginsKtsAfter, "org.jetbrains.intellij${GradlePluginsSearcher.SPLIT_RULE}")
    }

    fun testKtsPluginVersion() {
        completionCheckResult(gradleKtsFileName, """plugins {
    id("org.jetbrains.intellij") version "$caret"
}""", pluginsKtsAfter, "")
    }

    //build.gradle.kts plugins kotlin("")
    fun testKtsKotlinPluginId() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("$caret")
}""", pluginsKtsKotlinAfter, "jvm${GradlePluginsSearcher.SPLIT_RULE}")
    }

    fun testKtsKotlinPluginVersion() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm") version "$caret"
}""", pluginsKtsKotlinAfter, "")
    }

    fun testKtsKotlinPluginVersion2() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm","$caret")
}""", {
            """plugins {
    kotlin("jvm","$it")
}"""
        }, "")
    }

}