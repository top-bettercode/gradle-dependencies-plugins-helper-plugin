package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.plugins.gradle.codeInsight.AbstractGradleCompletionContributor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl

class GradlePluginsCompletionContributor : AbstractGradleCompletionContributor() {

    init {
        extend(CompletionType.SMART,
                psiElement(PsiElement::class.java)
                        .and(psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle.kts"))))
                        .withParent(KtLiteralStringTemplateEntry::class.java)
                        .withAncestor(15, psiElement(KtCallExpression::class.java)
                                .withText(string().startsWith("plugins")))
                , CompletionPluginsCompletionProvider())
        extend(CompletionType.SMART,
                psiElement(PsiElement::class.java)
                        .and(psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle"))))
                        .withParent(GrLiteral::class.java)
                        .withAncestor(10, psiElement(GrMethodCallExpressionImpl::class.java)
                                .withText(string().startsWith("plugins")))
                , CompletionPluginsCompletionProvider())

    }

    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {
        companion object {
            private val regex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()
        }

        val pluginsSearcher = GradlePluginsSearcher()

        override fun addCompletions(
                params: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet) {
            var parent = params.position.parent?.parent?.parent
            result.stopHere()
            val searchText = CompletionUtil.findReferenceOrAlphanumericPrefix(params)
            if (parent is KtParenthesizedExpression) {
                parent = parent.parent
            }
            val searchResult: List<String>
            var completionResultSet = result
            if (parent != null && parent.text.contains("version")) {
                searchResult = pluginsSearcher.searchPluginVersions(parent.text.replace(regex, "$1"))
                completionResultSet = result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(searchResult.indexOf(element.lookupString))
                            }
                        })
                )
            } else
                searchResult = pluginsSearcher.searchPlugins(searchText)

            searchResult.forEach {
                completionResultSet.addElement(LookupElementBuilder.create(it).withIcon(AllIcons.Nodes.PpLib))
            }
        }
    }
}

