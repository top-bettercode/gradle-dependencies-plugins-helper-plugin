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


    fun search(searchParam: SearchParam): List<ArtifactInfo> {
        val existResult = artifactsCaches[searchParam.q]
        if (existResult != null) {
            return existResult
        }
        val result: MutableList<ArtifactInfo>
        if (searchParam.advancedSearch.isNotEmpty()) {
            result = searchByClassNameInMavenCentral(searchParam)
        } else {
            result = searchInJcenter(searchParam)
        }

        artifactsCaches.put(searchParam.q, result)
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchByClassNameInMavenCentral(searchParam: SearchParam): MutableList<ArtifactInfo> {
        val result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "http://search.maven.org/solrsearch/select?q=${searchParam.advancedSearch}:\"${searchParam.id}\"&core=gav&rows=30&wt=json"
        val connection = getConnection(url)
        val stream = getResponse(connection) ?: return result
        val docs = ((JsonSlurper().parse(stream) as Map<*, *>)["response"] as Map<*, *>) ["docs"] as List<Map<*, *>>
        docs.forEach {
            result.add(ArtifactInfo(it["g"] as String, it["a"] as String, it["v"] as String, "mavenCentral", "Apache"))
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchInJcenter(searchParam: SearchParam): MutableList<ArtifactInfo> {
        var result: MutableList<ArtifactInfo> = mutableListOf()

        val url = "https://api.bintray.com/search/packages/maven?${searchParam.q}"
        val connection = getConnection(url)
        val stream = getResponse(connection) ?: return result
        var jsonResult = JsonSlurper().parse(stream) as List<Map<*, *>>
        val findById = jsonResult.filter { (it["system_ids"] as List<String>).contains(searchParam.id) }
        if (findById.isNotEmpty()) {
            jsonResult = findById
        }
        for (any in jsonResult) {
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
        return result
    }


}

