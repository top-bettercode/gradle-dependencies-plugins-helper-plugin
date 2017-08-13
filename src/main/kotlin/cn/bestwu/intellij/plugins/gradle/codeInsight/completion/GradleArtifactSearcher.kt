package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.notification.NotificationType
import groovy.json.JsonSlurper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class GradleArtifactSearcher {
    companion object {
        private fun getConnection(spec: String): HttpURLConnection {
            val url = URL(spec)
            return url.openConnection() as HttpURLConnection
        }

        private fun getResponse(connection: HttpURLConnection): InputStream? {
            if (connection.responseCode != 200) {
                show("find dependencies fail", "response:${connection.errorStream?.bufferedReader()?.readText() ?: connection.inputStream.bufferedReader().readText()}.", NotificationType.WARNING)
                return null
            }
            return connection.inputStream
        }

        private val artifactsCaches = HashMap<String, List<ArtifactInfo>>()
    }

    @Suppress("UNCHECKED_CAST")
    fun search(searchParam: SearchParam): List<ArtifactInfo> {
        val existResult = artifactsCaches[searchParam.q]
        if (existResult != null) {
            return existResult
        }
        var result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "https://api.bintray.com/search/packages/maven?${searchParam.q}"
        val connection = getConnection(url)
        val stream = getResponse(connection) ?: return result
        var json = JsonSlurper().parse(stream) as List<Map<*, *>>
        val findById = json.filter { (it["system_ids"] as List<String>).contains(searchParam.id) }
        if (findById.isNotEmpty()) {
            json = findById
        }
        for (any in json) {
            val system_ids = any["system_ids"] as MutableList<String>
            if (system_ids.contains(searchParam.id)) {
                system_ids.clear()
                system_ids.add(searchParam.id)
            }
            for (id in system_ids) {
                val list = split(id)
                val groupId: String
                val artifactId: String
                if (list.size == 2) {
                    groupId = list[0]
                    artifactId = list[1]
                } else {
                    groupId = id
                    artifactId = ""
                }
                if (searchParam.id == id) {
                    ((any["versions"] as List<String>).mapTo(result) { ArtifactInfo(groupId, artifactId, it, any["repo"] as String, any["owner"] as String) })
                } else {
                    result.add(ArtifactInfo(groupId, artifactId, "", any["repo"] as String, any["owner"] as String))
                }
            }
        }
        if (findById.size > 1 && findById.any { "spring" == it["owner"] }) {
            result = result.sortedWith(kotlin.Comparator<ArtifactInfo> { o1, o2 ->
                compareVersion(o2.version, o1.version)
            }).toMutableList()
        }

        artifactsCaches.put(searchParam.q, result)
        return result
    }


}

