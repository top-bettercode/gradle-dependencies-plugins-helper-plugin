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

import cn.bestwu.gdph.config.Settings
import com.intellij.CommonBundle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationListener
import com.intellij.notification.impl.NotificationsConfigurationImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory
import com.intellij.openapi.externalSystem.service.notification.NotificationData
import com.intellij.openapi.externalSystem.service.notification.NotificationSource
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.progress.util.ReadTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Consumer
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.idea.maven.indices.MavenIndex
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.indices.MavenRepositoriesConfigurable
import org.jetbrains.idea.maven.model.MavenRemoteRepository
import org.jetbrains.plugins.gradle.integrations.maven.MavenRepositoriesHolder
import org.jetbrains.plugins.gradle.util.GradleBundle
import org.jetbrains.plugins.gradle.util.GradleConstants
import javax.swing.event.HyperlinkEvent

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
        private val UNINDEXED_MAVEN_REPOSITORIES_NOTIFICATION_GROUP = "Unindexed maven repositories gradle detection"

        fun performTask(project: Project) {
            if (project.isDisposed) return
            if (ApplicationManager.getApplication().isUnitTestMode) return
            val repositoriesHolder = MavenRepositoriesHolder.getInstance(project)
            val settings = Settings.getInstance()
            val remoteRepositories: MutableSet<MavenRemoteRepository>
            if (settings.useMavenIndex) {
                remoteRepositories = mutableSetOf()
                remoteRepositories.add(Settings.mavenCentralRemoteRepository.toMavenRemoteRepository())
                remoteRepositories.addAll(repositoriesHolder.remoteRepositories)
            } else {
                remoteRepositories = repositoriesHolder.remoteRepositories
            }
            repositoriesHolder.update(remoteRepositories)
            MavenProjectIndicesManager.getInstance(project).scheduleUpdateIndicesList(Consumer<List<MavenIndex>> { indexes ->
                if (project.isDisposed) return@Consumer

                val repositoriesWithEmptyIndex = ContainerUtil.mapNotNull<MavenIndex, String>(indexes) { index ->
                    if (index.updateTimestamp == -1L && MavenRepositoriesHolder.getInstance(project).contains(index.repositoryPathOrUrl))
                        index.repositoryPathOrUrl
                    else
                        null
                }
                if (!repositoriesWithEmptyIndex.isEmpty()) {
                    val notificationData = NotificationData(
                            GradleBundle.message("gradle.integrations.maven.notification.not_updated_repository.title"),
                            "\n<br>" + GradleBundle.message("gradle.integrations.maven.notification.not_updated_repository.text", StringUtil.join(repositoriesWithEmptyIndex, "<br>")),
                            NotificationCategory.WARNING,
                            NotificationSource.PROJECT_SYNC)
                    notificationData.isBalloonNotification = true
                    notificationData.balloonGroup = UNINDEXED_MAVEN_REPOSITORIES_NOTIFICATION_GROUP
                    notificationData.setListener("#open", object : NotificationListener.Adapter() {
                        override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, MavenRepositoriesConfigurable::class.java)
                        }
                    })

                    notificationData.setListener("#disable", object : NotificationListener.Adapter() {
                        override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
                            val result = Messages.showYesNoDialog(project,
                                    "Notification will be disabled for all projects.\n\n" +
                                            "Settings | Appearance & Behavior | Notifications | " +
                                            UNINDEXED_MAVEN_REPOSITORIES_NOTIFICATION_GROUP +
                                            "\ncan be used to configure the notification.",
                                    "Unindexed Maven Repositories Gradle Detection",
                                    "Disable Notification", CommonBundle.getCancelButtonText(),
                                    Messages.getWarningIcon())
                            if (result == Messages.YES) {
                                NotificationsConfigurationImpl.getInstanceImpl().changeSettings(UNINDEXED_MAVEN_REPOSITORIES_NOTIFICATION_GROUP,
                                        NotificationDisplayType.NONE, false, false)

                                notification.hideBalloon()
                            }
                        }
                    })

                    ExternalSystemNotificationManager.getInstance(project).showNotification(GradleConstants.SYSTEM_ID, notificationData)
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