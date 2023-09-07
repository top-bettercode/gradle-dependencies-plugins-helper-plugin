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

package cn.bestwu.gdph.search

import cn.bestwu.gdph.browseNotification
import cn.bestwu.gdph.show
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import groovy.json.JsonSlurper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
val regex = Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",")

fun getConnection(spec: String, requestProperties: Map<String, List<String>> = mapOf()): HttpURLConnection {
    val url = URL(spec)
    val httpURLConnection = url.openConnection() as HttpURLConnection
    httpURLConnection.connectTimeout = 4000
    httpURLConnection.readTimeout = 4000
    requestProperties.forEach { (t, u) ->
        u.forEach {
            httpURLConnection.addRequestProperty(t, it)
        }
    }
    return httpURLConnection
}

private fun getResponse(connection: HttpURLConnection, project: Project): InputStream? {
    try {
        if (connection.responseCode != 200) {
            val responseText = (connection.errorStream?.bufferedReader()?.readText()
                    ?: connection.inputStream.bufferedReader().readText())
            if (connection.responseCode != 404 && (connection.responseCode != 400 || !responseText.contains("This REST API is available only in Artifactory Pro")))
                show(project, "response:$responseText.", "No dependencies found", NotificationType.WARNING)
            return null
        }
        return connection.inputStream
    } catch (e: Exception) {
        val url = connection.url.toString()
        browseNotification(project, "Request timeout", url)
        return null
    }
}

internal fun getResponseText(connection: HttpURLConnection, project: Project): String? {
    try {
        val stream = getResponse(connection, project) ?: return null
        return stream.bufferedReader().readText()
    } catch (e: Exception) {
        val url = connection.url.toString()
        browseNotification(project, "Request timeout", url)
        return null
    }
}

internal fun getResponseJson(connection: HttpURLConnection, project: Project): Any? {
    try {
        val stream = getResponse(connection, project) ?: return null
        return JsonSlurper().parse(stream)
    } catch (e: Exception) {
        val url = connection.url.toString()
        browseNotification(project, "Request timeout", url)
        return null
    }
}


internal fun getAliRepos(): List<String> {
    try {
        val connection = getConnection("https://maven.aliyun.com/repo/list?_input_charset=utf-8")
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val jsonResult = JsonSlurper().parse(inputStream)
            @Suppress("UNCHECKED_CAST")
            return ((jsonResult as Map<*, *>)["object"] as List<Map<*, *>>).map { it["repoId"].toString() }
        }
    } catch (_: Exception) {
    }
    return emptyList()
}


internal val versionTails = arrayOf("SNAPSHOTS", "ALPHA", "BETA", "M", "RC", "RELEASE")
internal val versionTailRegex = "^([A-Za-z]+?)(\\d*)$".toRegex()

/**
 * 比较版本信息

 * @param version1 版本1
 * *
 * @param version2 版本2
 * *
 * @return int
 */
fun compareVersion(version1: String, version2: String): Int {
    if (version1 == version2) {
        return 0
    }
    val separator = "[.-]"
    val version1s = version1.split(separator.toRegex()).toMutableList()
    val version2s = version2.split(separator.toRegex()).toMutableList()

    if (version1s.size < version2s.size) {
        version1s.addAll(List(version2s.size - version1s.size) { "" })
    } else {
        version2s.addAll(List(version1s.size - version2s.size) { "" })
    }
    val length = version1s.size

    for (i in 0 until length) {
        val toIntOrNull2 = version2s[i].toIntOrNull()
        val toIntOrNull1 = version1s[i].toIntOrNull()
        if (toIntOrNull1 == null && toIntOrNull2 != null)
            return -1
        else if (toIntOrNull1 != null && toIntOrNull2 == null)
            return 1
        var v2 = toIntOrNull2
                ?: versionTails.indexOf(
                        version2s[i].replace(versionTailRegex, "$1").uppercase(Locale.getDefault())
                )
        var v1 = toIntOrNull1
                ?: versionTails.indexOf(
                        version1s[i].replace(versionTailRegex, "$1").uppercase(Locale.getDefault())
                )
        if (v1 != -1 && v1 == v2 && toIntOrNull1 == null) {
            v2 = version2s[i].replace(versionTailRegex, "$2").toIntOrNull() ?: 0
            v1 = version1s[i].replace(versionTailRegex, "$2").toIntOrNull() ?: 0
        }
        if (v1 == -1 || v2 == -1) {
            val result = version1s[i].compareTo(version2s[i])
            if (result != 0) {
                return result
            }
        }
        if (v2 > v1) {
            return -1
        } else if (v2 < v1) {
            return 1
        }
        // 相等 比较下一组值
    }
    return 0
}