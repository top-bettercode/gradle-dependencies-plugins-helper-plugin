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

package test.gdf.gradle

import cn.bestwu.gdf.gradle.GradleDslToGradleKtsDslAction
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.util.io.FileUtil
import org.intellij.markdown.html.entities.Entities.map
import org.jetbrains.plugins.groovy.GroovyFileType
import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class GradleDslToGradleKtsDslActionTest : CodeInsightTestBase() {

    override fun getTestDataPath(): String {
        return this::class.java.getResource("/actions/gradleDslToGradleKtsDsl/").path
    }

    fun testGradleDslToGradleKtsDsl() {
        testGradleDslToGradleKtsDsl(GradleDslToGradleKtsDslAction())
    }

    private fun testGradleDslToGradleKtsDsl(action: AnAction) {
        val fileName = getTestName(false).substringAfter("test").decapitalize()
        val file = FileUtil.findFirstThatExist("$testDataPath/$fileName.gradle", "$fileName.gradle")!!
        myFixture.configureByText(GroovyFileType.GROOVY_FILE_TYPE, file.readText())
        val dataContext = SimpleDataContext.getSimpleContext(map, DataManager.getInstance().getDataContext(editor.component))
        val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
        action.actionPerformed(event)
        myFixture.checkResultByFile("${fileName}After.gradle.kts")
    }

}