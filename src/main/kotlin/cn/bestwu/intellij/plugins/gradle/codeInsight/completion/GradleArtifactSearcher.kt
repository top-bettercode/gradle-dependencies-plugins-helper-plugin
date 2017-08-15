package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import groovy.json.JsonSlurper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class ArtifactInfo(groupId: String, artifactId: String, version: String = "", repo: String = "", owner: String = "") {
    val groupId: String = groupId.trim()
    val artifactId: String = artifactId.trim()
    var version: String = version.trim()
        set(value) {
            field = value
            this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
        }
    val repo: String = repo.trim()
    val owner: String = owner.trim()
    val id: String
    var presentableText: String

    override fun toString(): String {
        return "ArtifactInfo(groupId='$groupId', artifactId='$artifactId', version='$version', id='$id')"
    }

    fun type() = "$repo${if ("jcenter" != repo && "mavenCentral" != repo) " By $owner" else ""}"

    init {
        this.id = "${this.groupId}:${this.artifactId}"
        this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
    }
}

class SearchParam {
    val id: String
    val q: String
    val mq: String
    val nq: String
    val advancedSearch: String
    val failContent: String

    constructor(text: String) {
        if (text.startsWith("c:", true)) {
            id = text.substringAfter("c:").trim()
            q = text.trim()
            mq = text.trim()
            nq = text.trim()
            advancedSearch = "c"
            failContent = "<a href='http://search.maven.org/#search|gav|1|c:\"$id\"'>search in mavenCentral</a>"
        } else if (text.startsWith("fc:", true)) {
            id = text.substringAfter("fc:").trim()
            q = text.trim()
            mq = text.trim()
            nq = text.trim()
            advancedSearch = "fc"
            failContent = "<a href='http://search.maven.org/#search|gav|1|fc:\"$id\"'>search in mavenCentral</a>"
        } else {
            advancedSearch = ""
            val list = split(text)
            if (list.size in (2..3)) {
                val groupId = list[0].trim()
                val artifactId = list[1].trim()
                this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
                this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
                this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
                this.nq = "g=$groupId${if (artifactId.isEmpty()) "" else "&a=$artifactId"}"
            } else {
                this.id = text.trim()
                this.q = "q=${text.trim()}*"
                this.mq = "a:\"${text.trim()}\""
                this.nq = "q=${text.trim()}"
            }
            failContent = "<a href='https://bintray.com/search?query=$id'>search in jcenter</a>"
        }
    }

    constructor(groupIdParam: String, artifactIdParam: String) {
        advancedSearch = ""
        val groupId = groupIdParam.trim()
        val artifactId = artifactIdParam.trim()
        this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
        this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
        this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
        this.nq = "g=$groupId${if (artifactId.isEmpty()) "" else "&a=$artifactId"}"
        failContent = "<a href='https://bintray.com/search?query=$id'>search in jcenter</a>"
    }

    override fun toString(): String {
        return "SearchParam(id='$id', q='$q', advancedSearch='$advancedSearch', failContent='$failContent')"
    }


}

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


    private val versionTails = arrayOf("SNAPSHOTS", "ALPHA", "BETA", "M", "RC", "RELEASE")
    private val versionTailRegex = "^(.*?)\\d*$".toRegex()

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

        for (i in 0..length - 1) {
            val v2 = version2s[i].toIntOrNull() ?: versionTails.indexOf(version2s[i].replace(versionTailRegex, "$1").toUpperCase())
            val v1 = version1s[i].toIntOrNull() ?: versionTails.indexOf(version1s[i].replace(versionTailRegex, "$1").toUpperCase())
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

        if (vl)
            return -1
        else if (version1s.size > version2s.size)
            return 1
        return 0
    }


    fun search(searchParam: SearchParam, project: Project): List<ArtifactInfo> {
        val existResult = artifactsCaches[searchParam.q]
        if (existResult != null) {
            return existResult
        }
        var result: MutableList<ArtifactInfo>
        if (searchParam.advancedSearch.isNotEmpty()) {
            result = searchByClassNameInNexus(searchParam, project)
            if (result.isEmpty())
                result = searchByClassNameInMavenCentral(searchParam, project)
        } else {
            result = searchInNexus(searchParam, project)
//            result = searchInJcenter(searchParam, project)
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

    @Suppress("UNCHECKED_CAST")
    private fun searchByClassNameInNexus(searchParam: SearchParam, project: Project): MutableList<ArtifactInfo> {
        val result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&cn=${searchParam.id}"
        val connection = getConnection(url)
        connection.setRequestProperty("Accept", "application/json")
        val stream = getResponse(connection, project) ?: return result
        val jsonResult = (JsonSlurper().parse(stream) as Map<*, *>)["data"] as List<Map<*, *>>
        jsonResult.forEach {
            val artifactInfo = ArtifactInfo(it["groupId"] as String, it["artifactId"] as String, it["latestRelease"] as String, "mavenCentral", "Apache")
            if (!result.any { it.id == artifactInfo.id }) {
                result.add(artifactInfo)
            }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchInNexus(searchParam: SearchParam, project: Project): MutableList<ArtifactInfo> {
        val result: MutableList<ArtifactInfo> = mutableListOf()
        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&${searchParam.nq}"
        val connection = getConnection(url)
        connection.setRequestProperty("Accept", "application/json")
        val stream = getResponse(connection, project) ?: return result
        val jsonResult = (JsonSlurper().parse(stream) as Map<*, *>)["data"] as List<Map<*, *>>
        jsonResult.forEach {
            val artifactInfo = ArtifactInfo(it["groupId"] as String, it["artifactId"] as String, it["version"] as String, "mavenCentral", "Apache")
            result.add(artifactInfo)
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

