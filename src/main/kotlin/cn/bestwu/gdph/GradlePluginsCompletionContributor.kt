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

package cn.bestwu.gdph

import cn.bestwu.gdph.search.GradlePluginsSearcher
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl
import java.net.SocketTimeoutException


class GradlePluginsCompletionContributor : AbstractGradlePluginsCompletionContributor() {

    init {
        extend(
                CompletionType.SMART,
                psiElement(PsiElement::class.java)
                        .and(
                                psiElement().inFile(
                                        PlatformPatterns.psiFile().withName(string().endsWith(".gradle"))
                                )
                        )
                        .withParent(GrLiteral::class.java)
                        .withAncestor(
                                10, psiElement(GrMethodCallExpressionImpl::class.java)
                                .withText(string().startsWith(pluginsExtension))
                        ), CompletionPluginsCompletionProvider()
        )

    }


    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
                params: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet
        ) {
            try {
                val parent = params.position.parent?.parent?.parent
                result.stopHere()
                val isVersion = parent != null && parent.text.contains("version")
                var allText = ""
                val text = parent!!.text
                val searchText = if (isVersion) {
                    text.replace(versionRegex, "$1")
                } else {
                    allText =
                            text.replace(idRegex, "$1").substringBefore("IntellijIdeaRulezzz ").trim()
                    allText.substringBeforeLast(".")
                }
                val searchResult: List<String>
                var completionResultSet = result
                if (isVersion) {
                    searchResult = GradlePluginsSearcher.searchPluginVersions(searchText)
                    completionResultSet = result.withRelevanceSorter(completionSorter(searchResult))
                } else {
                    if (searchText.length < 2) {
                        return
                    }
                    searchResult =
                            searchResultFix(GradlePluginsSearcher.searchPlugins(searchText), allText)
                }


                searchResult.forEach {
                    val lookupElementBuilder = if (isVersion) LookupElementBuilder.create(it)
                            .withIcon(AllIcons.Nodes.PpLib)
                            .withInsertHandler(insertHandler) else LookupElementBuilder.create(it)
                            .withPresentableText(it.replace(GradlePluginsSearcher.splitRule, ":"))
                            .withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler)
                    completionResultSet.addElement(lookupElementBuilder)
                }
            } catch (e: SocketTimeoutException) {
                val url = "https://plugins.gradle.org/search"
                browseNotification(params.position.project, "Request timeout", url)
            }
        }
    }
}

