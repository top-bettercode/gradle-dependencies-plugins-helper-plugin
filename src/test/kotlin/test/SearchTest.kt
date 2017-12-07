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

@file:Suppress("UNCHECKED_CAST")

package test

import cn.bestwu.gdph.search.getConnection
import groovy.json.JsonSlurper
import org.junit.Test

/**
 *
 * @author Peter Wu
 * @since
 */
class NexusSearcherTest {

    @Test
    fun doSearchByClassName() {
//        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?cn=com.sun.jna.examples.win32.W32API.HWND"
        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?cn=org.springframework.http.converter.json.MappingJacksonHttpMessageConverter"
        val connection = getConnection(url)
        connection.setRequestProperty("Accept", "application/json")
        val stream = connection.inputStream
        val jsonResult = (JsonSlurper().parse(stream) as Map<*, *>)["data"] as List<Map<*, *>>
        jsonResult.forEach {
            val repo = if (it["latestReleaseRepositoryId"] != null) {
                it["latestReleaseRepositoryId"]
            } else {
                it["latestSnapshotRepositoryId"]
            }
            println(it.keys)
            println(it["groupId"] as String + ":" + it["artifactId"] as String + ":" + (if (it["latestRelease"] == null) it["version"] else it["latestRelease"]) as String + ":" + repo as String)
        }
    }
}