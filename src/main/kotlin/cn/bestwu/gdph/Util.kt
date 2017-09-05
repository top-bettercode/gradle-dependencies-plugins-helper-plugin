package cn.bestwu.gdph

import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader

internal fun split(dependency: String) = Regex(":").split(dependency)
internal fun trim(dependency: String) = dependency.trim('"', '\'', '(', ')')

private val group = NotificationGroup(
        "GradleDependencies",
        NotificationDisplayType.NONE,
        true
)

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
    val lookupString = text.substring(context.startOffset, context.tailOffset)
    var idStart = context.startOffset
    do {
        idStart--
        if ('\n' == text[idStart] || idStart == -1) {
            return@InsertHandler
        }
    } while (!stopChars.contains(text[idStart]))
    idStart++

    var idEnd = context.tailOffset
    while (!stopChars.contains(text[idEnd])) {
        idEnd++
        if ('\n' == text[idEnd] || idEnd == text.length) {
            return@InsertHandler
        }
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
            if ('\n' == text[idEnd] || idEnd == text.length) {
                return
            }
        }
        context.offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd)
    }
}