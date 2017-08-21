package cn.bestwu.gdph

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrCommandArgumentListImpl

class OpenJcenterOrMavenCentralProvider : DocumentationProvider {

    override fun getQuickNavigateInfo(element: PsiElement?, element1: PsiElement?): String? {
        return null
    }

    override fun getUrlFor(element: PsiElement?, element1: PsiElement?): List<String>? {
        return null
    }

    override fun generateDoc(element: PsiElement, element1: PsiElement?): String? {
        if (element is GroovyPsiElement || element is KtElement) {
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
        if (parent is GrCommandArgumentListImpl || parent is KtParenthesizedExpression) {
            parent = parent.parent
        }
        if (parent != null && parent.text.contains("version"))
            searchText = parent.text.replace(GradlePluginsCompletionContributor.regex, "$1").trim()
        return "<a href='https://plugins.gradle.org/search?term=$searchText'>search in GradlePlugins</a><br/>" +
                "<a href='https://plugins.gradle.org/plugin/$searchText'>show in GradlePlugins</a><br/>"
    }

    private fun dependenciesDoc(element: PsiElement): String? {
        var e = element
        do {
            e = e.parent ?: return null
        } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text)
        val dependency = trim(element.text)
        val mavenUrl = split(dependency).let {
            if (it.size >= 2) {
                if ("c" == it[0]) {
                    return "<a href='http://search.maven.org/#search|gav|1|c:\"${it[1]}\"'>search in mavenCentral</a><br/>"
                } else if ("fc" == it[0]) {
                    return "<a href='http://search.maven.org/#search|gav|1|fc:\"${it[1]}\"'>search in mavenCentral</a><br/>"
                } else
                    "http://search.maven.org/#search|gav|1|g:\"${it[0]}\" AND a:\"${it[1]}\""
            } else {
                "http://search.maven.org/#search|gav|1|g:\"${it[0]}\""
            }
        }
        return "<a href='https://bintray.com/search?query=$dependency'>search in jcenter</a><br/>" +
                "<a href='$mavenUrl'>search in mavenCentral</a><br/>"
    }

    override fun getDocumentationElementForLookupItem(manager: PsiManager?, o: Any?, element: PsiElement?): PsiElement? {
        return null
    }

    override fun getDocumentationElementForLink(manager: PsiManager?, s: String?, element: PsiElement?): PsiElement? {
        return null
    }
}
