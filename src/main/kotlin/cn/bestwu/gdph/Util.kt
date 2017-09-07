package cn.bestwu.gdph

import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.removeQuotes

internal fun split(dependency: String) = Regex(":").split(dependency)
internal fun trim(dependency: String) = removeQuotes(dependency).trim('(', ')')

private val group = NotificationGroup(
        "GradleDependencies",
        NotificationDisplayType.NONE,
        true
)

internal fun supportMavenIndex(): Boolean {
    try {
        Class.forName("org.jetbrains.idea.maven.indices.MavenIndex")
        return true
    } catch (e: ClassNotFoundException) {
        return false
    }
}

internal var repoIcon = IconLoader.getIcon("/icons/repo.png")

internal fun show(project: Project, content: String, title: String = "", type: NotificationType = NotificationType.INFORMATION, listener: NotificationListener? = null) {
    val notification = group.createNotification(title, content, type, listener)
    Notifications.Bus.notify(notification, project)
}


internal class VersionComparator(private val index: Int) : Comparable<VersionComparator> {
    override fun compareTo(other: VersionComparator): Int = this.index - other.index
}

private val stopChars = arrayOf('"', '\'')
internal var INSERT_HANDLER: InsertHandler<LookupElement> = InsertHandler<LookupElement> { context, _ ->
    context.commitDocument()
    val text = context.document.text
    var idStart = context.startOffset
    do {
        idStart--
        if (idStart == -1 || '\n' == text[idStart]) {
            return@InsertHandler
        }
    } while (!stopChars.contains(text[idStart]))
    idStart++

    var idEnd = context.tailOffset
    while (!stopChars.contains(text[idEnd])) {
        idEnd++
        if (idEnd == text.length || '\n' == text[idEnd]) {
            return@InsertHandler
        }
    }
    val quote = text[idEnd]
    var lookupString = text.substring(context.startOffset, context.tailOffset)
    val list = lookupString.split(GradlePluginsSearcher.splitRule)
    if (list.size == 2) {
        lookupString = list[0]

        var tailEnd = idEnd
        var tailStart = idEnd + 1
        while ('\n' != text[tailEnd]) {
            tailEnd++
            if (')' == text[tailEnd]) {
                tailStart = tailEnd + 1
            }
            if (tailEnd == text.length)
                break
        }
        context.document.replaceString(tailStart, tailEnd, " version $quote${list[1]}$quote")
    }
    context.document.replaceString(idStart, idEnd, lookupString)
}


internal fun contributorDuringCompletion(context: CompletionInitializationContext) {
    if (context.completionType == CompletionType.SMART) {
        val offset = context.caret.offset
        val text = context.editor.document.charsSequence
        var idEnd = offset
        while (!stopChars.contains(text[idEnd])) {
            idEnd++
            if (idEnd == text.length || '\n' == text[idEnd]) {
                return
            }
        }
        context.offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd)
    }
}