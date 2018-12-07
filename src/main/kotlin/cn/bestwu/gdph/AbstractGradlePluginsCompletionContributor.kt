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

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher

abstract class AbstractGradlePluginsCompletionContributor : CompletionContributor() {
    companion object {
        const val pluginsExtension="plugins"
        val regex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()

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