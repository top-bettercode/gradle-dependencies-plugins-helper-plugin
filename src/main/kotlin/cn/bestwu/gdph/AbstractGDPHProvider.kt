package cn.bestwu.gdph

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

abstract class AbstractGDPHProvider : DocumentationProvider {
    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        return null
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return null
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
        return null
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return null
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
        return null
    }


    protected fun pluginsDoc(searchText: String): String {
        return "<a href='https://plugins.gradle.org/search?term=$searchText'>search in GradlePlugins</a><br/>" +
                "<a href='https://plugins.gradle.org/plugin/$searchText'>show in GradlePlugins</a><br/>"
    }

    protected fun dependenciesDoc(dependency: String): String {
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


}