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

import com.intellij.openapi.project.Project
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
abstract class AbstractArtifactSearcher {
    companion object {
        internal val artifactsCaches = HashMap<String, Set<ArtifactInfo>>()
        internal const val jcenterKey = "jcenter:"
    }

    abstract val cache: Boolean
    abstract val key: String
    protected abstract fun doSearch(searchParam: SearchParam, project: Project): Set<ArtifactInfo>
    protected abstract fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo>

    protected open fun handleEmptyResult(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        return emptySet()
    }

    protected open fun handleEmptyResultByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        return emptySet()
    }

    fun search(searchParam: SearchParam, project: Project): Set<ArtifactInfo> {
        val cacheKey = "$key${searchParam.src}"
        if (cache) {
            val existResult = artifactsCaches[cacheKey]
            if (existResult != null) {
                return existResult
            }
        }
        var result = doSearch(searchParam, project)
        if (key != jcenterKey)
            result = result.filter { !it.groupId.contains(searchParam.groupId.filter { g -> g != '*' }) || !it.artifactId.contains(searchParam.artifactId.filter { a -> a != '*' }) }.toSet()
        return if (result.isEmpty()) {
            handleEmptyResult(searchParam, project)
        } else {
            if (cache)
                artifactsCaches[cacheKey] = result
            result
        }
    }

    fun searchByClassName(searchParam: ClassNameSearchParam, project: Project): Set<ArtifactInfo> {
        val cacheKey = "$key$searchParam"
        val existResult = artifactsCaches[cacheKey]
        if (existResult != null) {
            return existResult
        }
        val result = doSearchByClassName(searchParam, project)
        return if (result.isEmpty()) {
            handleEmptyResultByClassName(searchParam, project)
        } else {
            artifactsCaches[cacheKey] = result
            result
        }
    }
}