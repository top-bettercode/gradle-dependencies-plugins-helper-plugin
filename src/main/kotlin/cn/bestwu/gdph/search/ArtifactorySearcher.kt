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

import cn.bestwu.gdph.config.Settings
import com.intellij.openapi.project.Project
import java.net.HttpURLConnection
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
@Suppress("UNCHECKED_CAST")
object ArtifactorySearcher : AbstractArtifactSearcher() {

    override val cache: Boolean
        get() = true
    override val key: String
        get() = "artifactory:"

    override fun doSearch(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        val settings = Settings.getInstance()
        val result = TreeSet<ArtifactInfo>()
        if (searchParam.fa && searchParam.fg) {
            val url = "${settings.artifactoryUrl}/api/search/versions?g=${searchParam.groupId}&a=${searchParam.artifactId}&repos=${settings.repos}"
            val connection = httpURLConnection(url)
            var jsonResult = getResponseJson(connection, project)
            if (jsonResult != null) {
                jsonResult = (jsonResult as Map<*, *>)["results"] as List<Map<*, *>>
                jsonResult.forEach {
                    val version = it["version"] as String
                    result.add(ArtifactInfo(searchParam.groupId, searchParam.artifactId, version, "artifactory", "", false, ""))
                }
                return result
            }
        }
        val url = "${settings.artifactoryUrl}${searchParam.toAQ()}&repos=${settings.repos}"
        val connection = httpURLConnection(url)
        var jsonResult = getResponseJson(connection, project) ?: return result
        jsonResult = (jsonResult as Map<*, *>)["results"] as List<Map<*, *>>
        jsonResult.forEach {
            val artifactInfo = (it["uri"] as String).toArtifactInfo("")
            result.add(artifactInfo)
        }

        return result
    }

    override fun handleEmptyResult(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        return if (Settings.getInstance().useMavenCentral) {
            MavenCentralSearcher.search(searchParam, project)
        } else {
            JcenterSearcher.search(searchParam, project)
        }
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        val settings = Settings.getInstance()
        val url = "${settings.artifactoryUrl}/api/search/archive?name=${searchParam.q.substringAfterLast(".")}.class&repos=${settings.repos}"
        val connection = httpURLConnection(url)
        var jsonResult = getResponseJson(connection, project) ?: return emptySet()
        jsonResult = (jsonResult as Map<*, *>)["results"] as List<Map<*, *>>
        val result = TreeSet<ArtifactInfo>()
        jsonResult.forEach {
            val className = it["entry"] as String
            (it["archiveUris"] as List<String>).forEach { uri ->
                val artifactInfo = uri.toArtifactInfo(className)
                val exist = result.find { r -> r.id == artifactInfo.id }
                if (exist != null) {
                    if (compareVersion(exist.version, artifactInfo.version) < 0) {
                        exist.version = artifactInfo.version
                    }
                } else {
                    result.add(artifactInfo)
                }
            }
        }
        return result
    }

    private fun httpURLConnection(url: String): HttpURLConnection {
        val settings = Settings.getInstance()
        return if (settings.artifactoryUsername.isNotBlank() && settings.artifactoryPassword.isNotBlank()) {
            getConnection(url, mapOf("Authorization" to listOf("Basic " + Base64.getEncoder().encodeToString("${settings.artifactoryUsername}:${settings.artifactoryPassword}".toByteArray()))))
        } else
            getConnection(url)
    }

    private fun String.toArtifactInfo(className: String): ArtifactInfo {
        ///artifactory/api/storage/third-party-releases-local/org/apache/jackrabbit/jackrabbit-core/1.2.3/jackrabbit-core-1.2.3.jar
        var artifactString = this.substringAfter("/api/storage/")
        val repo = artifactString.substringBefore("/")
        artifactString = artifactString.substringAfter("/").substringBeforeLast("/")
        val version = artifactString.substringAfterLast("/")
        artifactString = artifactString.substringBeforeLast("/")
        val groupId = artifactString.substringBeforeLast("/").replace("/", ".")
        val artifactId = artifactString.substringAfterLast("/")
        val owner = "artifactory"
        return ArtifactInfo(groupId, artifactId, version, "$repo${if (owner.isNotBlank() && !(repo == "jcenter" && owner == "bintray")) " by $owner" else ""}", "${Settings.getInstance().nexusSearchUrl}/$repo", true, className)
    }

    override fun handleEmptyResultByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        return MavenCentralSearcher.searchByClassName(searchParam, project)
    }
}