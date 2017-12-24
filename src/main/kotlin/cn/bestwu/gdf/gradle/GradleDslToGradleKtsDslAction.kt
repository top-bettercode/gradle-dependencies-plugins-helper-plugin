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

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.plugins.groovy.GroovyLanguage
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

/**
 *
 * @author Peter Wu
 * @since
 */
class GradleDslToGradleKtsDslAction : BaseRefactoringAction() {
    override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext): Boolean {
        return isEnabledOnElements(arrayOf(element))
    }

    override fun isAvailableInEditorOnly(): Boolean {
        return false
    }

    override fun isAvailableForLanguage(language: Language): Boolean {
        return GroovyLanguage == language
    }

    override fun isEnabledOnElements(elements: Array<PsiElement>): Boolean {

        return elements
                .map { it.containingFile }
                .none { it !is GroovyFile }
    }

    override fun getHandler(dataContext: DataContext) = object : RefactoringActionHandler {

        override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
            invokeInner(project, arrayOf(file), editor)
        }

        override fun invoke(project: Project, elements: Array<PsiElement>, dataContext: DataContext?) {
            var editor: Editor? = null
            if (dataContext != null) {
                editor = CommonDataKeys.EDITOR.getData(dataContext)
            }
            invokeInner(project, elements, editor)
        }

        private fun invokeInner(project: Project, elements: Array<PsiElement>, editor: Editor?) {
            val mapNotationToStringNotationAction = MapNotationToStringNotationAction()
            val event = AnActionEvent.createFromAnAction(mapNotationToStringNotationAction, null, "", dataContext)
            mapNotationToStringNotationAction.actionPerformed(event)

            val files = ContainerUtil.newHashSet<GroovyFile>()

            for (element in elements) {
                var e = element
                if (element !is PsiFile) {
                    e = element.containingFile
                }

                if (e is GroovyFile) {
                    files.add(e)
                } else {
                    if (!ApplicationManager.getApplication().isUnitTestMode) {
                        CommonRefactoringUtil.showErrorHint(project, editor, "Only GroovyDsl can convert", "error", null)
                        return
                    }
                }
            }
            GradleDslToGradleKtsDslProcessor(project, *files.toTypedArray()).run()
        }
    }
}