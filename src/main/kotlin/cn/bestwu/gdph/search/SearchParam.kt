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
    val src: String
}

class ClassNameSearchParam(override val src: String) : ISearchParam {
    override val docUrl: String
    val k: String
    val q: String

    init {
        when {
            src.startsWith("c:", true) -> {
                k = "c"
                q = src.substringAfter("c:").trim()
                docUrl = "<a href='http://search.maven.org/#search|gav|1|c:$quot$q$quot'>search in mavenCentral</a>"
            }
            src.startsWith("fc:", true) -> {
                k = "fc"
                q = src.substringAfter("fc:").trim()
                docUrl = "<a href='http://search.maven.org/#search|gav|1|fc:$quot$q$quot'>search in mavenCentral</a>"
            }
            else -> {
                k = ""
                q = ""
                docUrl = ""
            }
        }
    }

}

class SearchParam(groupIdParam: String, artifactIdParam: String, val fg: Boolean, val fa: Boolean, src: String = "") : ISearchParam {
    override val src: String
    override val docUrl: String
    val groupId: String = groupIdParam.trim()
    val artifactId: String = artifactIdParam.trim()


    private fun fullQuery(fullname: Boolean, name: String) = if (fullname) name else "*$name*"
    private fun halfQuery(fullname: Boolean, name: String) = if (fullname) name else "$name*"

    fun toId() = if (groupId.isEmpty()) src else "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"

    fun toQ() = if (groupId.isEmpty()) "q=*$src*" else "g=${fullQuery(fg, groupId)}${if (artifactId.isEmpty()) "" else "&a=${fullQuery(fa, artifactId)}"}"

    fun toMq() = if (groupId.isEmpty()) "a:$quot$src$quot" else "g:$quot$groupId$quot${if (artifactId.isEmpty()) "" else "+AND+a:$quot$artifactId$quot"}"

    fun toNq() = if (groupId.isEmpty()) "q=$src" else "g=${halfQuery(fg, groupId)}${if (artifactId.isEmpty()) "" else "&a=${halfQuery(fa, artifactId)}"}"

    override fun toString(): String {
        return "SearchParam(fg=$fg, fa=$fa, src='$src', docUrl='$docUrl', groupId='$groupId', artifactId='$artifactId')"
    }

    init {
        this.src = if (src.isEmpty()) "$groupId${if (fg) ":" else ""}${if (artifactId.isEmpty()) "" else artifactId}${if (fa) ":" else ""}" else src.trim()
        docUrl = "<a href='https://bintray.com/search?query=${toId()}'>search in jcenter</a>"
    }


}

fun toSearchParam(src: String): SearchParam {
    val list = split(src)
    return when {
        list.size in (2..3) -> {
            val groupId = list[0]
            val artifactId = list[1]
            val fa = src.count { it == ':' } > 1 && artifactId.isNotEmpty()
            SearchParam(groupId, artifactId, true, fa)
        }
        src.contains(":") -> SearchParam(src, "", fg = true, fa = false)
        else -> SearchParam("", "", fg = false, fa = false, src = src)
    }
}
