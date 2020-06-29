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
abstract class ArtifactSearcher {
    companion object {
        internal val artifactsCaches = HashMap<String, LinkedHashSet<ArtifactInfo>>()
        internal const val jcenterKey = "jcenter:"
    }

    abstract val cache: Boolean
    abstract val key: String
    protected abstract fun doSearch(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo>
    protected abstract fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo>

    protected open fun handleEmptyResult(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return result
    }

    protected open fun handleEmptyResultByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return result
    }

    fun search(searchParam: SearchParam, project: Project, preResult: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        val cacheKey = "$key${searchParam.src}"
        if (cache) {
            val existResult = artifactsCaches[cacheKey]
            if (existResult != null) {
                return existResult
            }
        }
        val result = doSearch(searchParam, project, preResult)
        if (key != jcenterKey)
            result.removeAll { !it.groupId.contains(searchParam.groupId.filter { g -> g != '*' }) || !it.artifactId.contains(searchParam.artifactId.filter { a -> a != '*' }) }
        return if (result.isEmpty()) {
            handleEmptyResult(searchParam, project, result)
        } else {
            if (cache)
                artifactsCaches[cacheKey] = result
            result
        }
    }

    fun searchByClassName(searchParam: ClassNameSearchParam, project: Project, preResult: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        val cacheKey = "$key$searchParam"
        val existResult = artifactsCaches[cacheKey]
        if (existResult != null) {
            return existResult
        }
        val result = doSearchByClassName(searchParam, project, preResult)
        return if (result.isEmpty()) {
            handleEmptyResultByClassName(searchParam, project, result)
        } else {
            artifactsCaches[cacheKey] = result
            result
        }
    }
}