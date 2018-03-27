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

package cn.bestwu.gdf

import cn.bestwu.gdf.gradle.GradleDependenciesSerializer
import cn.bestwu.gdf.maven.*
import com.google.common.collect.Lists

class MavenToGradleConverter(private val mavenDependencyParser: MavenDependenciesDeserializer, private val gradleDependencySerializer: GradleDependenciesSerializer, private val mavenToGradleMapper: MavenToGradleMapper) {

    fun convert(mavenDependencyXml: String): String {
        val mavenDependencies: List<MavenDependency>
        try {
            mavenDependencies = mavenDependencyParser.deserialize(mavenDependencyXml)
            if (mavenDependencies.isEmpty()) {
                return mavenDependencyXml
            }
        } catch (e: UnsupportedContentException) {
            return mavenDependencyXml
        } catch (e: DependencyValidationException) {
            return mavenDependencyXml
        }

        val dependencies = Lists
                .transform(mavenDependencies) { mavenDependency -> mavenToGradleMapper.map(mavenDependency!!) }
        return gradleDependencySerializer.serialize(dependencies)
    }

}
