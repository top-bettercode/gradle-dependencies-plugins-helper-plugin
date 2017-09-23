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
class DependenciesCompletionTest : CodeInsightTestBase() {

    //build.gradle dependencies
    fun testStdDependencies() {
        completionCheckResult(gradleFileName, """dependencies {
    compile 'kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect'
}""", "org.jetbrains.kotlin:kotlin-reflect")
    }

    fun testStdDependenciesVersion() {
        completionCheckResult(gradleFileName, """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion'
}""", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }

    //build.gradle dependencies map notation

    fun testDependenciesMapGroup() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'kotlin$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin'
}""", "org.jetbrains.kotlin")
    }

    fun testDependenciesMapName() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect'
}""", "kotlin-reflect")
    }

    fun testDependenciesMapVersion() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$kotlinVersion'
}""", kotlinVersion)
    }


    //build.gradle dependencies *
    fun testDependenciesWildcard() {
        completionCheckResult(gradleFileName, """dependencies{
    testCompile 'kotlin*junit$caret'
}""", """dependencies{
    testCompile 'org.jetbrains.kotlin:kotlin-test-junit'
}""", "org.jetbrains.kotlin:kotlin-test-junit")
    }

    //build.gradle dependencies by className
    fun testDependenciesByFullyQualifiedClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile 'fc:feign.Client$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }

    fun testDependenciesByClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile 'c:feign$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }


}