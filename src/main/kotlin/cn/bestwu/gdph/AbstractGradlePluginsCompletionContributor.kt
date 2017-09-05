package cn.bestwu.gdph

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

abstract class AbstractGradlePluginsCompletionContributor : CompletionContributor() {
    companion object {
        val pluginsExtension="plugins"
        val regex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()
        val pluginsSearcher = GradlePluginsSearcher()

        fun completionSorter(searchResult: List<String>): CompletionSorter {
            return CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                override fun weigh(element: LookupElement): Comparable<*> {
                    return VersionComparator(searchResult.indexOf(element.lookupString))
                }
            })
        }
    }

    override fun duringCompletion(context: CompletionInitializationContext) = contributorDuringCompletion(context)
}