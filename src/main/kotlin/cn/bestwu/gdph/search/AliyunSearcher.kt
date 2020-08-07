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
import cn.bestwu.gdph.config.Settings
import com.intellij.openapi.project.Project
import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 *
 * @author Peter Wu
 * @since
 */
object AliyunSearcher : AbstractArtifactSearcher() {

    override val cache: Boolean
        get() = true
    override val key: String
        get() = "aliyun.${Settings.getInstance().aliRepo}:"

    override fun doSearch(searchParam: SearchParam, project: Project): Collection<ArtifactInfo> {
        val url = "https://maven.aliyun.com${searchParam.toAliQ()}&repoId=${Settings.getInstance().aliRepo}"
        val connection = getConnection(url)
        var jsonResult = getResponseJson(connection, project) ?: return emptySet()
        val result = TreeSet<ArtifactInfo>()
        jsonResult = ((jsonResult as Map<*, *>)["object"] as? List<Map<*, *>>) ?: return emptySet()
        jsonResult.forEach {
            val groupId = it["groupId"] as String
            val onlyGroup = searchParam.artifactId.isBlank() && !searchParam.fg && searchParam.groupId.isNotBlank()
            val artifactId = if (onlyGroup) "" else it["artifactId"] as String
            val version = if (onlyGroup) "" else it["version"] as String
            val repositoryId = it["repositoryId"] as String
            val artifactInfo = ArtifactInfo(groupId, artifactId, version, "$repositoryId by aliyun", "", false, "")
            val id = artifactInfo.id + version
            if (!artifactInfo.groupId.startsWith(".") && !artifactInfo.groupId.startsWith("#") && !artifactInfo.groupId.startsWith("%") && !id.contains("#") && !id.contains("%")) {
                if (searchParam.fa && searchParam.fg)
                    result.add(artifactInfo)
                else
                    result.addArtifactInfo(artifactInfo)
            }
        }
        return result
    }

    override fun handleEmptyResult(searchParam: SearchParam, project: Project): Collection<ArtifactInfo> {
        val settings = Settings.getInstance()
        return when {
            settings.useNexus -> NexusSearcher.search(searchParam, project)
            settings.useArtifactory -> ArtifactorySearcher.search(searchParam, project)
            settings.useMavenCentral -> MavenCentralSearcher.search(searchParam, project)
            else -> JcenterSearcher.search(searchParam, project)
        }
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project): Collection<ArtifactInfo> {
        return emptySet()
    }

}