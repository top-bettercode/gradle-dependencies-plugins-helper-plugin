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

package cn.bestwu.gdph.kotlin

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiFileFactory
import com.intellij.util.IncorrectOperationException
import com.intellij.util.ReflectionUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import javax.swing.Icon

/**
 *
 * @author Peter Wu
 * @since
 */

object KotlinScriptFileType : LanguageFileType(KotlinLanguage.INSTANCE) {
    const val extension = "gradle.kts"

    override fun getName(): String {
        return "Kotlin"
    }

    override fun getDescription(): String {
        return this.name
    }

    override fun getDefaultExtension(): String {
        return extension
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon(
            "/org/jetbrains/kotlin/idea/icons/kotlin_file.png",
            ReflectionUtil.getGrandCallerClass()!!
        )
    }
}

class KtsPsiElementFactory(val project: Project) {

    fun createStatementFromText(text: String): KtExpression {
        val file = createKtFile(text)
        val statements = file.script?.blockExpression?.statements!!
        if (statements.size != 1) {
            throw IncorrectOperationException("count = " + statements.size + ", " + text)
        }
        if (statements[0] !is KtExpression) {
            throw IncorrectOperationException("type = " + statements[0].javaClass.name + ", " + text)
        }
        return statements[0]
    }

    private fun createKtFile(text: String): KtFile {
        val stamp = System.currentTimeMillis()
        val factory = PsiFileFactory.getInstance(project)
        return factory.createFileFromText(
            "DUMMY__1234567890_DUMMYYYYYY___.${KotlinScriptFileType.extension}",
            KotlinScriptFileType,
            text,
            stamp,
            false
        ) as KtFile
    }

}