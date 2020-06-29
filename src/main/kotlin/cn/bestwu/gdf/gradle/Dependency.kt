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

package cn.bestwu.gdf.gradle

import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap

class Dependency(val group: String, val name: String, val version: String?, private val classifier: Optional<String>, val configuration: String, exclusions: List<Exclusion>, val isTransitive: Boolean, extraOptions: Map<String, String>, val isOptional: Boolean) {
    val exclusions: List<Exclusion>
    val extraOptions: Map<String, String>

    init {
        this.extraOptions = ImmutableMap.copyOf(extraOptions)
        this.exclusions = ImmutableList.copyOf(exclusions)
    }

    fun getClassifier(): String? {
        return classifier.orNull()
    }

    fun hasExtraOptions(): Boolean {
        return extraOptions.isNotEmpty()
    }

    fun hasVersion(): Boolean {
        return version != null
    }

    fun hasClassifier(): Boolean {
        return classifier.isPresent
    }
}
