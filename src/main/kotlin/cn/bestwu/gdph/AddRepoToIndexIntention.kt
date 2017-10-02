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

import cn.bestwu.gdph.config.ProjectSettings
import cn.bestwu.gdph.maven.ImportMavenRepositoriesTask
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.groovy.intentions.base.Intention
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil

class AddRepoToIndexIntention : Intention() {

    override fun getText(): String {
        return "Add specified repository to maven repositories index"
    }

    override fun getFamilyName(): String {
        return "Add specified repository to maven repositories index"
    }

    override fun processIntention(element: PsiElement, project: Project, editor: Editor) {
        ProjectSettings.getInstance(project).remoteRepositories += ProjectSettings.separator + GrStringUtil.removeQuotes(element.text)
        ImportMavenRepositoriesTask.performTask(project)
    }

    override fun getElementPredicate(): PsiElementPredicate {
        return PsiElementPredicate { element ->
            cn.bestwu.gdph.config.Settings.getInstance().useMavenIndex && GrStringUtil.removeQuotes(element.text).isNotBlank() && PlatformPatterns.psiElement()
                    .and(GradleDependenciesCompletionContributor.GRADLE_FILE_PATTERN)
                    .withParent(GrLiteral::class.java)
                    .and(PlatformPatterns.psiElement()
                            .inside(true, PlatformPatterns.psiElement(GrMethodCallExpression::class.java).with(
                                    object : PatternCondition<GrMethodCallExpression>("withInvokedExpressionText") {
                                        override fun accepts(expression: GrMethodCallExpression, context: ProcessingContext): Boolean {
                                            if (checkExpression(expression)) return true
                                            return checkExpression(PsiTreeUtil.getParentOfType(expression, GrMethodCallExpression::class.java))
                                        }

                                        private fun checkExpression(expression: GrMethodCallExpression?): Boolean {
                                            if (expression == null) return false
                                            val grExpression = expression.invokedExpression
                                            return "maven" == grExpression.text
                                        }
                                    }))).accepts(element)
        }
    }


}