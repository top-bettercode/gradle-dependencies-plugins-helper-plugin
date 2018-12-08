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

import cn.bestwu.gdph.config.Settings
import cn.bestwu.gdph.maven.MavenIndexSearcher
import com.intellij.openapi.project.Project
import java.util.*

object GradleArtifactSearcher {

    fun searchByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        val preResult: LinkedHashSet<ArtifactInfo> = linkedSetOf()
        val settings = Settings.getInstance()
        return when {
            settings.useMavenIndex -> MavenIndexSearcher.searchByClassName(searchParam, project, preResult)
            settings.useNexus -> NexusSearcher.searchByClassName(searchParam, project, preResult)
            else -> MavenCentralSearcher.searchByClassName(searchParam, project, preResult)
        }
    }

    fun search(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        val preResult: LinkedHashSet<ArtifactInfo> = linkedSetOf()
        val settings = Settings.getInstance()
        return when {
            settings.useMavenIndex -> MavenIndexSearcher.search(searchParam, project, preResult)
            settings.useNexus -> NexusSearcher.search(searchParam, project, preResult)
            settings.useMavenCentral -> MavenCentralSearcher.search(searchParam, project, preResult)
            else -> JcenterSearcher.search(searchParam, project, preResult)
        }
    }


}

