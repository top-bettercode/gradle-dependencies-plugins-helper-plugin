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

package test.gdf.maven

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

    fun test4Convert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "org.jetbrains.kotlin:kotlin-stdlib:1.2.0:abc"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <classifier>abc</classifier>
</dependency>""")
    }

    fun test3Convert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "org.jetbrains.kotlin:kotlin-stdlib:1.2.0"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
</dependency>""")
    }

    fun test2Convert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "org.jetbrains.kotlin:kotlin-stdlib"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
</dependency>""")
    }

    fun testKtsCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    compile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
</dependency>""")
    }

    fun testKtsCompileMultiConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = """    compile("org.jetbrains.kotlin:kotlin-stdlib:1.2.0")
            testCompile("org.jsoup:jsoup:1.11.2")
        """.trimMargin()

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
</dependency>
<dependency>
<groupId>org.jsoup</groupId>
<artifactId>jsoup</artifactId>
<version>1.11.2</version>
<scope>test</scope>
</dependency>""")
    }

    fun testCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    compile \"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\"\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
</dependency>""")
    }

    fun testCompile2Convert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    compile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
</dependency>""")
    }

    fun testProvidedCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    providedCompile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>""")
    }

    fun testTestCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    testCompile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>test</scope>
</dependency>""")
    }

    fun testProvidedConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    provided 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>""")
    }

    fun testKtsProvidedConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    provided(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>""")
    }

    fun testKtsProvided4Convert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    provided(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0:abc\")\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <classifier>abc</classifier>
    <scope>provided</scope>
</dependency>""")
    }

    fun testKtsProvidedCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    providedCompile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>""")
    }

    fun testKtsTestCompileConvert() {
        myFixture.configureByText("pom.xml", "<caret>")
        val toPaste = "    testCompile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")\n"

        runPasteAction(toPaste)

        myFixture.checkResult("""<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>1.2.0</version>
    <scope>test</scope>
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
