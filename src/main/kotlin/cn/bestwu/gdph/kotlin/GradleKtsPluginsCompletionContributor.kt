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
import cn.bestwu.gdph.GradlePluginsSearcher
import cn.bestwu.gdph.insertHandler
import cn.bestwu.gdph.contributorDuringCompletion
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

class GradleKtsPluginsCompletionContributor : AbstractGradlePluginsCompletionContributor() {
    companion object {
        val kotlinRegex = "^kotlin\\(\"(.*?)\".*$".toRegex()
        val kotlinPrefix = "org.jetbrains.kotlin."
    }

    init {
        extend(CompletionType.SMART,
                PlatformPatterns.psiElement(PsiElement::class.java)
                        .and(PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle.kts"))))
                        .withParent(KtLiteralStringTemplateEntry::class.java)
                        .withAncestor(15, PlatformPatterns.psiElement(KtCallExpression::class.java)
                                .withText(StandardPatterns.string().startsWith(pluginsExtension)))
                , CompletionPluginsCompletionProvider())
    }

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)

    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
                params: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet) {
            var parent = params.position.parent?.parent?.parent
            result.stopHere()
            if (parent is KtParenthesizedExpression) {
                parent = parent.parent
            }
            var isVersion = parent != null && parent.text.contains("version")
            var searchText = if (isVersion) {
                val text = parent!!.text
                if (text.startsWith("kotlin")) {
                    text.replace(kotlinRegex, "$kotlinPrefix$1")
                } else
                    text.replace(regex, "$1")
            } else
                CompletionUtil.findReferenceOrAlphanumericPrefix(params)
            val isKotlin: Boolean
            if (parent?.parent?.parent is KtCallExpression && parent.parent.parent.firstChild.text == "kotlin") {
                isKotlin = true
                isVersion = isVersion || "(" != parent.prevSibling.text
                if (isVersion) {
                    searchText = parent.parent.parent.text.replace(kotlinRegex, "$kotlinPrefix$1")
                } else {
                    searchText = "$kotlinPrefix$searchText"
                }
            } else {
                isKotlin = false
            }

            var searchResult: List<String>
            var completionResultSet = result
            if (isVersion) {
                searchResult = pluginsSearcher.searchPluginVersions(searchText)
                completionResultSet = result.withRelevanceSorter(completionSorter(searchResult))
            } else {
                searchResult = pluginsSearcher.searchPlugins(searchText)
            }
            if (isKotlin && !isVersion) {
                searchResult = searchResult.filter { it.startsWith(kotlinPrefix) }
            }

            searchResult.forEach {
                val lookupElementBuilder =
                        if (isKotlin && !isVersion) {
                            LookupElementBuilder.create(it.substringAfter(kotlinPrefix)).withPresentableText(it.replace(GradlePluginsSearcher.splitRule, ":")).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler)
                        } else {
                            if (isVersion) LookupElementBuilder.create(it).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler) else LookupElementBuilder.create(it).withPresentableText(it.replace(GradlePluginsSearcher.splitRule, ":")).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(insertHandler)
                        }
                completionResultSet.addElement(lookupElementBuilder)
            }
        }


    }
}