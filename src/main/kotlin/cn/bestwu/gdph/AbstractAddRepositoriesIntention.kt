package cn.bestwu.gdph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.intentions.base.Intention
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import java.util.*

abstract class AbstractAddRepositoriesIntention : Intention() {

    private fun findClosure(psiFile: PsiFile, expressionName: String): GrMethodCall? {
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, GrMethodCall::class.java)
        return methodCalls.find { it.invokedExpression.text == expressionName }
    }

    protected fun processIntention(searchParam: SearchParam, project: Project, element: PsiElement) {
        val result: LinkedHashSet<ArtifactInfo> = GradleDependenciesCompletionContributor.artifactSearcher.search(GradleArtifactSearcher.keyBintray, searchParam, linkedSetOf(), project, GradleDependenciesCompletionContributor.artifactSearcher::searchInJcenter)
        if (result.isNotEmpty() && result.first().isSpecifiedRepo()) {
            val psiFile = element.containingFile
            val repositoriesClosure = findClosure(psiFile, "repositories")?.closureArguments?.first()
            val factory = GroovyPsiElementFactory.getInstance(project)
            val mavenRepo = "\t\tmaven { url '${result.first().repo()}' }"
            if (repositoriesClosure == null) {
                val dependenciesElement = findClosure(psiFile, "dependencies")
                psiFile.addBefore(factory.createStatementFromText("repositories {\n$mavenRepo\n}"), dependenciesElement)
                psiFile.addBefore(factory.createLineTerminator(2), dependenciesElement)
            } else {
                if (!repositoriesClosure.text.contains(result.first().repo())) {
                    repositoriesClosure.addStatementBefore(factory.createStatementFromText(mavenRepo), null)
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
}
