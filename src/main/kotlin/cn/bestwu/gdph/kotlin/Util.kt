package cn.bestwu.gdph.kotlin

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiFileFactory
import com.intellij.util.IncorrectOperationException
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
    val extension = "gradle.kts"

    private val myIcon = object : NotNullLazyValue<Icon>() {
        override fun compute(): Icon {
            return IconLoader.getIcon("/org/jetbrains/kotlin/idea/icons/kotlin_file.png")
        }
    }

    override fun getName(): String {
        return "Kotlin"
    }

    override fun getDescription(): String {
        return this.name
    }

    override fun getDefaultExtension(): String {
        return extension
    }

    override fun getIcon(): Icon? {
        return this.myIcon.value
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
            throw IncorrectOperationException("type = " + statements[0].javaClass.getName() + ", " + text)
        }
        return statements[0]
    }

    private fun createKtFile(text: String): KtFile {
        val stamp = System.currentTimeMillis()
        val factory = PsiFileFactory.getInstance(project)
        return factory.createFileFromText("DUMMY__1234567890_DUMMYYYYYY___.${KotlinScriptFileType.extension}", KotlinScriptFileType, text, stamp, false) as KtFile
    }

}