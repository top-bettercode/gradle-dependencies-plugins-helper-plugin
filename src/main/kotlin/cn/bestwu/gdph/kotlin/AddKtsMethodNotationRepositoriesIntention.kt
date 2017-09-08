package cn.bestwu.gdph.kotlin

import cn.bestwu.gdph.ArtifactInfo
import cn.bestwu.gdph.GradleArtifactSearcher
import cn.bestwu.gdph.GradleDependenciesCompletionContributor
import cn.bestwu.gdph.SearchParam
import com.intellij.codeInsight.intention.IntentionAction
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

open class AddKtsMethodNotationRepositoriesIntention : IntentionAction {
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
        val result: LinkedHashSet<ArtifactInfo> = GradleDependenciesCompletionContributor.artifactSearcher.search(GradleArtifactSearcher.keyBintray, searchParam, linkedSetOf(), project, GradleDependenciesCompletionContributor.artifactSearcher::searchInJcenter)
        if (result.isNotEmpty() && result.first().isSpecifiedRepo()) {
            val psiFile = element.containingFile
            val repositoriesClosure = findClosure(psiFile, "repositories")?.firstChild?.lastChild?.firstChild?.firstChild as? KtFunctionLiteral
            val factory = KtsPsiElementFactory(project)
            val mavenRepo = "\t\tmaven { url = uri(\"${result.first().repo()}\") }"
            if (repositoriesClosure == null) {
                val dependenciesElement = findClosure(psiFile, "dependencies")!!
                dependenciesElement.parent.addBefore(factory.createStatementFromText("repositories {\n$mavenRepo\n}"), dependenciesElement)
                dependenciesElement.parent.addBefore(GroovyPsiElementFactory.getInstance(project).createLineTerminator(2), dependenciesElement)
            } else {
                if (!repositoriesClosure.text.contains(result.first().repo())) {
                    repositoriesClosure.addBefore(factory.createStatementFromText(mavenRepo), repositoriesClosure.rBrace)
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
        processIntention(SearchParam(stringNotation), project, element)
    }

    private fun satisfiedElement(element: PsiElement): Boolean {
        return GradleKtsDependenciesCompletionContributor.IN_METHOD_DEPENDENCY_NOTATION.accepts(element) && GrStringUtil.removeQuotes(element.text).count { it == ':' } > 0
    }


}