package cn.bestwu.gdph

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.StandardPatterns.string
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl


class GradlePluginsCompletionContributor : AbstractGradlePluginsCompletionContributor() {

    init {
        extend(CompletionType.SMART,
                psiElement(PsiElement::class.java)
                        .and(psiElement().inFile(PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(".gradle"))))
                        .withParent(GrLiteral::class.java)
                        .withAncestor(10, psiElement(GrMethodCallExpressionImpl::class.java)
                                .withText(string().startsWith(pluginsExtension)))
                , CompletionPluginsCompletionProvider())

    }


    private class CompletionPluginsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
                params: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet) {
            val parent = params.position.parent?.parent?.parent
            result.stopHere()
            val isVersion = parent != null && parent.text.contains("version")
            val searchText = if (isVersion) {
                val text = parent!!.text
                text.replace(regex, "$1")
            } else
                CompletionUtil.findReferenceOrAlphanumericPrefix(params)
            val searchResult: List<String>
            var completionResultSet = result
            if (isVersion) {
                searchResult = pluginsSearcher.searchPluginVersions(searchText)
                completionResultSet = result.withRelevanceSorter(completionSorter(searchResult))
            } else {
                searchResult = pluginsSearcher.searchPlugins(searchText)
            }
            searchResult.forEach {
                val lookupElementBuilder = LookupElementBuilder.create(it).withIcon(AllIcons.Nodes.PpLib).withInsertHandler(INSERT_HANDLER)
                completionResultSet.addElement(lookupElementBuilder)
            }
        }
    }
}

