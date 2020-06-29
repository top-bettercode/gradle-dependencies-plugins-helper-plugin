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

import cn.bestwu.gdf.DependencyUtil
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall

class MapNotationToStringNotationAction : CodeInsightAction() {

    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isValidForFile(project, editor, file) && file.name == "build.gradle"
    }

    override fun getHandler() = object : CodeInsightActionHandler {

        private fun findDependenciesClosure(psiFile: PsiFile): GrClosableBlock? {
            val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, GrMethodCall::class.java)
            val dependenciesBlock = methodCalls.find { it.invokedExpression.text == "dependencies" }
                    ?: return null
            return dependenciesBlock.closureArguments.first()
        }

        private fun convertToStringNotation(dependenciesClosure: GrClosableBlock, factory: GroovyPsiElementFactory) {
            val statements = PsiTreeUtil.getChildrenOfTypeAsList(dependenciesClosure, GrMethodCall::class.java)
            statements.forEach { it.delete() }
            statements.forEach {
                dependenciesClosure.addStatementBefore(factory.createExpressionFromText(toStringNotation(it)), null)
            }
        }

        fun toStringNotation(methodCall: GrMethodCall): String {
            val text = methodCall.text
            if (methodCall.argumentList.allArguments.size != 3 && methodCall.argumentList.allArguments.size != 4) {
                return text.replace("^( *[A-Za-z]+) ('?)(.*)('?) *$".toRegex(), "$1($2$3$4)").replace("'", "\"")
            }
            val coordinate = Coordinate.fromMap(DependencyUtil.toMap(methodCall.namedArguments))
            return text.replace(methodCall.argumentList.text, "(\"${coordinate.toStringNotation()}\")").replace(" ", "").replace("'", "\"")
        }


        private fun removeEmptyLines(dependenciesClosure: GrClosableBlock, factory: GroovyPsiElementFactory) {
            val dependenciesClosureText = dependenciesClosure.text
            val withoutEmptyLines = dependenciesClosureText.replace(Regex("\n[ \t]*(?=\n)"), "")
            if (withoutEmptyLines != dependenciesClosureText) {
                dependenciesClosure.replace(factory.createClosureFromText(withoutEmptyLines))
            }
        }

        override fun invoke(project: Project, editor: Editor, file: PsiFile) {
            WriteCommandAction.writeCommandAction(project, file).run<Exception> {
                val dependenciesClosure = findDependenciesClosure(file)
                if (dependenciesClosure != null) {
                    val factory = GroovyPsiElementFactory.getInstance(project)
                    convertToStringNotation(dependenciesClosure, factory)
                    removeEmptyLines(dependenciesClosure, factory)
                }
            }
        }

        override fun startInWriteAction(): Boolean = false
    }

}