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

package cn.bestwu.gdph

import cn.bestwu.gdph.search.ArtifactInfo
import cn.bestwu.gdph.search.GradlePluginsSearcher
import cn.bestwu.gdph.search.compareVersion
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.notification.BrowseNotificationAction
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ReflectionUtil
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.removeQuotes
import java.net.URLEncoder

internal fun split(dependency: String) = Regex(":").split(dependency)
internal fun trim(dependency: String) = removeQuotes(dependency).trim('(', ')')
val quot = URLEncoder.encode("\"", "UTF-8")!!

internal var repoIcon = IconLoader.getIcon(
        "/icons/repo.png",
        ReflectionUtil.getGrandCallerClass()!!
)

internal fun browseNotification(project: Project, text: String, url: String, title: String = text) {
    val notificationGroup = NotificationGroup.findRegisteredGroup("GradleDependencies")!!
    val notification = notificationGroup.createNotification(title, "", NotificationType.WARNING)
    notification.addAction(BrowseNotificationAction(text, url))
    Notifications.Bus.notify(notification, project)
}


internal fun show(
        project: Project,
        content: String,
        title: String = "",
        type: NotificationType = NotificationType.INFORMATION
) {
    val notificationGroup = NotificationGroup.findRegisteredGroup("GradleDependencies")!!
    val notification = notificationGroup.createNotification(title, content, type)
    Notifications.Bus.notify(notification, project)
}

internal fun MutableSet<ArtifactInfo>.addArtifactInfo(artifactInfo: ArtifactInfo) {
    val exist = find { r -> r.id == artifactInfo.id }
    if (exist != null && artifactInfo.version.isNotBlank() && exist.version.isNotBlank()) {
        if (compareVersion(exist.version, artifactInfo.version) < 0) {
            exist.version = artifactInfo.version
        }
    } else {
        add(artifactInfo)
    }
}

internal class VersionComparator(private val index: Int) : Comparable<VersionComparator> {
    override fun compareTo(other: VersionComparator): Int = this.index - other.index
}

private val stopChars = arrayOf('"', '\'')
internal var insertHandler: InsertHandler<LookupElement> = InsertHandler { context, _ ->
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
    if (idEnd >= text.length) {
        if (text.indexOf('#') != -1)
            context.document.replaceString(text.indexOf('#'), idEnd, "")
        return@InsertHandler
    }
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
        while (tailEnd < text.length && '\n' != text[tailEnd]) {
            tailEnd++
            if (tailEnd < text.length && ')' == text[tailEnd]) {
                tailStart = tailEnd + 1
            }
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
        if (text.isBlank()) {
            return
        }
        while (idEnd < text.length && !stopChars.contains(text[idEnd])) {
            idEnd++
            if (idEnd == text.length || '\n' == text[idEnd]) {
                return
            }
        }
        context.offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, idEnd)
    }
}