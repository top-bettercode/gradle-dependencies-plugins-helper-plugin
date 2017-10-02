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

package cn.bestwu.gdph.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@com.intellij.openapi.components.State(
        name = "GDPHProjectSettings",
        storages = arrayOf(Storage("gdph.xml"))
)
class ProjectSettings(var remoteRepositories: String = ProjectSettings.remoteRepositories) : PersistentStateComponent<ProjectSettings> {
    override fun loadState(state: ProjectSettings?) {
        this.remoteRepositories = state?.remoteRepositories ?: ProjectSettings.remoteRepositories
    }

    override fun getState(): ProjectSettings? {
        return this
    }

    companion object {
        val separator=";"
        val remoteRepositories = "https://repo1.maven.org/maven2/"

        fun getInstance(project: Project): ProjectSettings {
            return ServiceManager.getService(project, ProjectSettings::class.java)
        }
    }


}