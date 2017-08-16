package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.jetbrains.idea.maven.model.MavenRemoteRepository

internal fun String.toMavenRemoteRepository() = MavenRemoteRepository(this, null, this, null, null, null)

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


internal class VersionComparator(val index: Int) : Comparable<VersionComparator> {
    override fun compareTo(other: VersionComparator): Int = this.index - other.index
}


internal fun contributorBeforeCompletion(context: CompletionInitializationContext) {
    if (context.completionType == CompletionType.SMART) {
        val offset = context.caret.offset
        val text = context.editor.document.text
        var idStart = offset
        do {
            idStart--
            if ('\n' == text[idStart] || idStart == -1) {
                return
            }
        } while ('"' != text[idStart] && '\'' != text[idStart])
        idStart++
        if (text.substring(idStart, idStart + 2) == "c:" || text.substring(idStart, idStart + 3) == "fc:")
            context.caret.moveToOffset(idStart)
    }
}

internal fun contributorDuringCompletion(context: CompletionInitializationContext) {
    if (context.completionType == CompletionType.SMART) {
        val offset = context.caret.offset
        val text = context.editor.document.charsSequence
        var idEnd = offset
        while ('"' != text[idEnd] && '\'' != text[idEnd]) {
            idEnd++
            if ('\n' == text[idEnd] || idEnd == text.length) {
                return
            }
        }
        context.offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd)
    }
}