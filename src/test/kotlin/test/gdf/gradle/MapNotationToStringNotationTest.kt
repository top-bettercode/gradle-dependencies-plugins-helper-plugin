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

package test.gdf.gradle

import cn.bestwu.gdf.gradle.MapNotationToStringNotationAction
import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class MapNotationToStringNotationTest :  CodeInsightTestBase(){

    fun testToStringNotation() {
        actionCheckResult(gradleFileName,"""dependencies {
    compile 'com.google.code.gson:gson:2.1'
    compile group: 'commons-beanutils', name: 'commons-beanutils-core', version: '1.7.0'
    compile group: 'commons-beanutils', name: 'commons-beanutils-core', version: '1.7.0', classifier: 'jdk5'
    compile group: 'commons-lang', name: 'commons-lang', version: '2.4'
    compile(group: 'org.apache.zookeeper', name: 'zookeeper', version: '3.4.6') {
        exclude(module: 'slf4j-log4j12')
    }
    compile group: 'org.slf4j', name: 'slf4j-api', version: '1.6.1'
    compileOnly group: 'javax.servlet', name: 'servlet-api', version: '2.5'
    compileOnly group: 'org.springframework', name: 'spring', version: '2.5.6.SEC03'
    testCompile group: 'ch.qos.logback', name: 'logback-classic', version: '0.9.29'
    testCompile group: 'ch.qos.logback', name: 'logback-core', version: '0.9.29'
    testCompile group: 'junit', name: 'junit', version: '4.5'
    testCompile group: 'org.unitils', name: 'unitils', version: '2.3'
}""","""dependencies {
    compile("com.google.code.gson:gson:2.1")
    compile("commons-beanutils:commons-beanutils-core:1.7.0")
    compile("commons-beanutils:commons-beanutils-core:1.7.0:jdk5")
    compile("commons-lang:commons-lang:2.4")
    compile("org.apache.zookeeper:zookeeper:3.4.6") {
        exclude(module: "slf4j-log4j12")
    }
    compile("org.slf4j:slf4j-api:1.6.1")
    compileOnly("javax.servlet:servlet-api:2.5")
    compileOnly("org.springframework:spring:2.5.6.SEC03")
    testCompile("ch.qos.logback:logback-classic:0.9.29")
    testCompile("ch.qos.logback:logback-core:0.9.29")
    testCompile("junit:junit:4.5")
    testCompile("org.unitils:unitils:2.3")
}""",MapNotationToStringNotationAction())
    }



}