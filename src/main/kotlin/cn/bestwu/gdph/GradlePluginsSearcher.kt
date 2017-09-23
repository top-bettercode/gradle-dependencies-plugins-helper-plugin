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

import org.jsoup.Jsoup

class GradlePluginsSearcher {
    companion object {
        private val pluginsCaches = HashMap<String, List<String>>()
        private val pluginVersionsCaches = HashMap<String, List<String>>()
        val splitRule = "#"
    }

    fun searchPlugins(text: String): List<String> {
        var result = pluginsCaches[text]
        if (result != null) {
            return result
        }
        val elements = Jsoup.connect("https://plugins.gradle.org/search?term=${text.trim()}").get().select("#search-results tbody tr")
        result = elements.mapNotNull {
            val pluginId = it.select(".plugin-id a").text()
            if (pluginId.isEmpty()) {
                return@mapNotNull null
            }
            pluginId + splitRule + it.select(".latest-version").text()
        }
        if (result.isNotEmpty()) {
            pluginsCaches.put(text, result)
        }
        return result
    }

    fun searchPluginVersions(text: String): List<String> {
        var result = pluginVersionsCaches[text]
        if (result == null) {
            result = ArrayList<String>()
        } else {
            return result
        }
        val plugin = Jsoup.connect("https://plugins.gradle.org/plugin/${text.trim()}").get()
        result.add(plugin.select(".version-info h3").text().replace("^Version (.*) \\(latest\\)$".toRegex(), "$1"))
        val elements = plugin.select(".other-versions li")
        elements.mapTo(result) { it.select("a").text() }
        if (result.isNotEmpty()) {
            pluginVersionsCaches.put(text, result)
        }
        return result
    }

}

