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

package cn.bestwu.gdph.kotlin

import cn.bestwu.gdph.search.ArtifactInfo
import cn.bestwu.gdph.search.JcenterSearcher
import cn.bestwu.gdph.search.SearchParam
import cn.bestwu.gdph.search.toSearchParam
import cn.bestwu.gdph.show
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.psi.KtScriptInitializer
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil
import java.util.*

open class KtsMethodNotationAddRepositoriesIntention : IntentionAction {
    override fun startInWriteAction(): Boolean {
        return true
    }

    private fun findMatchingElement(file: PsiFile, editor: Editor): PsiElement? {
        if (!file.viewProvider.languages.contains(KotlinLanguage.INSTANCE)) {
            return null
        }

        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            val start = selectionModel.selectionStart
            val end = selectionModel.selectionEnd

            if (start in 0..end) {
                val selectionRange = TextRange(start, end)
                var element = PsiImplUtil.findElementInRange(file, start, end, PsiElement::class.java)
                while (element != null && element.textRange != null && selectionRange.contains(element.textRange)) {
                    if (satisfiedElement(element)) return element
                    element = element.parent
                }
            }
        }

        val position = editor.caretModel.offset
        var element = file.findElementAt(position)
        while (element != null) {
            if (satisfiedElement(element)) return element
            if (element is PsiFile) break
            element = element.parent
        }

        element = file.findElementAt(position - 1)
        while (element != null) {
            if (satisfiedElement(element)) return element
            if (element is PsiFile) return null
            element = element.parent
        }

        return null
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return findMatchingElement(file, editor) != null
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val element = findMatchingElement(file, editor) ?: return
        assert(element.isValid) { element }
        processIntention(element, project)
    }

    private fun findClosure(psiFile: PsiFile, expressionName: String): PsiElement? {
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(PsiTreeUtil.getChildOfType(psiFile, KtScript::class.java)?.firstChild ?: return null, KtScriptInitializer::class.java)
        return methodCalls.find {
            it.firstChild.firstChild.text == expressionName
        }
    }

    private fun processIntention(searchParam: SearchParam, project: Project, element: PsiElement) {
        val result: LinkedHashSet<ArtifactInfo> = JcenterSearcher.search(searchParam, project, linkedSetOf())
        if (result.isNotEmpty()) {
            val psiFile = element.containingFile
            val repositoriesClosure = findClosure(psiFile, "repositories")?.firstChild?.lastChild?.firstChild?.firstChild as? KtFunctionLiteral
            val factory = KtsPsiElementFactory(project)
            val repo = if (result.first().isSpecifiedRepo()) "\t\tmaven { url = uri(\"${result.first().repo()}\") }" else "\t\tjcenter()"
            if (repositoriesClosure == null) {
                val dependenciesElement = findClosure(psiFile, "dependencies")!!
                dependenciesElement.parent.addBefore(factory.createStatementFromText("repositories {\n$repo\n}"), dependenciesElement)
                dependenciesElement.parent.addBefore(GroovyPsiElementFactory.getInstance(project).createLineTerminator(2), dependenciesElement)
            } else {
                if (repositoriesClosure.text.contains(if (result.first().isSpecifiedRepo()) result.first().repo() else "jcenter")) {
                    show(project, "repository:\n$repo\n already in repositories", type = NotificationType.WARNING)
                }else{
                    repositoriesClosure.addBefore(factory.createStatementFromText(repo), repositoriesClosure.rBrace)
                }
            }
        }
    }


    override fun getText(): String {
        return "Add specified repository to repositories"
    }

    override fun getFamilyName(): String {
        return "Add specified repository to repositories"
    }

    private fun processIntention(element: PsiElement, project: Project) {
        var stringNotation = GrStringUtil.removeQuotes(element.text)
        if (stringNotation.count { it == ':' } == 1) {
            stringNotation += ":"
        }
        processIntention(toSearchParam(stringNotation), project, element)
    }

    private fun satisfiedElement(element: PsiElement): Boolean {
        return GradleKtsDependenciesCompletionContributor.IN_METHOD_DEPENDENCY_NOTATION.accepts(element) && GrStringUtil.removeQuotes(element.text).count { it == ':' } > 0
    }


}