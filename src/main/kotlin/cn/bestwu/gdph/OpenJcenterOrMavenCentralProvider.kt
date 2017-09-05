package cn.bestwu.gdph

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrCommandArgumentListImpl
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.literals.GrLiteralImpl
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.literals.GrStringContentImpl

class OpenJcenterOrMavenCentralProvider : AbstractGDPHProvider() {

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element is GrLiteralImpl || element is GrStringContentImpl) {
            return dependenciesDoc(element) ?: pluginsDoc(element)
        }
        return null
    }

    private fun pluginsDoc(element: PsiElement): String? {
        var e = element
        do {
            e = e.parent ?: return null
        } while ("plugins" != e.firstChild.text)
        var parent = element.parent
        var searchText = trim(element.text)

        if (parent is GrCommandArgumentListImpl) {
            parent = parent.parent
        }
        val parentText = parent.text
        if (parent != null) {
            if (parentText.contains("version")) {
                searchText = parentText.replace(AbstractGradlePluginsCompletionContributor.regex, "$1").trim()
            }
        }
        return pluginsDoc(searchText)
    }

    private fun dependenciesDoc(element: PsiElement): String? {
        var e = element
        do {
            e = e.parent ?: return null
        } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text)
        return dependenciesDoc(trim(element.text))
    }
}
