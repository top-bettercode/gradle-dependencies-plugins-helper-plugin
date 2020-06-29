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

import com.google.common.base.*
import com.google.common.base.Objects
import com.google.common.base.Optional
import com.google.common.collect.ComparisonChain
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables.transform
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import java.util.*
import java.util.regex.Pattern

class Coordinate(private val group: Optional<String>, val name: String, val version: Optional<String>, private val classifier: Optional<String>, private val extension: Optional<String>) : Comparable<Coordinate> {

    fun toMapNotation(quote: String): String {
        return ON_COMMA_SPACE_JOINER.join(transform(toMap().entries) { mapEntry -> String.format("%s: %s%s%s", mapEntry!!.key, quote, mapEntry.value, quote) })
    }

    fun toStringNotation(): String {
        val stringBuilder = StringBuilder()
        if (group.isPresent) {
            stringBuilder.append(group.get())
        }
        stringBuilder.append(':')
        stringBuilder.append(name)
        appendIfPresent(stringBuilder, ':', version)
        if (!version.isPresent && classifier.isPresent) {
            stringBuilder.append(':')
        }
        appendIfPresent(stringBuilder, ':', classifier)
        appendIfPresent(stringBuilder, '@', extension)
        return stringBuilder.toString()
    }

    private fun appendIfPresent(stringBuilder: StringBuilder, separator: Char, optionalValue: Optional<String>) {
        if (optionalValue.isPresent) {
            stringBuilder.append(separator)
            stringBuilder.append(optionalValue.get())
        }
    }

    private fun toMap(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        putIfPresent(map, group, GROUP_KEY)
        map[NAME_KEY] = name
        putIfPresent(map, version, VERSION_KEY)
        putIfPresent(map, classifier, CLASSIFIER_KEY)
        putIfPresent(map, extension, EXT_KEY)
        return map
    }

    private fun putIfPresent(map: MutableMap<String, String>, value: Optional<String>, key: String) {
        if (value.isPresent) {
            map[key] = value.get()
        }
    }

    override fun hashCode(): Int {
        return Objects.hashCode(group, name, version, classifier, extension)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val coordinate = other as Coordinate?
        return (Objects.equal(this.group, coordinate!!.group)
                && Objects.equal(this.name, coordinate.name)
                && Objects.equal(this.version, coordinate.version)
                && Objects.equal(this.classifier, coordinate.classifier)
                && Objects.equal(this.extension, coordinate.extension))
    }

    override fun toString(): String {
        return ("Coordinate{"
                + "group=" + group
                + ", name='" + name + '\''
                + ", version=" + version
                + ", classifier=" + classifier
                + ", extension=" + extension
                + '}')
    }

    override fun compareTo(other: Coordinate): Int {
        return ComparisonChain.start()
                .compare(this.group, other.group, OPTIONAL_COMPARATOR)
                .compare(this.name, other.name)
                .compare(this.version, other.version, OPTIONAL_COMPARATOR)
                .compare(this.classifier, other.classifier, OPTIONAL_COMPARATOR)
                .compare(this.extension, other.extension, OPTIONAL_COMPARATOR)
                .result()
    }

    private class NaturalAbsentFirstOptionalOrdering<T : Comparable<T>> : Comparator<Optional<T>> {

        override fun compare(o1: Optional<T>, o2: Optional<T>): Int {
            if (o1.isPresent && o2.isPresent) {
                return o1.get().compareTo(o2.get())
            }
            if (!o1.isPresent && !o2.isPresent) {
                return 0
            }
            return if (o1.isPresent) {
                1
            } else {
                -1
            }
        }
    }

    class CoordinateBuilder constructor(private var name: String) {
        private var group = Optional.absent<String>()
        private var version = Optional.absent<String>()
        private var classifier = Optional.absent<String>()
        private var extension = Optional.absent<String>()

        fun withGroup(group: String): CoordinateBuilder {
            this.group = optionalOf(group)
            return this
        }

        private fun optionalOf(group: String): Optional<String> {
            return if (group.isEmpty()) Optional.absent() else Optional.of(group)
        }

