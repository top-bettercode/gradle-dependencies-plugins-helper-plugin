package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import groovy.json.JsonSlurper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class GradleArtifactSearcher {
    companion object {
        private val regex = Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",")

        private fun getConnection(spec: String): HttpURLConnection {
            val url = URL(spec)
            return url.openConnection() as HttpURLConnection
        }

        private fun getResponse(connection: HttpURLConnection, project: Project): InputStream? {
            if (connection.responseCode != 200) {
                show(project, "response:${connection.errorStream?.bufferedReader()?.readText() ?: connection.inputStream.bufferedReader().readText()}.", "find dependencies fail", NotificationType.WARNING)
                return null
            }
            return connection.inputStream
        }

        private val artifactsCaches = HashMap<String, List<ArtifactInfo>>()
    }


    fun search(searchParam: SearchParam, project: Project): List<ArtifactInfo> {
        val existResult = artifactsCaches[searchParam.q]
        if (existResult != null) {
            return existResult
        }
        val result: MutableList<ArtifactInfo>
        if (searchParam.advancedSearch.isNotEmpty()) {
            result = searchByClassNameInMavenCentral(searchParam, project)
        } else {
            result = searchInJcenter(searchParam, project)
//            if (result.isEmpty())
//                result = searchInMavenCentral(searchParam, project)
        }
        artifactsCaches.put(searchParam.q, result)
        return result
    }


    @Suppress("UNCHECKED_CAST")
    private fun searchByClassNameInMavenCentral(searchParam: SearchParam, project: Project): MutableList<ArtifactInfo> {
        val result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "http://search.maven.org/solrsearch/select?q=${searchParam.advancedSearch}:\"${searchParam.id}\"&core=gav&rows=1000&wt=json"
        val connection = getConnection(url)
        val stream = getResponse(connection, project) ?: return result
        regex.findAll(stream.bufferedReader().readText()).forEach {
            val artifactInfo = ArtifactInfo(it.groupValues[1], it.groupValues[2], it.groupValues[3], "mavenCentral", "Apache")
            val exist = result.find { it.id == artifactInfo.id }
            if (exist != null) {
                if (compareVersion(exist.version, artifactInfo.version) < 0) {
                    exist.version = artifactInfo.version
                }
            } else {
                result.add(artifactInfo)
            }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST", "unused")
    private fun searchInMavenCentral(searchParam: SearchParam, project: Project): MutableList<ArtifactInfo> {
        val result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "http://search.maven.org/solrsearch/select?q=${searchParam.mq}&rows=50&core=gav&wt=json"
        val connection = getConnection(url)
        val stream = getResponse(connection, project) ?: return result
        regex.findAll(stream.bufferedReader().readText()).forEach {
            val artifactInfo = ArtifactInfo(it.groupValues[1], it.groupValues[2], it.groupValues[3], "mavenCentral", "Apache")
            result.add(artifactInfo)
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchInJcenter(searchParam: SearchParam, project: Project): MutableList<ArtifactInfo> {
        var result: MutableList<ArtifactInfo> = mutableListOf()

        val url = "https://api.bintray.com/search/packages/maven?${searchParam.q}"
        val connection = getConnection(url)
        val stream = getResponse(connection, project) ?: return result
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

