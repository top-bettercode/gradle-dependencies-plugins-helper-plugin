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

package cn.bestwu.gdph.kotlin

import cn.bestwu.gdph.AbstractGDPHProvider
import cn.bestwu.gdph.AbstractGradlePluginsCompletionContributor
import cn.bestwu.gdph.trim
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

class KtsOpenJcenterOrMavenCentralProvider : AbstractGDPHProvider() {


    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element is KtValueArgumentList || element is KtStringTemplateExpression) {
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

        if (parent is KtParenthesizedExpression) {
            parent = parent.parent
        }
        val parentText = parent.text
        if (parent != null) {
            if (parentText.contains("version")) {
                searchText = if (parentText.startsWith("kotlin")) {
                    parentText.replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1").trim()
                } else {
                    parentText.replace(AbstractGradlePluginsCompletionContributor.regex, "$1").trim()
                }
            } else if (parentText.startsWith("kotlin")) {
                searchText = searchText.replace("^(.*?)\".*$".toRegex(), "$1")
                searchText = "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$searchText"
            } else if (parent.parent.parent.text.startsWith("kotlin")) {
                searchText = trim(element.parent.parent.text).replace("^(.*?)\".*$".toRegex(), "$1")
                searchText = "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$searchText"
            }
        }
        return pluginsDoc(searchText)
    }

    private fun dependenciesDoc(element: PsiElement): String? {
        var e = element
        do {
            e = e.parent ?: return null
        } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text)
        var dependency = trim(element.text)
        if (element.parent.text.startsWith("kotlin") || element.parent.parent.parent.text.startsWith("kotlin")) {
            dependency = "${GradleKtsDependenciesCompletionContributor.kotlinPrefix}$dependency"
        }

        return dependenciesDoc(dependency)
    }


}