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

import cn.bestwu.gdph.config.ProjectSettings
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
import org.jetbrains.idea.maven.indices.MavenIndex
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.model.MavenRemoteRepository
import org.jetbrains.plugins.gradle.integrations.maven.MavenRepositoriesHolder
import org.jetbrains.plugins.gradle.util.GradleConstants
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

        fun performTask(project: Project) {
            if (project.isDisposed) return
            if (ApplicationManager.getApplication().isUnitTestMode) return
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            val settings = Settings.getInstance()
            val projectSettings = ProjectSettings.getInstance(project)
            val remoteRepositories: MutableSet<MavenRemoteRepository>
            if (settings.useMavenIndex) {
                remoteRepositories = projectSettings.remoteRepositories.split(ProjectSettings.separator).map { it.toMavenRemoteRepository() }.toMutableSet()
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