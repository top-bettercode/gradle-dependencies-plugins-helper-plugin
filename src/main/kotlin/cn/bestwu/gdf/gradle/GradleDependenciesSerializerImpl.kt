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

package cn.bestwu.gdf.gradle

import com.google.common.base.Joiner
import kotlin.collections.Map.Entry

class GradleDependenciesSerializerImpl : GradleDependenciesSerializer {

    override fun serialize(dependencies: List<Dependency>): String {
        return NEW_LINE_JOINER.join(dependencies.map(FORMAT_GRADLE_DEPENDENCY))
    }

    companion object {
        private const val NEW_LINE = '\n'
        private val NEW_LINE_JOINER = Joiner.on(NEW_LINE)
        private val COMMA_JOINER = Joiner.on(", ")
        private val EXTRA_OPTION_FORMATTER: (Entry<String, String>) -> String = {
            String.format("%s = %s (%s is not supported)", it.key, it.value, it.key)
        }

        private val FORMAT_GRADLE_DEPENDENCY: (Dependency) -> String = { dependency ->
            var comment = ""
            if (dependency.hasExtraOptions()) {
                comment = createComment(dependency.extraOptions)
            }
            if (useClosure(dependency)) {
                if (dependency.isOptional) {
                    comment += prepareComment(comment, "optional = true (optional is not supported for dependency with closure)")
                }
                String.format("%s(%s) {%s%s%s}",
                        dependency.configuration, toStringNotation(dependency), comment, NEW_LINE, getClosureContent(dependency))
            } else {
                val optional = if (dependency.isOptional) ", optional" else ""
                String.format("%s %s%s%s", dependency.configuration, toStringNotation(dependency), optional, comment)
            }
        }

        private fun prepareComment(comment: String, text: String): String {
            return if (comment.isEmpty()) String.format(" // %s", text) else String.format(", %s", text)
        }

        private fun createComment(extraOptions: Map<String, String>): String {
            return String.format(" // %s", COMMA_JOINER.join(extraOptions.entries.map(EXTRA_OPTION_FORMATTER)))
        }

        private fun useClosure(dependency: Dependency): Boolean {
            val exclusions = dependency.exclusions
            return exclusions.isNotEmpty() || !dependency.isTransitive
        }

        private fun toStringNotation(dependency: Dependency): String {
            val quotationMark = if (dependency.version != null && dependency.version.contains("\${")) '"' else '\''
            val result = StringBuilder()
            result.append(quotationMark)
            result.append(dependency.group)
            result.append(':')
            result.append(dependency.name)
            appendIf(dependency.version, result, dependency.hasVersion())
            appendIf(dependency.getClassifier(), result, dependency.hasClassifier())
            result.append(quotationMark)
            return result.toString()
        }

        private fun getClosureContent(dependency: Dependency): String {
            val stringBuilder = StringBuilder()
            for ((group, module) in dependency.exclusions) {
                stringBuilder.append(String.format("\texclude group: '%s', module: '%s'", group, module))
                stringBuilder.append(NEW_LINE)
            }
            if (!dependency.isTransitive) {
                stringBuilder.append("\ttransitive = false")
                stringBuilder.append(NEW_LINE)
            }
            return stringBuilder.toString()
        }

        private fun appendIf(value: String?, result: StringBuilder, shouldAppend: Boolean) {
            if (shouldAppend) {
                result.append(':')
                result.append(value)
            }
        }

    }
}
