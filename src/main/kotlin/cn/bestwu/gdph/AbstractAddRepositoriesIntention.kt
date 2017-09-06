package cn.bestwu.gdph

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.intentions.base.Intention
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import java.util.*

abstract class AbstractAddRepositoriesIntention : Intention() {

    private fun findRepositoriesClosure(psiFile: PsiFile): GrClosableBlock? {
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, GrMethodCall::class.java)
        val repositoriesBlock = methodCalls.find { it.invokedExpression.text == "repositories" } ?: return null
        return repositoriesBlock.closureArguments.first()
    }

    protected fun processIntention(searchParam: SearchParam, project: Project, element: PsiElement) {
        val result: LinkedHashSet<ArtifactInfo> = GradleDependenciesCompletionContributor.artifactSearcher.search(GradleArtifactSearcher.keyBintray, searchParam, project, GradleDependenciesCompletionContributor.artifactSearcher::searchInJcenter)
        if (result.isNotEmpty() && result.first().isSpecifiedRepo()) {
            val repositoriesClosure = findRepositoriesClosure(element.containingFile)
            val factory = GroovyPsiElementFactory.getInstance(project)
            val mavenRepo = "\t\tmaven { url '${result.first().repo()}' }"
            if (repositoriesClosure == null) {
                element.containingFile.add(factory.createLineTerminator(2))
                element.containingFile.add(factory.createStatementFromText("repositories {\n$mavenRepo\n}"))
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
