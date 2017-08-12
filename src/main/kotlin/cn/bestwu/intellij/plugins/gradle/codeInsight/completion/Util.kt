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

class ArtifactInfo {
    val groupId: String
    val artifactId: String
    val version: String
    val repo: String
    val owner: String
    val id: String
    val presentableText: String
    val q: String

    constructor(groupId: String, artifactId: String, version: String = "", repo: String = "", owner: String = "") {
        this.groupId = groupId.trim()
        this.artifactId = artifactId.trim()
        this.version = version.trim()
        this.repo = repo.trim()
        this.owner = owner.trim()
        this.id = "${this.groupId}:${this.artifactId}"
        this.presentableText = "${this.id}${if (this.version.isEmpty()) "" else ":${this.version}"}"
        this.q = "g=${this.groupId}&a=${this.artifactId}"
    }

    constructor(src: String) {
        this.repo = ""
        this.owner = ""
        this.presentableText = ""
        val list = split(src)
        if (list.size in (2..3)) {
            this.groupId = list[0].trim()
            this.artifactId = list[1].trim()
            this.version = if (list.size == 3) list[2].trim() else ""
            this.id = "${this.groupId}${if (this.artifactId.isEmpty()) "" else ":${this.artifactId}"}"
            this.q = "g=*${this.groupId}*${if (this.artifactId.isEmpty()) "" else "&a=*${this.artifactId}*"}"
        } else {
            this.groupId = ""
            this.artifactId = ""
            this.version = ""
            this.id = src.trim()
            this.q = "q=*${src.trim()}*"
        }
    }

    override fun toString(): String {
        return "ArtifactInfo(groupId='$groupId', artifactId='$artifactId', version='$version', id='$id', src='$q')"
    }

    fun type() = "$repo${if ("jcenter" != repo) " By $owner" else ""}"
}

class VersionComparator(val index: Int) : Comparable<VersionComparator> {
    override fun compareTo(other: VersionComparator): Int = this.index - other.index
}

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
        try {
            val v2 = Integer.parseInt(version2s[i])
            val v1 = Integer.parseInt(version1s[i])
            if (v2 > v1) {
                return -1
            } else if (v2 < v1) {
                return 1
            }
        } catch (e: NumberFormatException) {
            val result = version1s[i].compareTo(version2s[i])
            if (result != 0) {
                return result
            }
        }

        // 相等 比较下一组值
    }

    if (vl)
        return -1
    else if (version1s.size > version2s.size)
        return 1
    return 0
}