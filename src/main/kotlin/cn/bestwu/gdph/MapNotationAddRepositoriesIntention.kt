package cn.bestwu.gdph

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList

class MapNotationAddRepositoriesIntention : AbstractAddRepositoriesIntention() {

    private var group: String? = null
    private var name: String? = null

    override fun processIntention(element: PsiElement, project: Project, editor: Editor) {
        processIntention(SearchParam(group!!, name!!, true, true), project, element)
    }

    override fun getElementPredicate(): PsiElementPredicate {
        return PsiElementPredicate { element ->
            if (GradleDependenciesCompletionContributor.IN_MAP_DEPENDENCY_NOTATION.accepts(element) && element.parent.parent.parent is GrArgumentList) {
                val argumentList = element.parent.parent.parent as GrArgumentList
                val namedArguments = argumentList.namedArguments
                val map = namedArguments.associate {
                    val expression = it.expression
                    if (it.label != null && expression != null) {
                        return@associate Pair(it.label!!.text, trim(expression.text))
                    } else
                        return@associate Pair(null, null)
                }
                group = map["group"]
                name = map["name"]
                !group.isNullOrBlank() && !name.isNullOrBlank()
            } else {
                false
            }
        }
    }


}