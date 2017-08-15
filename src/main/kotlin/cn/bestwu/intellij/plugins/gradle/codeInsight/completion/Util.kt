package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
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

fun show(project: Project, content: String, title: String = "", type: NotificationType = NotificationType.INFORMATION, listener: NotificationListener? = null) {
    val notification = group.createNotification(title, content, type, listener)
    Notifications.Bus.notify(notification, project)
}

class ArtifactInfo(groupId: String, artifactId: String, version: String = "", repo: String = "", owner: String = "") {
    val groupId: String = groupId.trim()
    val artifactId: String = artifactId.trim()
    var version: String = version.trim()
        set(value) {
            field = value
            this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
        }
    val repo: String = repo.trim()
    val owner: String = owner.trim()
    val id: String
    var presentableText: String

    override fun toString(): String {
        return "ArtifactInfo(groupId='$groupId', artifactId='$artifactId', version='$version', id='$id')"
    }

    fun type() = "$repo${if ("jcenter" != repo && "mavenCentral" != repo) " By $owner" else ""}"

    init {
        this.id = "${this.groupId}:${this.artifactId}"
        this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
    }
}

class SearchParam {
    val id: String
    val q: String
    val mq: String
    val nq: String
    val advancedSearch: String
    val failContent: String

    constructor(text: String) {
        if (text.startsWith("c:", true)) {
            id = text.substringAfter("c:").trim()
            q = text.trim()
            mq = text.trim()
            nq = text.trim()
            advancedSearch = "c"
            failContent = "<a href='http://search.maven.org/#search|gav|1|c:\"$id\"'>search in mavenCentral</a>"
        } else if (text.startsWith("fc:", true)) {
            id = text.substringAfter("fc:").trim()
            q = text.trim()
            mq = text.trim()
            nq = text.trim()
            advancedSearch = "fc"
            failContent = "<a href='http://search.maven.org/#search|gav|1|fc:\"$id\"'>search in mavenCentral</a>"
        } else {
            advancedSearch = ""
            val list = split(text)
            if (list.size in (2..3)) {
                val groupId = list[0].trim()
                val artifactId = list[1].trim()
                this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
                this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
                this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
                this.nq = "g=$groupId${if (artifactId.isEmpty()) "" else "&a=$artifactId"}"
            } else {
                this.id = text.trim()
                this.q = "q=${text.trim()}*"
                this.mq = "a:\"${text.trim()}\""
                this.nq = "q=${text.trim()}"
            }
            failContent = "<a href='https://bintray.com/search?query=$id'>search in jcenter</a>"
        }
    }

    constructor(groupIdParam: String, artifactIdParam: String) {
        advancedSearch = ""
        val groupId = groupIdParam.trim()
        val artifactId = artifactIdParam.trim()
        this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
        this.q = "g=$groupId${if (artifactId.isEmpty()) "*" else "&a=$artifactId*"}"
        this.mq = "g:\"$groupId\"${if (artifactId.isEmpty()) "" else "+AND+a:\"$artifactId\""}"
        this.nq = "g=$groupId${if (artifactId.isEmpty()) "" else "&a=$artifactId"}"
        failContent = "<a href='https://bintray.com/search?query=$id'>search in jcenter</a>"
    }

    override fun toString(): String {
        return "SearchParam(id='$id', q='$q', advancedSearch='$advancedSearch', failContent='$failContent')"
    }


}

class VersionComparator(val index: Int) : Comparable<VersionComparator> {
    override fun compareTo(other: VersionComparator): Int = this.index - other.index
}

private val versionTails = arrayOf("SNAPSHOTS", "ALPHA", "BETA", "M", "RC", "RELEASE")
private val versionTailRegex = "^(.*?)\\d*$".toRegex()

/**
 * 比较版本信息

 * @param version1 版本1
 * *
 * @param version2 版本2
 * *
 * @return int
 */
fun compareVersion(version1: String, version2: String): Int {
    if (version1 == version2) {
        return 0
    }
    val separator = "[.-]"
    val version1s = version1.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val version2s = version2.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    val vl = version1s.size < version2s.size
    val length = if (vl) version1s.size else version2s.size

    for (i in 0..length - 1) {
        val v2 = version2s[i].toIntOrNull() ?: versionTails.indexOf(version2s[i].replace(versionTailRegex, "$1").toUpperCase())
        val v1 = version1s[i].toIntOrNull() ?: versionTails.indexOf(version1s[i].replace(versionTailRegex, "$1").toUpperCase())
        if (v1 == -1 || v2 == -1) {
            val result = version1s[i].compareTo(version2s[i])
            if (result != 0) {
                return result
            }
        }
        if (v2 > v1) {
            return -1
        } else if (v2 < v1) {
            return 1
        }
        // 相等 比较下一组值
    }

    if (vl)
        return -1
    else if (version1s.size > version2s.size)
        return 1
    return 0
}

fun contributorBeforeCompletion(context: CompletionInitializationContext) {
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

fun contributorDuringCompletion(context: CompletionInitializationContext) {
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