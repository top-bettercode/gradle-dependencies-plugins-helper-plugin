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

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class NoneCompletionTest : CodeInsightTestBase() {

    //build.gradle dependencies
    fun testEndNone() {
        completionCheckResult(gradleKtsFileName, """plugins {
    kotlin("jvm$caret""",
                """plugins {
    kotlin("jvm"""
        )
    }

    fun testEndNone1() {
        completionCheckResult(gradleFileName, """plugins {
    kotlin("jvm") version "1.1.50"
}

repo$caret """,
                """plugins {
    kotlin("jvm") version "1.1.50"
}

repo """
        )
    }

    fun testEndNone2() {
        completionCheckResult(gradleFileName, """plugins {
    kotlin("jvm") version "1.1.50"
}

repo$caret""",
                """plugins {
    kotlin("jvm") version "1.1.50"
}

repo"""
        )
    }

    //build.gradle dependencies
    fun testNone() {
        completionCheckResult(gradleFileName, caret, "")
    }

}