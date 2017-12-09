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

import cn.bestwu.gdph.checkNotIndexedRepositories
import cn.bestwu.gdph.config.Settings
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.indices.MavenArtifactSearcher
import org.jetbrains.idea.maven.indices.MavenClassSearcher
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
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
        val m = MavenProjectIndicesManager.getInstance(project)
        if (searchParam.groupId.isNotEmpty()) {
            if (searchParam.artifactId.isEmpty() && !searchParam.fg)
                m.groupIds.forEach {
                    if (it == searchParam.groupId) {
                        m.getArtifactIds(searchParam.groupId).mapTo(result) { artifactInfo(searchParam.groupId, it) }
                    } else {
                        result.add(artifactInfo(it))
                    }
                }
            else {
                m.getArtifactIds(searchParam.groupId).forEach {
                    if (it == searchParam.artifactId) {
                        m.getVersions(searchParam.groupId, it).sortedWith(kotlin.Comparator<String> { o1, o2 ->
                            compareVersion(o2, o1)
                        }).forEach { version ->
                            result.add(artifactInfo(searchParam.groupId, it, version))
                        }
                    } else if (!searchParam.fa) {
                        result.add(artifactInfo(searchParam.groupId, it))
                    }
                }
            }
        } else {
            val searcher = MavenArtifactSearcher()
            val searchResults = searcher.search(project, searchParam.toId().toMavenIndexParam(), 1000)
            searchResults.flatMapTo(result) {
                it.versions.map { artifactInfo(it.groupId, it.artifactId) }
            }
        }

        return result
    }

    override fun handleEmptyResult(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return if (Settings.getInstance().useNexus)
            NexusSearcher.search(searchParam, project, result)
        else
            JcenterSearcher.search(searchParam, project, result)
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        checkNotIndexedRepositories(MavenRepositoriesHolder.getInstance(project))
        val searcher = MavenClassSearcher()
        val searchResults = searcher.search(project, searchParam.q.toMavenIndexParam(), 1000)
        searchResults.filter { it.versions.isNotEmpty() }
                .flatMap {
                    it.versions.map { artifactInfo(it.groupId, it.artifactId, it.version) }
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