package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.config.Settings
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
        fun performTask(project: Project) {
            if (project.isDisposed) return
            if (ApplicationManager.getApplication().isUnitTestMode) return
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            val settings = Settings.getInstance(project)
            val remoteRepositories: MutableSet<String>
            if (settings.useMavenIndex) {
                remoteRepositories = settings.remoteRepositories
                if (settings.originRemoteRepositories == null) {
                    val mutableSet = repositoriesHolder.remoteRepositories.map { it.url }.toMutableSet()
                    settings.originRemoteRepositories = mutableSet
                    remoteRepositories.addAll(mutableSet)
                }
            } else {
                remoteRepositories = settings.originRemoteRepositories?.toMutableSet() ?: mutableSetOf()
            }
            repositoriesHolder.update(remoteRepositories.map { it.toMavenRemoteRepository() }.toMutableSet())
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
                repositoriesHolder.updateNotIndexedUrls(repositoriesWithEmptyIndex)
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
        if (ApplicationManager.getApplication().isUnitTestMode || !Settings.getInstance(project).useMavenIndex) return
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
    }
}

class GradleMavenProjectImportNotificationListener : ExternalSystemTaskNotificationListenerAdapter() {

    override fun onSuccess(id: ExternalSystemTaskId) {
        if (GradleConstants.SYSTEM_ID.id == id.projectSystemId.id && id.type == ExternalSystemTaskType.RESOLVE_PROJECT) {
            val project = id.findProject() ?: return
            if (Settings.getInstance(project).useMavenIndex)
                ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
        }
    }
}