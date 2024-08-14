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

package test

import cn.bestwu.gdph.config.Settings
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

/**
 *
 * @author Peter Wu
 * @since
 */
abstract class CodeInsightTestBase : LightJavaCodeInsightFixtureTestCase() {
    companion object {
        const val gradleFileName = "build.gradle"
        const val gradleKtsFileName = "build.gradle.kts"
        const val caret = "<caret>"

        private const val intention = "Add specified repository to repositories"
    }

    override fun setUp() {
        super.setUp()
        Settings.getInstance().useNexus = false
    }

    protected fun completionCheckResult(fileName: String, before: String, after: (String) -> String, selectItem: String) {
        myFixture.configureByText(fileName, before)
        myFixture.complete(CompletionType.SMART, 1)
        val selectVersion = selectLookupItem(selectItem)
        val afterStr = after(selectVersion)
        println("result:\n$afterStr")
        myFixture.checkResult(afterStr)
    }

    protected fun completionCheckResult(fileName: String, before: String, after: String) {
        myFixture.configureByText(fileName, before)
        myFixture.complete(CompletionType.SMART, 1)
        myFixture.checkResult(after)
    }

    protected fun actionCheckResult(fileType: FileType, before: String, after: String, action: CodeInsightAction) {
        myFixture.configureByText(fileType, before)
        action.actionPerformedImpl(project,editor)
        myFixture.checkResult(after)
    }

//    protected fun completionCheckResultByFile(before: String, after: String, selectItem: String) {
//        myFixture.configureByFiles(before)
//        myFixture.complete(CompletionType.SMART, 1)
//        if (selectItem.isNotBlank()) {
//            selectLookupItem(selectItem)
//        }
//        myFixture.checkResultByFile(after)
//    }

    private fun selectLookupItem(selectItem: String): String {
        val lookupElements = myFixture.lookupElements
        assertNotNull("Lookup is empty", lookupElements)
        val toSelect: LookupElement? = lookupElements!!.firstOrNull {
            println(it.lookupString)
            it.lookupString.startsWith(selectItem)
        }
        assertNotNull("$selectItem not found in lookup", toSelect)
        myFixture.lookup.currentItem = toSelect
        myFixture.type(Lookup.NORMAL_SELECT_CHAR)
        return toSelect!!.lookupString.split("[#:]".toRegex()).last()
    }


    protected fun intentionCheckResult(fileName: String, given: String, expected: String) {
        myFixture.configureByText(fileName, given)
        val list = myFixture.filterAvailableIntentions(intention)
        assert(list.size == 1)
        myFixture.launchAction(list.first())
        PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
        myFixture.checkResult(expected)
    }

    protected fun intentionEmpty(fileName: String, given: String) {
        myFixture.configureByText(fileName, given)
        val list = myFixture.filterAvailableIntentions(intention)
        assert(list.size == 0)
    }

}