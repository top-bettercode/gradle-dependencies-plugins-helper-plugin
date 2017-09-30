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

import cn.bestwu.gdph.show
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author Peter Wu
 * @since
 */
val regex = Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",")

internal fun getConnection(spec: String): HttpURLConnection {
    val url = URL(spec)
    return url.openConnection() as HttpURLConnection
}

internal fun getResponse(connection: HttpURLConnection, project: Project): InputStream? {
    if (connection.responseCode != 200) {
        show(project, "response:${connection.errorStream?.bufferedReader()?.readText() ?: connection.inputStream.bufferedReader().readText()}.", "find dependencies fail", NotificationType.WARNING)
        return null
    }
    return connection.inputStream
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
    val version1s = version1.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val version2s = version2.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    val vl = version1s.size < version2s.size
    val length = if (vl) version1s.size else version2s.size

    for (i in 0 until length) {
        val toIntOrNull2 = version2s[i].toIntOrNull()
        val toIntOrNull1 = version1s[i].toIntOrNull()
        if (toIntOrNull1 == null && toIntOrNull2 != null)
            return -1
        else if (toIntOrNull1 != null && toIntOrNull2 == null)
            return 1
        var v2 = toIntOrNull2 ?: versionTails.indexOf(version2s[i].replace(versionTailRegex, "$1").toUpperCase())
        var v1 = toIntOrNull1 ?: versionTails.indexOf(version1s[i].replace(versionTailRegex, "$1").toUpperCase())
        if (v1 == v2 && toIntOrNull1 == null && toIntOrNull2 == null) {
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
    if (vl) {
        return if (version2s.last().matches(versionTailRegex))
            1
        else
            -1
    } else if (version1s.size > version2s.size) {
        return if (version1s.last().matches(versionTailRegex))
            -1
        else
            1
    }
    return 0
}