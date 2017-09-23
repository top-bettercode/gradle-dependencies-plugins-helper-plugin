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

package test.intention

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class AddRepositoriesIntentionTest : CodeInsightTestBase() {

    private val intentionAfter = """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1'
}"""
    private val intentionCenterAfter = """repositories {
    jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3'
}"""

    //build.gradle
    fun testAddRepositoriesIntention() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionAddRepos() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1'
}""")
    }

    fun testAddRepositoriesIntentionExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionCenterNoRepo() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", intentionCenterAfter)
    }

    fun testAddRepositoriesIntentionCenter() {
        intentionCheckResult(gradleFileName, """repositories {
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", """repositories {
jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3'
}""")
    }

    fun testAddRepositoriesIntentionCenterExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", intentionCenterAfter)
    }

}