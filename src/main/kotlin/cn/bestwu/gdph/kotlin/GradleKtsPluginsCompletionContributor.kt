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

import cn.bestwu.gdph.AbstractGradlePluginsCompletionContributor
import cn.bestwu.gdph.browseNotification
import cn.bestwu.gdph.contributorDuringCompletion
import cn.bestwu.gdph.insertHandler
import cn.bestwu.gdph.search.GradlePluginsSearcher
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import java.net.SocketTimeoutException

class GradleKtsPluginsCompletionContributor : AbstractGradlePluginsCompletionContributor() {
    object Util {
        val kotlinRegex = "^kotlin\\(\"(.*?)\".*$".toRegex()
    }

    companion object {
        const val KOTLIN_PREFIX = "org.jetbrains.kotlin."
    }

    init {
        extend(
                CompletionType.SMART,
                PlatformPatterns.psiElement(PsiElement::class.java)
                        .and(
                                PlatformPatterns.psiElement().inFile(
                                        PlatformPatterns.psiFile()
                                                .withName(StandardPatterns.string().endsWith(".gradle.kts"))
                                )
                        )
                        .withParent(KtLiteralStringTemplateEntry::class.java)
                        .withAncestor(
                                15, PlatformPatterns.psiElement(KtCallExpression::class.java)
                                .withText(StandardPatterns.string().startsWith(PLUGINS_EXTENSION))
                        ), CompletionPluginsCompletionProvider()
        )
    }

    override fun duringCompletion(context: CompletionInitializationContext) =
            contributorDuringCompletion(context)

    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
                params: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet
        ) {
            try {
                var parent = params.position.parent?.parent?.parent
                result.stopHere()
                if (parent is KtParenthesizedExpression) {
                    parent = parent.parent
                }
                val isVersion = parent != null && parent.text.contains("version")
                val text = parent!!.text.trim('"')
                var allText = ""
                var searchText = if (isVersion) {
                    if (text.startsWith("kotlin")) {
                        text.replace(Util.kotlinRegex, "$KOTLIN_PREFIX$1")
                    } else
                        text.replace(versionRegex, "$1")
                } else {
                    allText =
                            text.replace(idRegex, "$1").substringBefore("IntellijIdeaRulezzz$").trim()
                    allText.substringBeforeLast(".")
                }
                val isKotlin: Boolean
                if (!isVersion && parent.parent?.parent is KtCallExpression && parent.parent.parent.firstChild.text == "kotlin") {
                    isKotlin = true
                    searchText = KOTLIN_PREFIX
                    allText = KOTLIN_PREFIX + allText
                } else {
                    isKotlin = false
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
                    val lookupElementBuilder =
                            if (isKotlin) {
                                LookupElementBuilder.create(it.substringAfter(KOTLIN_PREFIX))
                                        .withPresentableText(
                                                it.replace(
                                                        GradlePluginsSearcher.SPLIT_RULE,
                                                        ":"
                                                )
                                        ).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler)
                            } else {
                                if (isVersion) LookupElementBuilder.create(it)
                                        .withIcon(AllIcons.Nodes.PpLib)
                                        .withInsertHandler(insertHandler) else LookupElementBuilder.create(
                                        it
                                ).withPresentableText(it.replace(GradlePluginsSearcher.SPLIT_RULE, ":"))
                                        .withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler)
                            }
                    completionResultSet.addElement(lookupElementBuilder)
                }
            } catch (e: SocketTimeoutException) {
                val url = "https://plugins.gradle.org/search"
                browseNotification(params.position.project, "Request timeout", url)
            }
        }


    }
}