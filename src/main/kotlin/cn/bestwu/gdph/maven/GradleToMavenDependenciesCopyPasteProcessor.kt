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

package cn.bestwu.gdph.maven

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class GradleToMavenDependenciesCopyPasteProcessor : CopyPastePreProcessor {

    companion object {
        val dependencyRegex = Regex("^ *([A-Za-z]*?)[( ]*['\"]?([.A-Za-z0-9\\-]*:[.A-Za-z0-9\\-]*(:[.A-Za-z0-9\\-]*)?)['\"]?[) ]*$")
    }

    override fun preprocessOnCopy(file: PsiFile, startOffsets: IntArray, endOffsets: IntArray, text: String): String? {
        return null
    }

    override fun preprocessOnPaste(project: Project, file: PsiFile, editor: Editor, text: String, rawText: RawText?): String {
        return if (!canPasteForFile(file)) {
            text
        } else {
            var result = ""
            for (s in text.split("\n")) {
                result += preprocessedText(s) + "\n"
            }
            return result.trimEnd('\n')
        }
    }

    private fun canPasteForFile(file: PsiFile): Boolean {
        return file.name == "pom.xml"
    }

    private fun preprocessedText(text: String): String {
        val dependency: String
        val scope: String
        val matchResult = dependencyRegex.find(text)
        if (matchResult == null) {
            return text
        } else {
            val groupValues = matchResult.groupValues
            scope = when (groupValues[1]) {
                "providedCompile", "provided" -> "provided"
                "testCompile" -> "test"
                else -> "compile"
            }
            dependency = groupValues[2]

            val split = dependency.split(":")
            return when {
                split.size == 3 -> "<dependency>\n\t<groupId>${split[0]}</groupId>\n\t<artifactId>${split[1]}</artifactId>\n\t<version>${split[2]}</version>${if (scope != "compile") "\n\t<scope>$scope</scope>" else ""}\n</dependency>"
                split.size == 2 -> "<dependency>\n\t<groupId>${split[0]}</groupId>\n\t<artifactId>${split[1]}</artifactId>${if (scope != "compile") "\n\t<scope>$scope</scope>" else ""}\n</dependency>"
                else -> text
            }
        }
    }

}
