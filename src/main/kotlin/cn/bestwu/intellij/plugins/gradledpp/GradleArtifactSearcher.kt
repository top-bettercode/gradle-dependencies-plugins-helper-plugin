package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.config.Settings
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import groovy.json.JsonSlurper
import org.jetbrains.idea.maven.indices.MavenArtifactSearcher
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.model.MavenArtifactInfo
import org.jetbrains.plugins.gradle.integrations.maven.MavenRepositoriesHolder
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

    fun type() = "$repo${if ("bintray" != owner && "Apache" != owner && owner.isNotBlank()) " By $owner" else ""}"

    init {
        this.id = "${this.groupId}:${this.artifactId}"
        this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
    }
}

class SearchParam {
    val groupId: String
    val artifactId: String
    val id: String
    val q: String
    val mq: String
    val nq: String
    val advancedSearch: String
    val failContent: String

    constructor(text: String) {
        if (text.startsWith("c:", true)) {
            groupId = ""
            artifactId = ""
            id = text.substringAfter("c:").trim()
            q = text.trim()
            mq = text.trim()
            nq = text.trim()
            advancedSearch = "c"
            failContent = "<a href='http://search.maven.org/#search|gav|1|c:\"$id\"'>search in mavenCentral</a>"
        } else if (text.startsWith("fc:", true)) {
            groupId = ""
            artifactId = ""
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
                groupId = list[0].trim()
                artifactId = list[1].trim()
                this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
                this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
                this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
                this.nq = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
            } else {
                groupId = ""
                artifactId = ""
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
        groupId = groupIdParam.trim()
        artifactId = artifactIdParam.trim()
        this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
        this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
        this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
        this.nq = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
        failContent = "<a href='https://bintray.com/search?query=$id'>search in jcenter</a>"
    }

    override fun toString(): String {
        return "SearchParam(groupId='$groupId', artifactId='$artifactId', id='$id', q='$q', mq='$mq', nq='$nq', advancedSearch='$advancedSearch', failContent='$failContent')"
    }


}

class GradleArtifactSearcher {
    companion object {
        private val regex = Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",")
        private val keyIndex = "index:"
        private val keyNexus = "nexus:"
        private val keyMaven = "maven:"
        private val keyBintray = "bintray:"

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

        for (i in 0 until length) {
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
        var result: List<ArtifactInfo>
        if (searchParam.advancedSearch.isNotEmpty()) {
            if (Settings.getInstance(project).useNexus) {
                result = search(keyNexus, searchParam, project, this::searchByClassNameInNexus)
                if (result.isEmpty())
                    result = search(keyMaven, searchParam, project, this::searchByClassNameInMavenCentral)
            } else {
                result = search(keyMaven, searchParam, project, this::searchByClassNameInMavenCentral)
            }
        } else {
            result = if (Settings.getInstance(project).useMavenIndex) {
                search(keyIndex, searchParam, project, this::searchInMavenIndexes)
            } else {
                if (Settings.getInstance(project).useNexus)
                    search(keyNexus, searchParam, project, this::searchInNexus)
                else
                    search(keyBintray, searchParam, project, this::searchInJcenter)
            }
        }
        return result
    }

    private fun search(repoKey: String, searchParam: SearchParam, project: Project, run: (SearchParam, Project, MutableList<ArtifactInfo>) -> MutableList<ArtifactInfo>): List<ArtifactInfo> {
        val key = "$repoKey${searchParam.q}"
        val existResult = artifactsCaches[key]
        if (existResult != null) {
            return existResult
        }
        var result: MutableList<ArtifactInfo> = mutableListOf()
        result = run(searchParam, project, result)
        artifactsCaches.put(key, result)
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchByClassNameInMavenCentral(searchParam: SearchParam, project: Project, result: MutableList<ArtifactInfo>): MutableList<ArtifactInfo> {
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
    private fun searchByClassNameInNexus(searchParam: SearchParam, project: Project, result: MutableList<ArtifactInfo>): MutableList<ArtifactInfo> {
        val url = "${Settings.getInstance(project).nexusSearchUrl}/service/local/lucene/search?repositoryId=central&cn=${searchParam.id}"
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

    private fun searchInMavenIndexes(searchParam: SearchParam, project: Project, result: MutableList<ArtifactInfo>): MutableList<ArtifactInfo> {
        val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
        try {
            repositoriesHolder::class.java.getMethod("checkNotIndexedRepositories").invoke(repositoriesHolder)
        } catch (e: NoSuchMethodException) {
        }

        val m = MavenProjectIndicesManager.getInstance(project)
        if (searchParam.groupId.isNotEmpty()) {
            if (searchParam.artifactId.isEmpty())
                m.groupIds.mapTo(result) { ArtifactInfo(it, "", "", "maven", "") }
            else {
                m.getArtifactIds(searchParam.groupId).forEach {
                    if (it == searchParam.artifactId) {
                        m.getVersions(searchParam.groupId, it).sortedWith(kotlin.Comparator<String> { o1, o2 ->
                            compareVersion(o2, o1)
                        }).forEach { version ->
                            result.add(ArtifactInfo(searchParam.groupId, it, version, "maven", ""))
                        }
                    } else {
                        result.add(ArtifactInfo(searchParam.groupId, it, "", "maven", ""))
                    }
                }
            }
        } else {
            val searcher = MavenArtifactSearcher()
            val searchResults = searcher.search(project, searchParam.id, 1000)
            searchResults
                    .flatMap {
                        it.versions.sortedWith(kotlin.Comparator<MavenArtifactInfo> { o1, o2 ->
                            compareVersion(o2.version, o1.version)
                        })
                    }
                    .mapTo(result) { ArtifactInfo(it.groupId, it.artifactId, it.version, "maven", "") }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun searchInNexus(searchParam: SearchParam, project: Project, result: MutableList<ArtifactInfo>): MutableList<ArtifactInfo> {
        val url = "${Settings.getInstance(project).nexusSearchUrl}/service/local/lucene/search?repositoryId=central&${searchParam.nq}"
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
    private fun searchInJcenter(searchParam: SearchParam, project: Project, result: MutableList<ArtifactInfo>): MutableList<ArtifactInfo> {
        var cresult = result
        val url = "https://api.bintray.com/search/packages/maven?${searchParam.q}"
        val connection = getConnection(url)
        val stream = getResponse(connection, project) ?: return cresult
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
                    ((any["versions"] as List<String>).mapTo(cresult) { ArtifactInfo(groupId, artifactId, it, any["repo"] as String, any["owner"] as String) })
                } else {
                    cresult.add(ArtifactInfo(groupId, artifactId, "", any["repo"] as String, any["owner"] as String))
                }
            }
        }
        if (findById.size > 1 && findById.any { "spring" == it["owner"] }) {
            cresult = cresult.sortedWith(kotlin.Comparator<ArtifactInfo> { o1, o2 ->
                compareVersion(o2.version, o1.version)
            }).toMutableList()
        }
        return cresult
    }


}

