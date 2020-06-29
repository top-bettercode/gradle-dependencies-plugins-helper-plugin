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

import cn.bestwu.gdph.split
import com.intellij.openapi.project.Project
import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 *
 * @author Peter Wu
 * @since
 */
object JcenterSearcher : ArtifactSearcher() {

    override val cache: Boolean
        get() = true
    override val key: String
        get() = jcenterKey

    override fun doSearch(searchParam: SearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        var cresult = result
        val url = "https://api.bintray.com/search/packages/maven?${searchParam.toQ()}"
        val connection = getConnection(url)
        var jsonResult = getResponseJson(connection, project) ?: return cresult
        val qId = searchParam.toId()
        val findById = (jsonResult as List<Map<*, *>>).filter { (it["system_ids"] as List<String>).contains(qId) }
        if (findById.isNotEmpty() && searchParam.fa) {
            jsonResult = findById
        }
        for (any in jsonResult) {
            val systemIds = any["system_ids"] as MutableList<String>
            if (systemIds.contains(qId)) {
                systemIds.clear()
                systemIds.add(qId)
            }
            for (id in systemIds) {
                val list = split(id)
                val groupId: String
                val artifactId: String
                if (list.size == 2) {
                    groupId = list[0]
                    artifactId = list[1]
                } else {
                    groupId = id
                    artifactId = ""
                }
                if (id == qId) {
                    ((any["versions"] as List<String>).mapTo(cresult) { ArtifactInfo(groupId, artifactId, it, any["repo"] as String, any["owner"] as String) })
                } else if (!searchParam.fa) {
                    cresult.add(ArtifactInfo(groupId, if (searchParam.artifactId.isEmpty() && !searchParam.fg && searchParam.groupId.isNotEmpty() && searchParam.groupId != groupId) "" else artifactId, "", any["repo"] as String, any["owner"] as String))
                }
            }
        }
        if (findById.size == 2 && findById.any { "spring" == it["owner"] } && findById.any { "bintray" == it["owner"] }) {
            cresult = cresult.sortedWith(kotlin.Comparator { o1, o2 ->
                compareVersion(o2.version, o1.version)
            }).toCollection(linkedSetOf())
        }
        return cresult
    }

    override fun doSearchByClassName(searchParam: ClassNameSearchParam, project: Project, result: LinkedHashSet<ArtifactInfo>): LinkedHashSet<ArtifactInfo> {
        return result
    }

}