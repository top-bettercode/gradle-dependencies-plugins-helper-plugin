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

import cn.bestwu.gdph.supportMavenIndex
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.Storage

@com.intellij.openapi.components.State(
        name = "GDPHSettings",
        storages = [(Storage("gdph.settings.xml"))]
)
class Settings(var useNexus: Boolean = Settings.useNexus, var nexusSearchUrl: String = Settings.nexusSearchUrl, var useMavenIndex: Boolean = Settings.useMavenIndex, var useMavenCentral: Boolean = Settings.useMavenCentral) : PersistentStateComponent<Settings> {

    override fun loadState(state: Settings) {
        this.useMavenIndex = state.useMavenIndex && supportMavenIndex()
        this.useNexus = state.useNexus
        this.useMavenCentral = state.useMavenCentral
        this.nexusSearchUrl = state.nexusSearchUrl
    }

    override fun getState(): Settings? {
        return this
    }

    companion object {
        const val useNexus: Boolean = false
        const val useMavenIndex: Boolean = false
        const val useMavenCentral: Boolean = false
        const val nexusSearchUrl: String = "https://oss.sonatype.org"
        const val mavenCentralRemoteRepository = "https://repo1.maven.org/maven2/"

        fun getInstance(): Settings {
            return ServiceManager.getService(Settings::class.java)
        }
    }


}