        fun withVersion(version: String): CoordinateBuilder {
            this.version = optionalOf(version)
            return this
        }

        fun withClassifier(classifier: String): CoordinateBuilder {
            this.classifier = optionalOf(classifier)
            return this
        }

        fun withExtension(extension: String): CoordinateBuilder {
            this.extension = optionalOf(extension)
            return this
        }

        fun build(): Coordinate {
            return Coordinate(group, name, version, classifier, extension)
        }

    }


    companion object {
        private const val NAME_KEY = "name"
        private const val GROUP_KEY = "group"
        private const val VERSION_KEY = "version"
        private const val CLASSIFIER_KEY = "classifier"
        private const val EXT_KEY = "ext"
        private val ALL_KEYS = ImmutableSet
                .of(GROUP_KEY, NAME_KEY, VERSION_KEY, CLASSIFIER_KEY, EXT_KEY)
        private val REQUIRED_KEYS = ImmutableSet.of(GROUP_KEY, NAME_KEY)
        private val ON_SEMICOLON_SPLITTER = Splitter.onPattern(":").limit(4)
        private val ON_COMMA_SPACE_JOINER = Joiner.on(", ")
        private val OPTIONAL_COMPARATOR = NaturalAbsentFirstOptionalOrdering<String>()

        fun parse(stringNotation: String): Coordinate {
            Preconditions.checkArgument(stringNotation.trim { it <= ' ' }.isNotEmpty(), "Coordinate is empty!")
            val parts = Lists.newArrayList(ON_SEMICOLON_SPLITTER.split(stringNotation))
            val numberOfParts = parts.size
            Preconditions.checkArgument(numberOfParts > 1, "Cannot parse coordinate!")
            val coordinateBuilder = CoordinateBuilder(getNotEmptyAt(parts, 1)).withGroup(getAt(parts, 0))
            if (numberOfParts >= 3) {
                coordinateBuilder.withVersion(getAt(parts, 2))
            }
            if (numberOfParts == 4) {
                coordinateBuilder.withClassifier(getAt(parts, 3))
            }
            val last = parts[parts.size - 1]
            if (last.contains("@")) {
                coordinateBuilder.withExtension(last.split("@".toRegex(), 2).toTypedArray()[1])
            }
            return coordinateBuilder.build()
        }

        fun isStringNotationCoordinate(stringNotation: String): Boolean {
            return Pattern.compile("[^:\\s]*:[^:\\s]+(:[^:\\s]*)?(:[^:\\s]+)?(@[^:\\s]+)?").matcher(stringNotation).matches()
        }

        private fun getNotEmptyAt(list: List<String>, index: Int): String {
            Preconditions.checkArgument(list[index].trim { it <= ' ' }.isNotEmpty(), "Cannot parse coordinate!")
            return getAt(list, index)
        }

        private fun getAt(list: List<String>, index: Int): String {
            val element = list[index]
            return if (isLast(list, index) && element.contains("@")) {
                element.split("@".toRegex(), 2).toTypedArray()[0]
            } else element
        }

        private fun isLast(list: List<String>, index: Int): Boolean {
            return list.size == index + 1
        }

        fun fromMap(map: Map<String, String>): Coordinate {
            val name = map[NAME_KEY] ?: error("'name' element is required. ")
            val coordinateBuilder = CoordinateBuilder(name)
            val group = map[GROUP_KEY]
            if (group != null && group.isNotBlank()) {
                coordinateBuilder.withGroup(group)
            }
            val version = map[VERSION_KEY]
            if (version != null && version.isNotBlank()) {
                coordinateBuilder.withVersion(version)
            }
            val classifier = map[CLASSIFIER_KEY]
            if (classifier != null && classifier.isNotBlank()) {
                coordinateBuilder.withClassifier(classifier)
            }
            val ext = map[EXT_KEY]
            if (ext != null && ext.isNotBlank()) {
                coordinateBuilder.withExtension(ext)
            }
            return coordinateBuilder.build()
        }

        fun isValidMap(map: Map<String, String>): Boolean {
            return Sets.difference(map.keys, ALL_KEYS).isEmpty() && map.keys.containsAll(REQUIRED_KEYS)
        }
    }
}
