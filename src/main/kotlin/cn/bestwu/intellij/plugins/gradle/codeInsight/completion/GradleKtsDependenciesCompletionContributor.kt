package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

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
import org.jetbrains.plugins.gradle.codeInsight.AbstractGradleCompletionContributor

class GradleKtsDependenciesCompletionContributor : AbstractGradleCompletionContributor() {

    init {
        // group:name:version notation
        // e.g.:
        //    compile('junit:junit:4.11')
        extend(CompletionType.SMART, IN_METHOD_DEPENDENCY_NOTATION, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(params: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                result.stopHere()
                val searchText = params.position.text.substringBefore("IntellijIdeaRulezzz")
                val searchParam = ArtifactInfo(searchText)
                if (searchParam.id.length < 2)
                    return
                val searchResult = artifactSearcher.search(searchParam)
                if (searchResult.isEmpty()) {
                    show("find dependencies fail", "<a href=\"https://bintray.com/search?query=${searchParam.id}\">search in jcenter</a><br/>", NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
                }

                val resultSet = searchResult.distinctBy { it.presentableText }
                val completionResultSet = result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(resultSet.indexOfFirst { it.presentableText == element.lookupString })
                            }
                        })
                )
                resultSet.forEach {
                    completionResultSet.addElement(LookupElementBuilder.create(it.presentableText).withIcon(AllIcons.Nodes.PpLib).withTypeText(it.type(), repoIcon, true))
                }
            }
        })
    }

    companion object {
        private val DEPENDENCIES_SCRIPT_BLOCK = "dependencies"
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

        private val IN_METHOD_DEPENDENCY_NOTATION = PlatformPatterns.psiElement()
                .withParent(KtLiteralStringTemplateEntry::class.java)
                .and(PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle.kts"))))
                .and(DEPENDENCIES_CALL_PATTERN)
    }
}

