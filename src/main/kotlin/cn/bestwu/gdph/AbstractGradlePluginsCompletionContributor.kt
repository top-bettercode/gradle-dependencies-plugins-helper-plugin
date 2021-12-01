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
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

abstract class AbstractGradlePluginsCompletionContributor : CompletionContributor() {
    companion object {
        const val pluginsExtension = "plugins"
        val versionRegex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()
        val idRegex = "^id *\\(? *[\"'](.*)[\"'] *\\)?.*$".toRegex()

        fun completionSorter(searchResult: List<String>): CompletionSorter {
            return CompletionSorter.emptySorter()
                .weigh(object : LookupElementWeigher("gradleDependencyWeigher") {
                    override fun weigh(element: LookupElement): Comparable<*> {
                        return VersionComparator(searchResult.indexOf(element.lookupString))
                    }
                })
        }

        fun searchResultFix(
            searchResult: List<String>,
            allText: String,
            page: Int = 0
        ): List<String> {
            if (searchResult.isNotEmpty()) {
                return searchResultFix1(searchResult, allText, page).ifEmpty { searchResult }
            }
            return searchResult
        }

        private fun searchResultFix1(
            searchResult: List<String>,
            allText: String,
            page: Int = 0
        ): List<String> {
            if (searchResult.isNotEmpty()) {
                val result = searchResult.filter { it.startsWith(allText) }
                return result.ifEmpty {
                    searchResultFix1(
                        GradlePluginsSearcher.searchPlugins(
                            allText.substringBeforeLast(
                                "."
                            ), page + 1
                        ), allText, page + 1
                    )
                }
            }
            return searchResult
        }
    }


    override fun duringCompletion(context: CompletionInitializationContext) =
        contributorDuringCompletion(context)
}