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
class MapNotationAddRepositoriesIntentionTest : CodeInsightTestBase() {

    private val intentionAfter = """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}"""
    private val intentionCenterAfter = """repositories {
    jcenter()
}

dependencies{
    compile group: 'org.jsoup', name: 'jsoup', version: '1.10.3'
}"""

    //build.gradle
    fun testAddRepositoriesIntention() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionAddRepos() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
    }

    fun testAddRepositoriesIntentionExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionEmpty() {
        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: '$caret', version: '9.5.1'
}""")
        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: '', version: '9.5.1'
}""")

        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: '$caret', name: 'feign-core', version: '9.5.1'
}""")
    }


    fun testAddRepositoriesIntentionCenterNoRepo() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'org.jsoup$caret', name: 'jsoup', version: '1.10.3'
}""", intentionCenterAfter)
    }

    fun testAddRepositoriesIntentionCenter() {
        intentionCheckResult(gradleFileName, """repositories {
}

dependencies{
    compile group: 'org.jsoup$caret', name: 'jsoup', version: '1.10.3'
}""", """repositories {
jcenter()
}

dependencies{
    compile group: 'org.jsoup', name: 'jsoup', version: '1.10.3'
}""")
    }

    fun testAddRepositoriesIntentionCenterExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'org.jsoup', name: 'jsoup$caret', version: '1.10.3'
}""", intentionCenterAfter)
    }

}