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

import cn.bestwu.gdph.*
import cn.bestwu.gdph.search.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry

class GradleKtsDependenciesCompletionContributor : CompletionContributor() {

    init {
        // group:name:version notation
        // e.g.:
        //    compile('junit:junit:4.11')
        extend(CompletionType.SMART, IN_METHOD_DEPENDENCY_NOTATION, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(params: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                result.stopHere()
                val text = trim(params.originalPosition?.text ?: "")
                val prefix = params.position.text.substringBefore("IntellijIdeaRulezzz")
                val iSearchParam: ISearchParam
                var isKotlin = false
                var isVersion = false
                var searchResult = if (text.startsWith("c:", true) || text.startsWith("fc:", true)) {
                    val classNameSearchParam = ClassNameSearchParam(text)
                    if (classNameSearchParam.q.length < 2)
                        return
                    iSearchParam = classNameSearchParam
                    GradleArtifactSearcher.searchByClassName(classNameSearchParam, params.position.project)
                } else {
                    val searchParam = if (text.contains(":") && !prefix.contains(":")) {
                        SearchParam(prefix, "", false, false)
                    } else {
                        val parent = params.position.parent.parent.parent
                        val pText = parent.parent.parent.text
                        if (pText.startsWith("kotlin")) {
                            isKotlin = true
                            if ("(" != parent.prevSibling.text) {
                                isVersion = true
                                SearchParam(kotlinGroup, pText.replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "$kotlinArtifactPrefix$1"), true, true)
                            } else {
                                SearchParam(kotlinGroup, "$kotlinArtifactPrefix$prefix", true, false)
                            }
                        } else
                            toSearchParam(prefix)
                    }
                    if (searchParam.src.length < 2)
                        return
                    iSearchParam = searchParam
                    GradleArtifactSearcher.search(searchParam, params.position.project)
                }
                if (searchResult.isEmpty()) {
                    show(params.position.project, iSearchParam.docUrl, "find dependencies fail", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }
                if (isKotlin) {
                    if (!isVersion)
                        searchResult.forEach { it.version = "" }
                    searchResult = searchResult.filter { it.gav.startsWith(kotlinPrefix) }.toSet()
                }
                var completionResultSet = if (isVersion) result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(searchResult.indexOfFirst { it.version == element.lookupString })
                            }
                        })
                ) else result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(searchResult.indexOfFirst { it.gav == (if (isKotlin) kotlinPrefix + element.lookupString else element.lookupString) })
                            }
                        })
                )
                if (iSearchParam is ClassNameSearchParam) {
                    completionResultSet = completionResultSet.withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
                }
                searchResult.forEach {
                    val lookupElementBuilder =
                            if (isKotlin) {
                                if (isVersion)
                                    LookupElementBuilder.create(it.version).withPresentableText(it.version).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(insertHandler)
                                else
                                    LookupElementBuilder.create(it.gav.substringAfter(kotlinPrefix)).withPresentableText(it.gav).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(insertHandler)
                            } else
                                LookupElementBuilder.create("${it.gav}${if (it.artifactId.isEmpty()) ":" else ""}").withPresentableText(it.gav).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(insertHandler)
                    completionResultSet.addElement(lookupElementBuilder)
                }
            }
        })
    }

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)

    companion object {
        private val DEPENDENCIES_SCRIPT_BLOCK = "dependencies"
        val kotlinPrefix = "org.jetbrains.kotlin:kotlin-"
        val kotlinArtifactPrefix = "kotlin-"
        val kotlinGroup = "org.jetbrains.kotlin"

        private val DEPENDENCIES_CALL_PATTERN = PlatformPatterns.psiElement()
                .inside(true, PlatformPatterns.psiElement(KtCallExpression::class.java).with(
                        object : PatternCondition<KtCallExpression>("withInvokedExpressionText") {
                            override fun accepts(expression: KtCallExpression, context: ProcessingContext): Boolean {
                                if (checkExpression(expression)) return true
                                return checkExpression(PsiTreeUtil.getParentOfType(expression, KtCallExpression::class.java))
                            }

                            private fun checkExpression(expression: KtCallExpression?): Boolean {
                                if (expression == null) return false
                                val grExpression = expression.firstChild ?: return false
                                return DEPENDENCIES_SCRIPT_BLOCK == grExpression.text || "imports" == grExpression.text
                            }
                        }))

        val GRADLE_KTS_FILE_PATTERN = PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle.kts")))

        val IN_METHOD_DEPENDENCY_NOTATION: PsiElementPattern.Capture<PsiElement> = PlatformPatterns.psiElement()
                .withParent(KtLiteralStringTemplateEntry::class.java)
                .and(GRADLE_KTS_FILE_PATTERN)
                .and(DEPENDENCIES_CALL_PATTERN)
    }
}

