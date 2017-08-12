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
        var json = JsonSlurper().parse(stream) as List<Map<*, *>>
        val findById = json.filter { (it["system_ids"] as List<String>).contains(artifactInfo.id) }
        if (findById.isNotEmpty()) {
            json = findById
        }
        for (any in json) {
            val system_ids = any["system_ids"] as MutableList<String>
            if (system_ids.contains(artifactInfo.id)) {
                system_ids.clear()
                system_ids.add(artifactInfo.id)
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
                if (artifactInfo.id == id) {
                    ((any["versions"] as List<String>).mapTo(result) { ArtifactInfo(groupId, artifactId, it, any["repo"] as String, any["owner"] as String) })
                } else {
                    result.add(ArtifactInfo(groupId, artifactId, "", any["repo"] as String, any["owner"] as String))
                }
            }
        }
        if (findById.size > 1 && findById.any { "spring" == it["owner"] }) {
            result = result.sortedWith(Comparator<ArtifactInfo> { o1, o2 ->
                compareVersion(o2.version, o1.version)
            })
        }

        artifactsCaches.put(artifactInfo.q, result)
        return result
    }


}

