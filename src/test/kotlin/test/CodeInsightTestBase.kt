package test

import cn.bestwu.gdph.config.Settings
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import junit.framework.TestCase

/**
 *
 * @author Peter Wu
 * @since
 */
open class CodeInsightTestBase : LightCodeInsightFixtureTestCase() {
    companion object {
        val gradleFileName = "build.gradle"
        val gradleKtsFileName = "build.gradle.kts"
        val kotlinVersion = "1.1.4-3"
        val feignVersion = "8.18.0"
        val caret = "<caret>"

        private val intention = "Add specified repository to repositories"
    }

    override fun setUp() {
        super.setUp()
        Settings.getInstance().useNexus = false
    }

    protected fun completionCheckResult(fileName: String, before: String, after: String, selectItem: String) {
        myFixture.configureByText(fileName, before)
        myFixture.complete(CompletionType.SMART, 1)
        selectLookupItem(selectItem)
        myFixture.checkResult(after)
    }

    protected fun completionCheckResultByFile(before: String, after: String, selectItem: String) {
        myFixture.configureByFiles(before)
        myFixture.complete(CompletionType.SMART, 1)
        selectLookupItem(selectItem)
        myFixture.checkResultByFile(after)
    }

    private fun selectLookupItem(selectItem: String) {
        val lookupElements = myFixture.lookupElements
        TestCase.assertNotNull("Lookup is empty", lookupElements)
        val toSelect: LookupElement? = lookupElements!!.firstOrNull {
            println(it.lookupString)
            selectItem == it.lookupString
        }
        TestCase.assertNotNull(selectItem + " not found in lookup", toSelect)
        myFixture.lookup.currentItem = toSelect
        myFixture.type(Lookup.NORMAL_SELECT_CHAR)
    }


    protected fun intentionCheckResult(fileName: String, given: String, expected: String) {
        myFixture.configureByText(fileName, given)
        val list = myFixture.filterAvailableIntentions(intention)
        assert(list.size == 1)
        println("An intention '$intention' should be applicable to: \n$given\n")
        myFixture.launchAction(list.first())
        PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
        myFixture.checkResult(expected)
    }

}