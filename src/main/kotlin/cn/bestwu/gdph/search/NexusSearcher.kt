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
import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 *
 * @author Peter Wu
 * @since
 */
object NexusSearcher : AbstractArtifactSearcher() {

    override val cache: Boolean
        get() = false
    override val key: String
        get() = "nexus:"

    private fun artifactInfo(groupId: String, artifactId: String, version: String = "", repo: String, className: String = ""): ArtifactInfo {
        val owner = Settings.getInstance().nexusSearchUrl.replace("^.*?\\.(.*?)\\..*$".toRegex(), "$1 nexus").trim()
        return ArtifactInfo(groupId, artifactId, version, "$repo${if (owner.isNotBlank() && !(repo == "jcenter" && owner == "bintray")) " by $owner" else ""}", "${Settings.getInstance().nexusSearchUrl}/$repo", true, className)
    }

    override fun doSearch(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        val nexusSearchUrl = Settings.getInstance().nexusSearchUrl
        val url = "$nexusSearchUrl/service/local/lucene/search?${searchParam.toNq()}"
        val connection = getConnection(url)
        connection.setRequestProperty("Accept", "application/json")
        var jsonResult = getResponseJson(connection, project) ?: return emptySet()
        jsonResult = (jsonResult as Map<*, *>)["data"] as List<Map<*, *>>
        if (searchParam.fg)
            jsonResult = jsonResult.filter { it["groupId"] == searchParam.groupId }
        val result = TreeSet<ArtifactInfo>()
        jsonResult.forEach {
            val repo = if (it["latestReleaseRepositoryId"] != null) {
                it["latestReleaseRepositoryId"]
            } else {
                it["latestSnapshotRepositoryId"]
            }
            val artifactInfo = artifactInfo(it["groupId"] as String, if (searchParam.artifactId.isBlank() && !searchParam.fg && searchParam.groupId.isNotBlank() && searchParam.groupId != it["groupId"]) "" else it["artifactId"] as String, "", repo as String)
            if (artifactInfo.id == searchParam.toId() && artifactInfo.artifactId.isNotBlank()) {
                artifactInfo.version = it["version"] as String
                result.add(artifactInfo)
            } else if (!searchParam.fa) {
                result.add(artifactInfo)
            }
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
        val nexusSearchUrl = Settings.getInstance().nexusSearchUrl
        val url = "$nexusSearchUrl/service/local/lucene/search?cn=${searchParam.q}"
        val connection = getConnection(url)
        connection.setRequestProperty("Accept", "application/json")
        var jsonResult = getResponseJson(connection, project) ?: return emptySet()
        jsonResult = (jsonResult as Map<*, *>)["data"] as List<Map<*, *>>
        val result = TreeSet<ArtifactInfo>()
        jsonResult.mapTo(result) {
            val repo = if (it["latestReleaseRepositoryId"] != null) {
                it["latestReleaseRepositoryId"]
            } else {
                it["latestSnapshotRepositoryId"]
            }
            artifactInfo(it["groupId"] as String, it["artifactId"] as String, (if (it["latestRelease"] == null) it["version"] else it["latestRelease"]) as String, repo as String)
        }
        return result
    }

    override fun handleEmptyResultByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        return MavenCentralSearcher.searchByClassName(searchParam, project)
    }

}