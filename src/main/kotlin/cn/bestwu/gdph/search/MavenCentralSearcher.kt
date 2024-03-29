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

import cn.bestwu.gdph.addArtifactInfo
import cn.bestwu.gdph.quot
import com.intellij.openapi.project.Project
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
object MavenCentralSearcher : AbstractArtifactSearcher() {

    override val cache: Boolean
        get() = true
    override val key: String
        get() = "maven:"

    fun artifactInfo(groupId: String, artifactId: String, version: String = "", className: String = ""): ArtifactInfo = ArtifactInfo(groupId, artifactId, version, "mavenCentral", "mavenCentral()", false, className)

    override fun doSearch(searchParam: SearchParam, project: Project): Collection<ArtifactInfo> {
        val url = "https://search.maven.org/solrsearch/select?q=${searchParam.toMq()}&rows=50&core=gav&wt=json"
        val connection = getConnection(url)
        val text = getResponseText(connection, project) ?: return emptySet()
        val result = TreeSet<ArtifactInfo>()
        regex.findAll(text).forEach {
            val groupId = it.groupValues[1]
            val artifactId = it.groupValues[2]
            val version = it.groupValues[3]
            val artifactInfo = artifactInfo(groupId = groupId, artifactId = if (searchParam.groupId.isNotBlank() && searchParam.artifactId.isBlank() && !searchParam.fg && groupId != searchParam.groupId) "" else artifactId)
            if (artifactInfo.id == searchParam.toId()) {
                artifactInfo.version = version
            }
            result.add(artifactInfo)
        }
        return result
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project): Collection<ArtifactInfo> {
        val url = "https://search.maven.org/solrsearch/select?q=${searchParam.k}:$quot${searchParam.q}$quot&core=gav&rows=1000&wt=json"
        val connection = getConnection(url)
        val text = getResponseText(connection, project) ?: return emptySet()
        val result = TreeSet<ArtifactInfo>()
        regex.findAll(text).forEach {
            val groupId = it.groupValues[1]
            val artifactId = it.groupValues[2]
            val version = it.groupValues[3]
            val artifactInfo = artifactInfo(groupId = groupId, artifactId = artifactId, version = version, className = "")
            result.addArtifactInfo(artifactInfo)
        }
        return result
    }

}