package cn.bestwu.gdph

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
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl


class GradlePluginsCompletionContributor : CompletionContributor() {
    companion object {
        val regex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()
        val kotlinRegex = "^kotlin\\(\"(.*?)\".*$".toRegex()
        val kotlinPrefix = "org.jetbrains.kotlin."
    }

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

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)

    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {

        val pluginsSearcher = GradlePluginsSearcher()

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
                completionResultSet = result.withRelevanceSorter(
                        CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                            override fun weigh(element: LookupElement): Comparable<*> {
                                return VersionComparator(searchResult.indexOf(element.lookupString))
                            }
                        })
                )
            } else {
                searchResult = pluginsSearcher.searchPlugins(searchText)
            }
            if (isKotlin && !isVersion) {
                searchResult = searchResult.filter { it.startsWith(kotlinPrefix) }
            }

            searchResult.forEach {
                val lookupElementBuilder =
                        if (isKotlin && !isVersion) {
                            LookupElementBuilder.create(it.substringAfter(kotlinPrefix)).withPresentableText(it).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(INSERT_HANDLER)
                        } else {
                            LookupElementBuilder.create(it).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(INSERT_HANDLER)
                        }
                completionResultSet.addElement(lookupElementBuilder)
            }
        }
    }
}

