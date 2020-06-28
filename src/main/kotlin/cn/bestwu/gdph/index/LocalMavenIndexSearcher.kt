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

package cn.bestwu.gdph.index

import cn.bestwu.gdph.config.Settings
import cn.bestwu.gdph.search.*
import com.intellij.openapi.project.Project
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.BooleanQuery
import org.apache.maven.index.Indexer
import org.apache.maven.index.IteratorSearchRequest
import org.apache.maven.index.MAVEN
import org.apache.maven.index.context.IndexCreator
import org.apache.maven.index.context.IndexingContext
import org.apache.maven.index.expr.SourcedSearchExpression
import org.apache.maven.index.expr.UserInputSearchExpression
import org.codehaus.plexus.DefaultContainerConfiguration
import org.codehaus.plexus.DefaultPlexusContainer
import org.codehaus.plexus.PlexusConstants
import org.codehaus.plexus.PlexusContainer
import org.codehaus.plexus.classworlds.ClassWorld
import java.io.File
import java.util.*

/**
 * Collection of some use cases.
 */
object LocalMavenIndexSearcher : ArtifactSearcher() {
    private val plexusContainer: PlexusContainer
    private val indexer: Indexer
    private val indexers: MutableList<IndexCreator> = mutableListOf()
    private var indexingContexts: List<IndexingContext> = mutableListOf()

    override val cache: Boolean
        get() = false
    override val key: String
        get() = "localIndex:"

    init {
        val config = DefaultContainerConfiguration()
        config.classPathScanning = PlexusConstants.SCANNING_INDEX
        config.classWorld = ClassWorld("plexus.core", LocalMavenIndexSearcher::class.java.classLoader)
        this.plexusContainer = DefaultPlexusContainer(config)
        this.indexer = plexusContainer.lookup(Indexer::class.java)
        indexers.add(plexusContainer.lookup(IndexCreator::class.java, "min"))
        indexers.add(plexusContainer.lookup(IndexCreator::class.java, "jarContent"))
        indexers.add(plexusContainer.lookup(IndexCreator::class.java, "maven-plugin"))
        val settings = Settings.getInstance()
        val indexParentDir = settings.indexParentDir
        if (settings.useLocalMavenIndex && indexParentDir.isNotBlank()) {
            configIndexingContext(File(indexParentDir))
        }
    }

    fun configIndexingContext(indexParentDir: File) {
        indexingContexts = indexParentDir.listFiles()!!.filter { it.isDirectory }.map {
            indexer.createIndexingContext(it.name, it.name, null, it, null, null, true, true, indexers)
        }
    }

    private fun String.toMavenIndexParam() = replace("[*-]".toRegex(), " ")
    private fun artifactInfo(groupId: String, artifactId: String = "", version: String = "", repoId: String): ArtifactInfo = ArtifactInfo(groupId, artifactId, version, "${repoId.capitalize()} Index")

    override fun doSearch(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        val builder = BooleanQuery.Builder()

        if (searchParam.groupId.isNotEmpty()) {
            val gidq = indexer.constructQuery(MAVEN.GROUP_ID, SourcedSearchExpression(searchParam.groupId))
            if (searchParam.fg)
                builder.add(gidq, Occur.MUST)
            else
                builder.add(gidq, Occur.SHOULD)

            if (searchParam.artifactId.isNotEmpty()) {
                val aidq = indexer.constructQuery(MAVEN.ARTIFACT_ID, SourcedSearchExpression(searchParam.artifactId))
                if (searchParam.fa)
                    builder.add(aidq, Occur.MUST)
                else
                    builder.add(aidq, Occur.SHOULD)
            }
        } else {
            val param = searchParam.toId().toMavenIndexParam()
            val gidq = indexer.constructQuery(MAVEN.GROUP_ID, SourcedSearchExpression(param))
            val aidq = indexer.constructQuery(MAVEN.ARTIFACT_ID, SourcedSearchExpression(param))
            builder.add(gidq, Occur.SHOULD)
            builder.add(aidq, Occur.SHOULD)
        }

        val request = IteratorSearchRequest(builder.build(), indexingContexts, null)
        val response = indexer.searchIterator(request)
        response.results.sortedWith(kotlin.Comparator { o1, o2 ->
            val g = o1.groupId.compareTo(o2.groupId)
            if (g == 0) {
                val a = o1.artifactId.compareTo(o2.artifactId)
                if (a == 0)
                    compareVersion(o2.version, o1.version)
                else
                    a
            } else
                g
        }).mapTo(result) { artifactInfo(it.groupId, it.artifactId, it.version, it.context) }
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
        val response = indexer.searchIterator(IteratorSearchRequest(indexer.constructQuery(MAVEN.CLASSNAMES,
                UserInputSearchExpression(searchParam.q.toMavenIndexParam())), indexingContexts, null))
        response.results
                .map { artifactInfo(it.groupId, it.artifactId, it.version, it.context) }
                .forEach { artifactInfo ->
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


    fun close() {
        indexingContexts.forEach {
            indexer.closeIndexingContext(it, false)
        }
    }
}
