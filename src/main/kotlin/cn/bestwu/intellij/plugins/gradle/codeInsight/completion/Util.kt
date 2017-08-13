package cn.bestwu.intellij.plugins.gradle.codeInsight.completion

import com.intellij.notification.*
import com.intellij.openapi.util.IconLoader


internal fun split(dependency: String) = Regex(":").split(dependency)

private val group = NotificationGroup(
        "GradleDependencies",
        NotificationDisplayType.NONE,
        true
)

internal var repoIcon = IconLoader.getIcon("/icons/repo.png")

fun show(title: String = "", content: String, type: NotificationType = NotificationType.INFORMATION, listener: NotificationListener? = null) {
    val notification = group.createNotification(title, content, type, listener)
    Notifications.Bus.notify(notification)
}

class ArtifactInfo(groupId: String, artifactId: String, version: String = "", repo: String = "", owner: String = "") {
    val groupId: String = groupId.trim()
    val artifactId: String = artifactId.trim()
    val version: String = version.trim()
    val repo: String = repo.trim()
    val owner: String = owner.trim()
    val id: String
    val presentableText: String

    override fun toString(): String {
        return "ArtifactInfo(groupId='$groupId', artifactId='$artifactId', version='$version', id='$id')"
    }

    fun type() = "$repo${if ("jcenter" != repo) " By $owner" else ""}"

    init {
        this.id = "${this.groupId}:${this.artifactId}"
        this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
    }
}

class SearchParam {
    val id: String
    val q: String

    constructor(text: String) {
        val list = split(text)
        if (list.size in (2..3)) {
            val groupId = list[0].trim()
            val artifactId = list[1].trim()
            this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
            this.q = "g=*$groupId*${if (artifactId.isEmpty()) "" else "&a=*$artifactId*"}"
        } else {
            this.id = text.trim()
            this.q = "q=*${text.trim()}*"
        }
    }

    constructor(groupIdParam: String, artifactIdParam: String) {
        val groupId = groupIdParam.trim()
        val artifactId = artifactIdParam.trim()
        this.id = "$groupId${if (artifactId.isEmpty()) "" else ":$artifactId"}"
        this.q = "g=*$groupId*${if (artifactId.isEmpty()) "" else "&a=*$artifactId*"}"
    }

    override fun toString(): String {
        return "SearchParam(id='$id', q='$q')"
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