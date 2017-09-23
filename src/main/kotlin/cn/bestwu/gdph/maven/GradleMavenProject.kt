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

import cn.bestwu.gdph.ArtifactInfo
import cn.bestwu.gdph.GradleArtifactSearcher
import cn.bestwu.gdph.SearchParam
import cn.bestwu.gdph.config.Settings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.progress.util.ReadTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.Consumer
import org.jetbrains.idea.maven.indices.MavenArtifactSearcher
import org.jetbrains.idea.maven.indices.MavenClassSearcher
import org.jetbrains.idea.maven.indices.MavenIndex
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.model.MavenRemoteRepository
import org.jetbrains.plugins.gradle.integrations.maven.MavenRepositoriesHolder
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.util.*
import java.util.stream.Collectors

/**
 *
 * @author Peter Wu
 * @since
 */
class ImportMavenRepositoriesTask(project: Project) : ReadTask() {
    private var myProject: Project = project
    private var myDumbService: DumbService


    companion object {

        private fun String.toMavenRemoteRepository() = MavenRemoteRepository(this, null, this, null, null, null)

        private fun String.toMavenIndexParam() = replace("[*-]".toRegex(), " ")

        fun searchByClassNameInMavenIndex(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            try {
                repositoriesHolder::class.java.getMethod("checkNotIndexedRepositories").invoke(repositoriesHolder)
            } catch (e: NoSuchMethodException) {
            }
            val searcher = MavenClassSearcher()
            val searchResults = searcher.search(project, searchParam.id.toMavenIndexParam(), 1000)
            searchResults.filter { it.versions.isNotEmpty() }
                    .flatMap {
                        it.versions.map { ArtifactInfo(it.groupId, it.artifactId, it.version, "maven", "") }
                    }.forEach { artifactInfo ->
                val exist = result.find { it.id == artifactInfo.id }
                if (exist != null) {
                    if (GradleArtifactSearcher.compareVersion(exist.version, artifactInfo.version) < 0) {
                        exist.version = artifactInfo.version
                    }
                } else {
                    result.add(artifactInfo)
                }
            }


            return result
        }

        fun searchInMavenIndexes(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            try {
                repositoriesHolder::class.java.getMethod("checkNotIndexedRepositories").invoke(repositoriesHolder)
            } catch (e: NoSuchMethodException) {
            }

            val m = MavenProjectIndicesManager.getInstance(project)
            if (searchParam.groupId.isNotEmpty()) {
                if (searchParam.artifactId.isEmpty() && !searchParam.fg)
                    m.groupIds.forEach {
                        if (it == searchParam.groupId) {
                            m.getArtifactIds(searchParam.groupId).mapTo(result) { ArtifactInfo(searchParam.groupId, it, "", "maven", "") }
                        } else {
                            result.add(ArtifactInfo(it, "", "", "maven", ""))
                        }
                    }
                else {
                    m.getArtifactIds(searchParam.groupId).forEach {
                        if (it == searchParam.artifactId) {
                            m.getVersions(searchParam.groupId, it).sortedWith(kotlin.Comparator<String> { o1, o2 ->
                                GradleArtifactSearcher.compareVersion(o2, o1)
                            }).forEach { version ->
                                result.add(ArtifactInfo(searchParam.groupId, it, version, "maven", ""))
                            }
                        } else if (!searchParam.fa) {
                            result.add(ArtifactInfo(searchParam.groupId, it, "", "maven", ""))
                        }
                    }
                }
            } else {
                val searcher = MavenArtifactSearcher()
                val searchResults = searcher.search(project, searchParam.id.toMavenIndexParam(), 1000)
                searchResults.flatMapTo(result) {
                    it.versions.map { ArtifactInfo(it.groupId, it.artifactId, "", "maven", "") }
                }
            }

            return result
        }

        fun performTask(project: Project) {
            if (project.isDisposed) return
            if (ApplicationManager.getApplication().isUnitTestMode) return
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            val settings = Settings.getInstance()
            val remoteRepositories: MutableSet<MavenRemoteRepository>
            if (settings.useMavenIndex) {
                remoteRepositories = settings.remoteRepositories.map { it.toMavenRemoteRepository() }.toMutableSet()
                remoteRepositories.addAll(repositoriesHolder.remoteRepositories)
            } else {
                remoteRepositories = repositoriesHolder.remoteRepositories
            }
            repositoriesHolder.update(remoteRepositories)
            MavenProjectIndicesManager.getInstance(project).scheduleUpdateIndicesList(Consumer<List<MavenIndex>> { indexes ->
                if (project.isDisposed) return@Consumer

                val repositoriesWithEmptyIndex = indexes.stream()
                        .filter({ index ->
                            index.updateTimestamp == -1L &&
                                    index.failureMessage == null &&
                                    repositoriesHolder.contains(index.repositoryPathOrUrl)
                        })
                        .map(MavenIndex::getRepositoryPathOrUrl)
                        .collect(Collectors.toList<String>())
                try {
                    repositoriesHolder::class.java.getMethod("updateNotIndexedUrls").invoke(repositoriesHolder, repositoriesWithEmptyIndex)
                } catch (e: NoSuchMethodException) {
                }
            })

        }
    }

    init {
        myDumbService = DumbService.getInstance(myProject)
    }

    @Throws(ProcessCanceledException::class)
    override fun runBackgroundProcess(indicator: ProgressIndicator): ReadTask.Continuation {
        return myDumbService.runReadActionInSmartMode<ReadTask.Continuation> {
            Continuation({
                performTask(myProject)
            })
        }
    }

    override fun onCanceled(indicator: ProgressIndicator) {
        if (!myProject.isDisposed) {
            ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
        }
    }

}

class GradleProjectStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        if (!Settings.getInstance().useMavenIndex || ApplicationManager.getApplication().isUnitTestMode) return
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
    }
}

class GradleMavenProjectImportNotificationListener : ExternalSystemTaskNotificationListenerAdapter() {

    override fun onSuccess(id: ExternalSystemTaskId) {
        if (Settings.getInstance().useMavenIndex && GradleConstants.SYSTEM_ID.id == id.projectSystemId.id && id.type == ExternalSystemTaskType.RESOLVE_PROJECT) {
            val project = id.findProject() ?: return
            ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
        }
    }
}