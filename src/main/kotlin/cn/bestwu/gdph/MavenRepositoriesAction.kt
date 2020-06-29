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

package cn.bestwu.gdph

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.util.Pair
import com.intellij.util.containers.ContainerUtil
import gnu.trove.THashSet
import org.jetbrains.idea.maven.indices.MavenIndicesManager
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager
import org.jetbrains.idea.maven.indices.MavenRepositoriesConfigurable
import org.jetbrains.idea.maven.indices.MavenRepositoryProvider
import org.jetbrains.idea.maven.model.MavenRemoteRepository
import org.jetbrains.idea.maven.project.MavenProjectsManager
import java.io.File
import java.util.*

/**
 *
 * @author Peter Wu
 * @since 0.1.3
 */
class ShowMavenRepositoriesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, MavenRepositoriesConfigurable::class.java)
    }
}

class UpdateAllMavenRepositoriesIndexAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        MavenProjectIndicesManager.getInstance(e.project).scheduleUpdateAll()
    }
}

class UpdateMavenRepositoriesIndexActionGroup : ActionGroup() {

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return if (e != null) {
            val actions = mutableListOf<AnAction>()
            try {
                MavenProjectIndicesManager.getInstance(e.project).indices.forEach {
                    actions.add(object : AnAction("Update ${it.repositoryPathOrUrl}") {
                        override fun actionPerformed(e: AnActionEvent) {
                            MavenProjectIndicesManager.getInstance(e.project).scheduleUpdate(listOf(it))
                        }
                    })
                }
                actions.toTypedArray()
            } catch (e: Exception) {
                emptyArray<AnAction>()
            }
        } else {
            arrayOf()
        }

    }

    override fun hideIfNoVisibleChildren(): Boolean {
        return true
    }

}