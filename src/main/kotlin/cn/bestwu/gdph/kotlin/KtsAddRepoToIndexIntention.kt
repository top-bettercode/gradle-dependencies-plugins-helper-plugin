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

import cn.bestwu.gdph.config.ProjectSettings
import cn.bestwu.gdph.maven.ImportMavenRepositoriesTask
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil

class KtsAddRepoToIndexIntention : IntentionAction {
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
        ProjectSettings.getInstance(project).remoteRepositories += ProjectSettings.separator + GrStringUtil.removeQuotes(element.text)
        ImportMavenRepositoriesTask.performTask(project)
    }

    override fun getText(): String {
        return "Add specified repository to maven repositories index"
    }

    override fun getFamilyName(): String {
        return "Add specified repository to maven repositories index"
    }


    private fun satisfiedElement(element: PsiElement): Boolean {
        return cn.bestwu.gdph.config.Settings.getInstance().useMavenIndex && GrStringUtil.removeQuotes(element.text).isNotBlank() && PlatformPatterns.psiElement()
                .withParent(KtLiteralStringTemplateEntry::class.java)
                .and(GradleKtsDependenciesCompletionContributor.GRADLE_KTS_FILE_PATTERN)
                .and(PlatformPatterns.psiElement()
                        .inside(true, PlatformPatterns.psiElement(KtCallExpression::class.java).with(
                                object : PatternCondition<KtCallExpression>("withInvokedExpressionText") {
                                    override fun accepts(expression: KtCallExpression, context: ProcessingContext): Boolean {
                                        if (checkExpression(expression)) return true
                                        return checkExpression(PsiTreeUtil.getParentOfType(expression, KtCallExpression::class.java))
                                    }

                                    private fun checkExpression(expression: KtCallExpression?): Boolean {
                                        if (expression == null) return false
                                        val grExpression = expression.firstChild ?: return false
                                        return "maven" == grExpression.text
                                    }
                                })))
                .accepts(element)
    }


}


