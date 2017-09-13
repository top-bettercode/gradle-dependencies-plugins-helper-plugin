
import cn.bestwu.gdph.ArtifactInfo
import cn.bestwu.gdph.GradleArtifactSearcher
import cn.bestwu.gdph.kotlin.GradleKtsPluginsCompletionContributor
import groovy.json.JsonSlurper
import org.jsoup.Jsoup
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 *
 * @author Peter Wu
 * @since
 */
class ParseResultTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun parseBintray() {
        val result = LinkedHashSet<String>()
        val text = "[{\"name\":\"io.fabric8:spring-cloud-kubernetes-ribbon\",\"repo\":\"jcenter\",\"owner\":\"bintray\",\"desc\":null,\"system_ids\":[\"io.fabric8:spring-cloud-kubernetes-ribbon\"],\"versions\":[\"0.1.6\",\"0.1.5\",\"0.1.4\",\"0.1.3\",\"0.1.1\",\"0.1.0\",\"0.0.16\",\"0.0.15\",\"0.0.14\",\"0.0.13\",\"0.0.11\",\"0.0.10\",\"0.0.9\",\"0.0.8\",\"0.0.7\",\"0.0.6\",\"0.0.5\",\"0.0.4\",\"0.0.3\",\"0.0.2\"],\"latest_version\":\"0.1.6\"}]"
        val json = JsonSlurper().parseText(text) as List<Map<*, *>>
        for (any in json) {
            if ("io.fabric8:spring-cloud-kubernetes-ribbon" == any["name"] as String) {
                ((any["versions"] as List<String>).mapTo(result) { "${any["name"]}:$it" })
            } else {
                result.add(any["name"] as String)
            }
        }

        println(result)
    }

    @Test
    fun parsePlugins() {
        val elements = Jsoup.connect("https://plugins.gradle.org/search?term=org.jetbrains").get().select("#search-results tbody tr")

        for ((key, value) in elements.map { Pair(it.select(".plugin-id a").text(), it.select(".latest-version").text()) }) {
            println("$key,$value")
        }
    }

    @Test
    fun parsePluginsVersion() {
        val plugin = Jsoup.connect("https://plugins.gradle.org/plugin/org.springframework.boot").get()
        println(plugin.select(".version-info h3").text().replace("^Version (.*) \\(latest\\)$".toRegex(), "$1"))
        val elements = plugin.select(".other-versions li")

        for (version in elements.map { it.select("a").text() }) {
            println(version)
        }
    }


    @Test
    fun regexVersion() {
        val regex = "^id *\\(? *[\"'](.*)[\"'] *\\)? *version.*$".toRegex()
        println("id(\"org.springframework.boot\") version( \"1.5.6.RELEASE\")".replace(regex, "$1"))
        println("id \"org.springframework.boot\" version( \"1.5.6.RELEASE\")".replace(regex, "$1"))
        println("id \"org.springframework.boot\" version \"1.5.6.RELEASE\"".replace(regex, "$1"))
        println("id('org.springframework.boot') version( '1.5.6.RELEASE')".replace(regex, "$1"))
        println("id 'org.springframework.boot' version( '1.5.6.RELEASE')".replace(regex, "$1"))
        println("id 'org.springframework.boot' version '1.5.6.RELEASE'".replace(regex, "$1"))
        println("id(\"org.springframework.boot\") version (\"1.5.6.RELEASE\")".replace(regex, "$1"))
        println("id(\"org.springframework.boot\") version \"1.5.6.RELEASE\"".replace(regex, "$1"))
    }

    @Test
    fun GrRegex() {
        println("org.springframework.boot:sprIntellijIdeaRulezzz ing-boot-dependencies".substringBefore("IntellijIdeaRulezzz "))
    }

    @Test
    fun sortVersion() {
        val versions = arrayListOf(
                "1.3.0.ALPHA",
                "1.3.0.SNAPSHOTS",
                "1.3.0.RELEASE",
                "1.3.0.BETA",
                "1.3.0.RC1",
                "1.3.0.RC2",
                "1.3.0.M3",
                "1.3.0.M4",
                "1.3.0.M2",
                "1.3.0.M5",
                "1.3.0.M1"
        )
        versions.sortWith(kotlin.Comparator { o1, o2 ->
            GradleArtifactSearcher.compareVersion(o1, o2)
        })
        println(versions)
    }

    @Test
    fun mavenRegex() {

        Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",").findAll(File(this::class.java.getResource("/result.json").path).readText()).forEach {
            val artifactInfo = ArtifactInfo(it.groupValues[1], it.groupValues[2], it.groupValues[3], "mavenCentral", "Apache")
            println(artifactInfo.toString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun searchInNexus() {
//        val url = "http://127.0.0.1:8083/nexus/service/local/lucene/search?repositoryId=central&cn=org.junit.Test"
//        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&cn=org.junit.Test"
        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&g=org*"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/json")
        val stream = connection.inputStream ?: return
//        println(stream.bufferedReader().readText())
        val jsonResult = (JsonSlurper().parse(stream) as Map<*, *>)["data"] as List<Map<*, *>>
        jsonResult.forEach {
            val artifactInfo = ArtifactInfo(it["groupId"] as String, it["artifactId"] as String, it["version"] as String, "mavenCentral", "Apache")
            println(artifactInfo)
        }
        println(jsonResult.size)
    }

    @Test
    fun local() {
        println(Locale.getDefault())
        println(Locale.CHINESE)
        println(Locale.getDefault()== Locale.CHINA)
        println(Locale.getDefault()== Locale.CHINESE)
        println(Locale.getDefault()== Locale.SIMPLIFIED_CHINESE)
    }

    @Test
    fun testkotlinRegex() {
        println("kotlin(\"jvm\", \"1.1.4\")".replace(GradleKtsPluginsCompletionContributor.kotlinRegex,"${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
        println("kotlin(\"jvm\") version (\"IntellijIdeaRulezzz\$\")".replace(GradleKtsPluginsCompletionContributor.kotlinRegex,"${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
        println("kotlin(\"jvm\") version \"IntellijIdeaRulezzz\$\"".replace(GradleKtsPluginsCompletionContributor.kotlinRegex,"${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
    }

    //     find e
//    var e = element
//    var i = 0
//    do {
//        i++
//        e = e.parent
//        show(element.project, content = "${e::class.java}:${e.text}:$i")
//    } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text && "plugins" != e.firstChild.text)

}