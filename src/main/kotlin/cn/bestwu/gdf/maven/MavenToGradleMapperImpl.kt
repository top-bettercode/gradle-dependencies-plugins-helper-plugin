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

package cn.bestwu.gdf.maven

import cn.bestwu.gdf.gradle.Dependency
import cn.bestwu.gdf.gradle.Exclusion
import com.google.common.base.Optional
import java.util.*

class MavenToGradleMapperImpl : MavenToGradleMapper {

    override fun map(mavenDependency: MavenDependency): Dependency {
        val excludes = mavenDependency.exclusions.map { Exclusion(it.groupId!!, it.artifactId!!) }
        val hasWildcardExclude = mavenDependency.exclusions.toMutableList().removeIf { it.groupId == ASTERISK && it.artifactId == ASTERISK }
        val transitive = !hasWildcardExclude
        val extraOptions = createExtraOptions(mavenDependency)
        val optional = isOptional(mavenDependency)
        return Dependency(mavenDependency.groupId!!, mavenDependency.artifactId!!, mavenDependency.version,
                Optional.fromNullable(mavenDependency.classifier), getScope(mavenDependency.scope), excludes, transitive,
                extraOptions, optional)
    }

    private fun createExtraOptions(mavenDependency: MavenDependency): HashMap<String, String> {
        val extraOptions = HashMap<String, String>()
        if (mavenDependency.systemPath != null) {
            extraOptions[SYSTEM_PATH] = mavenDependency.systemPath ?: ""
        }
        if (mavenDependency.type != null) {
            extraOptions[TYPE] = mavenDependency.type ?: ""
        }
        return extraOptions
    }

    private fun isOptional(mavenDependency: MavenDependency): Boolean {
        return isEqualTo(mavenDependency.optional, TRUE)
    }

    private fun isEqualTo(string1: String?, string2: String): Boolean {
        return string1 != null && string1.compareTo(string2) == 0
    }

    private fun getScope(scope: Scope): String {
        return if (scope == Scope.TEST) {
            "testCompile"
        } else {
            scope.toString().toLowerCase()
        }
    }

    companion object {

        private const val ASTERISK = "*"
        private const val SYSTEM_PATH = "systemPath"
        private const val TYPE = "type"
        private const val TRUE = "true"
    }
}
