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

import cn.bestwu.gdph.quot
import cn.bestwu.gdph.split

/**
 *
 * @author Peter Wu
 * @since
 */
interface ISearchParam {
    val docUrl: String
    val docText: String
    val src: String
}

class ClassNameSearchParam(override val src: String) : ISearchParam {
    override val docUrl: String
    override val docText: String
    val k: String
    val q: String

    init {
        when {
            src.startsWith("c:", true) -> {
                k = "c"
                q = src.substringAfter("c:").trim()
                docUrl = "https://search.maven.org/#search|gav|1|c:$quot$q$quot"
                docText = "search in mavenCentral"
            }

            src.startsWith("fc:", true) -> {
                k = "fc"
                q = src.substringAfter("fc:").trim()
                docUrl = "https://search.maven.org/#search|gav|1|fc:$quot$q$quot"
                docText = "search in mavenCentral"
            }

            else -> {
                k = ""
                q = ""
                docUrl = ""
                docText = ""
            }
        }
    }

    override fun toString(): String {
        return "ClassNameSearchParam(src='$src', docUrl='$docUrl', k='$k', q='$q')"
    }


}

class SearchParam(val groupId: String, val artifactId: String, val fg: Boolean, val fa: Boolean, src: String = "") : ISearchParam {
    override val src: String = if (src.isBlank()) "$groupId${if (fg) ":" else ""}${artifactId.ifBlank { "" }}${if (fa) ":" else ""}" else src.trim()
    override val docUrl: String
    override val docText: String

    private fun fullQuery(fullname: Boolean, name: String) = if (fullname) name else "*$name*"
    private fun halfQuery(fullname: Boolean, name: String) = if (fullname) name else "$name*"

    fun toId() = if (groupId.isBlank()) src else "$groupId${if (artifactId.isBlank()) "" else ":$artifactId"}"

    /**
     * jcenter params
     */
    fun toQ() = if (groupId.isBlank()) "q=*$src*" else "g=${fullQuery(fg, groupId)}${if (artifactId.isBlank()) "" else "&a=${fullQuery(fa, artifactId)}"}"

    /**
     * artifactory params
     */
    fun toAQ() = if (groupId.isBlank()) "/api/search/artifact?name=$src" else "/api/search/gavc?g=${fullQuery(fg, groupId)}${if (artifactId.isBlank()) "" else "&a=${fullQuery(fa, artifactId)}"}"

    /**
     * ali params
     */
    fun toAliQ() = if (groupId.isBlank()) "/artifact/aliyunMaven/searchArtifactByWords?queryTerm=$src&_input_charset=utf-8" else "/artifact/aliyunMaven/searchArtifactByGav?groupId=${fullQuery(fg, groupId)}${if (artifactId.isBlank()) "&artifactId=" else "&artifactId=${fullQuery(fa, artifactId)}"}&version=&_input_charset=utf-8"

    /**
     * maven center params
     */
    fun toMq() = if (groupId.isBlank()) src else "g:$groupId${if (fg) "" else "*"}${
        if (artifactId.isBlank()) {
            ""
        } else "+AND+a:$artifactId${if (fa) "" else "*"}"
    }"

    /**
     * nexus params
     */
    fun toNq() = if (groupId.isBlank()) "q=$src" else "g=${halfQuery(fg, groupId)}${if (artifactId.isBlank()) "" else "&a=${halfQuery(fa, artifactId)}"}"

    override fun toString(): String {
        return "SearchParam(groupId='$groupId', artifactId='$artifactId', fg=$fg, fa=$fa, src='$src', docUrl='$docUrl')"
    }

    init {
        docUrl = "https://search.maven.org/solrsearch/select?q=${toMq()}"
        docText = "search in mavenCentral"
    }
}

fun toSearchParam(src: String): SearchParam {
    val list = split(src)
    return when {
        list.size in (2..3) -> {
            val groupId = list[0].trim()
            val artifactId = list[1].trim()
            val fa = src.count { it == ':' } > 1 && artifactId.isNotBlank()
            SearchParam(groupId, artifactId, groupId.isNotBlank(), fa)
        }

        src.contains(":") -> SearchParam(src.trim(), "", fg = true, fa = false)
        else -> SearchParam("", "", fg = false, fa = false, src = src.trim())
    }
}
