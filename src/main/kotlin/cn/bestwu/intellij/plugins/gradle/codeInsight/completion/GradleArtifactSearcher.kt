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
    fun search(artifactInfo: ArtifactInfo): List<ArtifactInfo> {
        var result = artifactsCaches[artifactInfo.id]
        if (result != null) {
            return result
        }
        result = mutableListOf()
        val url = "https://api.bintray.com/search/packages/maven?${artifactInfo.q}"
        val connection = getConnection(url)
        val stream = getResponse(connection) ?: return result
        val json = JsonSlurper().parse(stream) as List<Map<*, *>>
        for (any in json) {
            for (id in any["system_ids"] as List<String>) {
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
                if (artifactInfo.id == id) {
                    ((any["versions"] as List<String>).mapTo(result) { ArtifactInfo(groupId, artifactId, it, any["repo"] as String, any["owner"] as String) })
                } else {
                    result.add(ArtifactInfo(groupId, artifactId, "", any["repo"] as String, any["owner"] as String))
                }
            }
        }
        artifactsCaches.put(artifactInfo.q, result)
        return result
    }

}

