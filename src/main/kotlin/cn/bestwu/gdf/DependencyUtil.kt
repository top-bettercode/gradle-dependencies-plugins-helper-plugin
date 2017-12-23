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

package cn.bestwu.gdf

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrString
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil
import java.util.*

object DependencyUtil {

    @JvmStatic
    fun toMap(namedArguments: Array<GrNamedArgument>): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        for (namedArgument in namedArguments) {
            val expression = namedArgument.expression
            if (namedArgument.label == null || expression == null) {
                continue
            }
            val key = namedArgument.label!!.text
            val value = removeQuotesAndUnescape(expression)
            map.put(key, value)
        }
        return map
    }

    private fun removeQuotesAndUnescape(expression: PsiElement): String {
        val quote = GrStringUtil.getStartQuote(expression.text)
        var value = GrStringUtil.removeQuotes(expression.text)
        if (isInterpolableString(quote) && !isGstring(expression)) {
            val stringWithoutQuotes = GrStringUtil.removeQuotes(expression.text)
            value = GrStringUtil.escapeAndUnescapeSymbols(stringWithoutQuotes, "", "\"$", StringBuilder())
        }
        return value
    }

    @JvmStatic
    private fun isGstring(element: PsiElement): Boolean {
        return element is GrString
    }

    @JvmStatic
    private fun isInterpolableString(quote: String): Boolean {
        return quote == GrStringUtil.DOUBLE_QUOTES || quote == GrStringUtil.TRIPLE_DOUBLE_QUOTES
    }
}