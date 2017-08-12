package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement

class OpenJcenterOrMavenCentralProvider : DocumentationProvider {

    companion object {
        private const val GROUP_AND_ARTIFACT: String = "http://search.maven.org/#search|gav|1|g:\"%s\" AND a:\"%s\""
        private const val GROUP: String = "http://search.maven.org/#search|gav|1|g:\"%s\""
    }

    override fun getQuickNavigateInfo(element: PsiElement?, element1: PsiElement?): String? {
        return null
    }

    override fun getUrlFor(element: PsiElement?, element1: PsiElement?): List<String>? {
        return null
    }

    override fun generateDoc(element: PsiElement, element1: PsiElement?): String? {
        if (element is GroovyPsiElement || element is KtElement) {
            var e = element
            do {
                e = e.parent ?: return null
            } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text)
            if (element1 != null) {
                val dependency = element.text.trim('"', '\'', '(', ')')
                val mavenUrl = split(dependency).let {
                    if (it.size >= 2) {
                        GROUP_AND_ARTIFACT.format(it[0], it[1])
                    } else {
                        GROUP.format(it[0])
                    }
                }
                return "<a href=\"https://bintray.com/search?query=$dependency\">search in jcenter</a><br/>" +
                        "<a href='$mavenUrl'>search in mavenCentral</a><br/>"
            }
        }
        return null
    }

    override fun getDocumentationElementForLookupItem(manager: PsiManager?, o: Any?, element: PsiElement?): PsiElement? {
        return null
    }

    override fun getDocumentationElementForLink(manager: PsiManager?, s: String?, element: PsiElement?): PsiElement? {
        return null
    }
}
