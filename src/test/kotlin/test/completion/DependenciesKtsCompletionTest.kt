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

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class DependenciesKtsCompletionTest : CodeInsightTestBase() {

    //build.gradle.kts dependencies
    fun testKtsDependencies() {
        completionCheckResult(gradleKtsFileName, """dependencies{
    compile("kotlin-reflect$caret")
}""", {"""dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect")
}"""}, "org.jetbrains.kotlin:kotlin-reflect")
    }

    fun testKtsDependenciesVersion() {
        completionCheckResult(gradleKtsFileName, """dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect$caret")
}""", {"""dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect:$it")
}"""}, "org.jetbrains.kotlin:kotlin-reflect:")
    }

    //build.gradle.kts dependencies kotlin()
    fun testKtsDependenciesKotlin() {
        completionCheckResult(gradleKtsFileName, """dependencies{
    compile(kotlin("$caret"))
}""", {"""dependencies{
    compile(kotlin("stdlib-jre8"))
}"""}, "stdlib-jre8")
    }

    fun testKtsDependenciesKotlinVersion() {
        completionCheckResult(gradleKtsFileName, """dependencies{
    compile(kotlin("stdlib-jre8","$caret"))
}""", {"""dependencies{
    compile(kotlin("stdlib-jre8","$it"))
}"""}, "")
    }

    //build.gradle.kts dependencies *
    fun testKtsDependenciesWildcard() {
        completionCheckResult(gradleKtsFileName, """dependencies{
    testCompile("kotlin*junit$caret")
}""", {"""dependencies{
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}"""}, "org.jetbrains.kotlin:kotlin-test-junit")
    }

    //build.gradle.kts dependencies by className
    fun testDependenciesByFullyQualifiedClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile("fc:feign.Client$caret")
}""", {"""dependencies{
    compile("com.netflix.feign:feign-core:$it")
}"""}, "com.netflix.feign:feign-core:")
    }
    fun testDependenciesByClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile("c:feign$caret")
}""", {"""dependencies{
    compile("com.netflix.feign:feign-core:$it")
}"""}, "com.netflix.feign:feign-core:")
    }

}