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

package cn.bestwu.gdph.maven

import cn.bestwu.gdph.checkNotIndexedRepositories
import cn.bestwu.gdph.config.Settings
import cn.bestwu.gdph.search.*
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.indices.MavenClassSearcher
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.plugins.gradle.integrations.maven.MavenRepositoriesHolder
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
object MavenIndexSearcher : ArtifactSearcher() {

    override val cache: Boolean
        get() = false
    override val key: String
        get() = "index:"

    private fun String.toMavenIndexParam() = replace("[*-]".toRegex(), " ")
    private fun artifactInfo(groupId: String, artifactId: String = "", version: String = ""): ArtifactInfo = ArtifactInfo(groupId, artifactId, version, "mavenIndex")

    override fun doSearch(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        checkNotIndexedRepositories(MavenRepositoriesHolder.getInstance(project))
        MavenProjectIndicesManager.getInstance(project).offlineSearchService.findGroupCandidates(MavenId(searchParam.toId())).mapTo(result) {
            artifactInfo(it.groupId ?: "", it.artifactId ?: "")
        }
        return result
    }

    override fun handleEmptyResult(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return when {
            Settings.getInstance().useNexus -> NexusSearcher.search(searchParam, project, result)
            Settings.getInstance().useMavenCentral -> MavenCentralSearcher.search(searchParam, project, result)
            else -> JcenterSearcher.search(searchParam, project, result)
        }
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        checkNotIndexedRepositories(MavenRepositoriesHolder.getInstance(project))
        val searcher = MavenClassSearcher()
        val searchResults = searcher.search(project, searchParam.q.toMavenIndexParam(), 1000)
        searchResults.filter { it.searchResults.items.isEmpty() }
                .flatMap {
                    it.searchResults.items.map { item ->
                        artifactInfo(item.groupId ?: "", item.artifactId ?: "", item.version ?: "")
                    }
                }.forEach { artifactInfo ->
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

    override fun handleEmptyResultByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return if (Settings.getInstance().useNexus)
            NexusSearcher.searchByClassName(searchParam, project, result)
        else
            MavenCentralSearcher.searchByClassName(searchParam, project, result)
    }

}