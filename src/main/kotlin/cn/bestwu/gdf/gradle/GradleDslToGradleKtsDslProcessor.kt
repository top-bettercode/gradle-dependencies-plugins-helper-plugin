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

import com.intellij.internal.statistic.UsageTrigger
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.ui.UsageViewDescriptorAdapter
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle

class GradleDslToGradleKtsDslProcessor(project: Project, vararg val myFiles: GroovyFile) : BaseRefactoringProcessor(project) {

    override fun createUsageViewDescriptor(usages: Array<UsageInfo>): UsageViewDescriptor {
        return object : UsageViewDescriptorAdapter() {
            override fun getElements(): Array<out GroovyFile> {
                return myFiles
            }

            override fun getProcessedElementsHeader(): String {
                return GroovyRefactoringBundle.message("files.to.be.converted")
            }
        }
    }

    override fun findUsages(): Array<UsageInfo> {
        return UsageInfo.EMPTY_ARRAY
    }

    //private static String
    override fun performRefactoring(usages: Array<UsageInfo>) {
        UsageTrigger.trigger("refactoring.convert.gradleDslToGradleKtsDsl")

        for (file in myFiles) {
            var text = file.text

            text = text.replace("'".toRegex(), "\"")
            text = text.replace("^ *id[ (]\"([A-Za-z.\\-]*)\"[)]? *$".toRegex(RegexOption.MULTILINE), "    $1")
            text = text.replace("^ *id[ (]\"([A-Za-z.\\-]*)\"[)]? *(version.*)$".toRegex(RegexOption.MULTILINE), "    id(\"\$1\") $2")
            text = text.replace("^(group|version|description) \"(.*)\" *$".toRegex(RegexOption.MULTILINE), "$1 = \"$2\"")

            var javaOptions = "tasks.withType(JavaCompile::class.java) {\n"
            val sourceRegex = "^sourceCompatibility = (\\d.\\d)$".toRegex(RegexOption.MULTILINE)
            val sourceMatchResult = sourceRegex.find(text)
            if (sourceMatchResult != null) {
                javaOptions += "    sourceCompatibility = \"${sourceMatchResult.groupValues[1]}\"\n"
            }
            val targetRegex = "^targetCompatibility = (\\d.\\d)$".toRegex(RegexOption.MULTILINE)
            val targetMatchResult = targetRegex.find(text)
            if (targetMatchResult != null) {
                javaOptions += "    targetCompatibility = \"${targetMatchResult.groupValues[1]}\"\n"
            }
            val encodingRegex = "^.*options\\*\\.encoding = \"(.*?)\"$".toRegex(RegexOption.MULTILINE)
            val encodingMatchResult = encodingRegex.find(text)
            if (encodingMatchResult != null) {
                javaOptions += "    options.encoding = \"${encodingMatchResult.groupValues[1]}\"\n"
            }
            javaOptions += "}"

            text = text.replace(sourceRegex, javaOptions(text, javaOptions))
            text = text.replace(targetRegex, javaOptions(text, javaOptions))
            text = text.replace(encodingRegex, javaOptions(text, javaOptions))

            text = text.replace("^( *exclude.*?)group: ?(.*?)$".toRegex(RegexOption.MULTILINE), "$1group = $2")
            text = text.replace("^( *exclude.*?)module: ?(.*?)$".toRegex(RegexOption.MULTILINE), "$1module = $2")
            text = text.replace("^( *maven) \\{ url (.*) }$".toRegex(RegexOption.MULTILINE), "$1($2)")

            val document = PsiDocumentManager.getInstance(myProject).getDocument(file)
            LOG.assertTrue(document != null)
            document!!.setText(text)
            PsiDocumentManager.getInstance(myProject).commitDocument(document)
            val fileName = getNewFileName(file)
            val newFile: PsiElement
            try {
                newFile = file.setName(fileName)
            } catch (e: IncorrectOperationException) {
                ApplicationManager.getApplication().invokeLater { Messages.showMessageDialog(myProject, e.message, RefactoringBundle.message("error.title"), Messages.getErrorIcon()) }
                return
            }

            doPostProcessing(newFile)
        }
    }

    private fun javaOptions(text: String, javaOptions: String): String {
        return if (text.contains("tasks.withType(JavaCompile::class.java) {")) {
            ""
        } else
            javaOptions
    }

    private fun doPostProcessing(newFile: PsiElement) {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        if (newFile !is KotlinFileType) {
            LOG.info(".kts is not assigned to Kotlin file type")
            return
        }
        CodeStyleManager.getInstance(myProject).reformat(newFile)
    }

    override fun getCommandName(): String {
        return "converting files to Gradle kts dsl"
    }

    companion object {
        private val LOG = Logger.getInstance(GradleDslToGradleKtsDslProcessor::class.java)

        private fun getNewFileName(file: GroovyFile): String {
            val dir = file.containingDirectory
            LOG.assertTrue(dir != null)


            val files = dir!!.files
            val fileNames = mutableSetOf<String>()
            for (psiFile in files) {
                fileNames.add(psiFile.name)
            }
            var fileName = file.name + ".kts"
            val index = 1
            while (fileNames.contains(fileName)) {
                fileName = file.name + index + ".kts"
            }
            return fileName
        }
    }
}