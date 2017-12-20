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

import com.intellij.codeInsight.editorActions.PasteHandler
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import com.intellij.util.Producer
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

class GradleToMavenDependenciesCopyPasteProcessorTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "org.apache.maven:maven-embedder:2.0"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.apache.maven</groupId>
    <artifactId>maven-embedder</artifactId>
    <version>2.0</version>
</dependency>""")
    }


    private fun runPasteAction(toPaste: String) {
        val producer = Producer<Transferable> { StringSelection(toPaste) }
        val actionManager = EditorActionManager.getInstance()
        val pasteActionHandler = actionManager.getActionHandler(ACTION_EDITOR_PASTE)
        val pasteHandler = PasteHandler(pasteActionHandler)
        object : WriteCommandAction.Simple<String>(project) {
            override fun run() {
                val component = myFixture.editor.component
                val dataContext = DataManager.getInstance().getDataContext(component)
                pasteHandler.execute(myFixture.editor, dataContext, producer)
            }
        }.execute()

    }
}
