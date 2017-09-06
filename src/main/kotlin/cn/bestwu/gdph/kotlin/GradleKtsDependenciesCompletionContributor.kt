package cn.bestwu.gdph.kotlin

import cn.bestwu.gdph.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
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
                val searchText = if (!text.startsWith("c:", true) && !text.startsWith("fc:", true)) prefix else text
                var isKotlin = false
                var isVersion = false
                val searchParam = if (text.contains(":") && !searchText.contains(":")) {
                    SearchParam(searchText, "", false, false)
                } else {
                    val parent = params.position.parent.parent.parent
                    val pText = parent.parent.parent.text
                    if (pText.startsWith("kotlin")) {
                        isKotlin = true
                        if ("(" != parent.prevSibling.text) {
                            isVersion = true
                            SearchParam(pText.replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "$kotlinPrefix$1:"))
                        } else
                            SearchParam("${kotlinPrefix}$searchText")
                    } else
                        SearchParam(searchText)
                }
                if (searchParam.id.length < 2)
                    return
                var searchResult = artifactSearcher.search(searchParam, params.position.project)
                if (searchResult.isEmpty()) {
                    show(params.position.project, searchParam.failContent, "find dependencies fail", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
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
                val cSearch = searchParam.advancedSearch.isNotEmpty()
                if (cSearch) {
                    completionResultSet = completionResultSet.withPrefixMatcher(PrefixMatcher.ALWAYS_TRUE)
                }
                searchResult.forEach {
                    val lookupElementBuilder =
                            if (isKotlin) {
                                if (isVersion)
                                    LookupElementBuilder.create(it.version).withPresentableText(it.version).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER)
                                else
                                    LookupElementBuilder.create(it.gav.substringAfter(kotlinPrefix)).withPresentableText(it.gav).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER)
                            } else
                                LookupElementBuilder.create("${it.gav}${if (it.artifactId.isEmpty()) ":" else ""}").withPresentableText(it.gav).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true).withInsertHandler(INSERT_HANDLER)
                    completionResultSet.addElement(lookupElementBuilder)
                }
            }
        })
    }

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)

    companion object {
        private val DEPENDENCIES_SCRIPT_BLOCK = "dependencies"
        val kotlinPrefix = "org.jetbrains.kotlin:kotlin-"
        private val artifactSearcher = GradleArtifactSearcher()

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

        val IN_METHOD_DEPENDENCY_NOTATION = PlatformPatterns.psiElement()
                .withParent(KtLiteralStringTemplateEntry::class.java)
                .and(PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle.kts"))))
                .and(DEPENDENCIES_CALL_PATTERN)
    }
}

