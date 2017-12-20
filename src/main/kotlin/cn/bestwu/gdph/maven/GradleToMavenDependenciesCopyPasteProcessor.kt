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


    override fun preprocessOnCopy(file: PsiFile, startOffsets: IntArray, endOffsets: IntArray, text: String): String? {
        return null
    }

    override fun preprocessOnPaste(project: Project, file: PsiFile, editor: Editor, text: String, rawText: RawText?): String {
        return if (!canPasteForFile(file)) {
            text
        } else preprocessedText(text)
    }

    private fun canPasteForFile(file: PsiFile): Boolean {
        return file.name == "pom.xml"
    }

    private fun preprocessedText(text: String): String {
        val split = text.split(":")
        if (split.size == 3) {
            return """<dependency>
            <groupId>${split[0]}</groupId>
            <artifactId>${split[1]}</artifactId>
            <version>${split[2]}</version>
        </dependency>"""
        } else if (split.size == 2) {
            return """<dependency>
            <groupId>${split[0]}</groupId>
            <artifactId>${split[1]}</artifactId>
        </dependency>"""
        } else
            return text
    }

}
