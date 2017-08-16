package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

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
    private val mavenCentralRemoteRepository: MavenRemoteRepository
    private var myProject: Project = project
    private var myDumbService: DumbService

    init {
        myDumbService = DumbService.getInstance(myProject)
        mavenCentralRemoteRepository = MavenRemoteRepository("central", null, "https://repo1.maven.org/maven2/", null, null, null)
    }

    @Throws(ProcessCanceledException::class)
    override fun runBackgroundProcess(indicator: ProgressIndicator): ReadTask.Continuation {
        return myDumbService.runReadActionInSmartMode<ReadTask.Continuation> {
            Continuation({
                performTask()
            })
        }
    }

    private fun performTask() {
        if (myProject.isDisposed) return
        if (ApplicationManager.getApplication().isUnitTestMode) return

//        val remoteRepository = MavenRemoteRepository("my", null, "http://127.0.0.1:8081/remote-repos/", null, null, null)
        val repositoriesHolder = MavenRepositoriesHolder.getInstance(myProject)
        val remoteRepositories = mutableSetOf<MavenRemoteRepository>()
        remoteRepositories.addAll(repositoriesHolder.remoteRepositories)
        remoteRepositories.add(mavenCentralRemoteRepository)
        repositoriesHolder.update(remoteRepositories)
        MavenProjectIndicesManager.getInstance(myProject).scheduleUpdateIndicesList(Consumer<List<MavenIndex>> { indexes ->
            if (myProject.isDisposed) return@Consumer

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

    override fun onCanceled(indicator: ProgressIndicator) {
        if (!myProject.isDisposed) {
            ProgressIndicatorUtils.scheduleWithWriteActionPriority(this)
        }
    }

}

class GradleProjectStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        if (ApplicationManager.getApplication().isUnitTestMode) return
        ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
    }
}

class GradleMavenProjectImportNotificationListener : ExternalSystemTaskNotificationListenerAdapter() {

    override fun onSuccess(id: ExternalSystemTaskId) {
        if (GradleConstants.SYSTEM_ID.id == id.projectSystemId.id && id.type == ExternalSystemTaskType.RESOLVE_PROJECT) {
            val project = id.findProject() ?: return
            ProgressIndicatorUtils.scheduleWithWriteActionPriority(ImportMavenRepositoriesTask(project))
        }
    }
}