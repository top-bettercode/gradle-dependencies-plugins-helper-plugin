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

/**
 *
 * @author Peter Wu
 * @since
 */
class ArtifactInfo(groupId: String, artifactId: String, version: String, repo: String, owner: String = "") {
    val groupId: String = groupId.trim()
    val artifactId: String = artifactId.trim()
    var version: String = version.trim()
        set(value) {
            field = value
            this.gav = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
        }
    private val repo: String = repo.trim()
    private val owner: String = owner.trim()
    val id: String
    var gav: String


    fun type() = "$repo${if (owner.isNotEmpty() && !(repo == "jcenter" && owner == "bintray")) " by $owner" else ""}"
    fun repo() = "https://dl.bintray.com/$owner/$repo"
    fun isSpecifiedRepo() = repo.isNotEmpty() && owner.isNotEmpty() && !(repo == "jcenter" && owner == "bintray")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArtifactInfo) return false

        if (gav != other.gav) return false

        return true
    }

    override fun hashCode(): Int {
        return gav.hashCode()
    }

    override fun toString(): String {
        return "ArtifactInfo(groupId='$groupId', artifactId='$artifactId', version='$version', repo='$repo', owner='$owner', id='$id', gav='$gav')"
    }

    init {
        this.id = "${this.groupId}${if (this.artifactId.isEmpty()) "" else ":${this.artifactId}"}"
        this.gav = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
    }


}