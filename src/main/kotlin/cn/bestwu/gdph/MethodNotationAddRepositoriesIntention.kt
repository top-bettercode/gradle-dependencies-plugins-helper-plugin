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

package cn.bestwu.gdph

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil

class MethodNotationAddRepositoriesIntention : AbstractAddRepositoriesIntention() {

    override fun processIntention(element: PsiElement, project: Project, editor: Editor) {
        var stringNotation = GrStringUtil.removeQuotes(element.text)
        if (stringNotation.count { it == ':' } == 1) {
            stringNotation += ":"
        }
        processIntention(SearchParam(stringNotation), project, element)
    }

    override fun getElementPredicate(): PsiElementPredicate {
        return PsiElementPredicate { element ->
            val text = GrStringUtil.removeQuotes(element.text)
            GradleDependenciesCompletionContributor.IN_METHOD_DEPENDENCY_NOTATION.accepts(element) && !text.contains("['\"]".toRegex()) && text.count { it == ':' } > 0
        }
    }


}