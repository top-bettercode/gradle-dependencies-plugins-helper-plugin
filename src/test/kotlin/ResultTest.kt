import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.ArtifactInfo
import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.compareVersion
import groovy.json.JsonSlurper
import org.jsoup.Jsoup
import org.junit.Test
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
            compareVersion(o1, o2)
        })
        println(versions)
    }

    @Test
    fun mavenRegex() {

        Regex("\\{\"id\":\"(.*?):(.*?):(.*?)\",").findAll("{\"responseHeader\":{\"status\":0,\"QTime\":22,\"params\":{\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"sort\":\"score desc,timestamp desc,g asc,a asc,v desc\",\"hl.snippets\":\"3\",\"indent\":\"off\",\"q\":\"fc:\\\"feign.Client\\\"\",\"core\":\"gav\",\"hl.fl\":\"fch\",\"wt\":\"json\",\"hl\":\"true\",\"rows\":\"30\",\"version\":\"2.2\"}},\"response\":{\"numFound\":111,\"start\":0,\"docs\":[{\"id\":\"io.github.openfeign:feign-core:9.5.1\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.5.1\",\"p\":\"jar\",\"timestamp\":1501638223000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\"-tests.jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.5.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.5.0\",\"p\":\"jar\",\"timestamp\":1494057013000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\"-tests.jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.4.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.4.0\",\"p\":\"jar\",\"timestamp\":1478630957000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\"-tests.jar\",\".jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.3.1\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.3.1\",\"p\":\"jar\",\"timestamp\":1472535150000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\"-tests.jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.3.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.3.0\",\"p\":\"jar\",\"timestamp\":1471611424000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\"-tests.jar\",\".jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.2.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.2.0\",\"p\":\"jar\",\"timestamp\":1471520866000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\"-tests.jar\",\".jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.1.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.1.0\",\"p\":\"jar\",\"timestamp\":1470666876000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-tests.jar\",\".pom\"]},{\"id\":\"io.github.openfeign:feign-core:9.0.0\",\"g\":\"io.github.openfeign\",\"a\":\"feign-core\",\"v\":\"9.0.0\",\"p\":\"jar\",\"timestamp\":1468470864000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\"-tests.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:6.1.3\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"6.1.3\",\"p\":\"jar\",\"timestamp\":1408469279000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:6.1.2\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"6.1.2\",\"p\":\"jar\",\"timestamp\":1396371784000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:6.1.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"6.1.1\",\"p\":\"jar\",\"timestamp\":1392757904000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:6.0.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"6.0.1\",\"p\":\"jar\",\"timestamp\":1384465993000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.4.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.4.1\",\"p\":\"jar\",\"timestamp\":1384463562000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:6.0.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"6.0.0\",\"p\":\"jar\",\"timestamp\":1383799714000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.4.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.4.0\",\"p\":\"jar\",\"timestamp\":1383768943000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.3.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.3.0\",\"p\":\"jar\",\"timestamp\":1379981203000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.2.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.2.0\",\"p\":\"jar\",\"timestamp\":1379889972000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.1.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.1.0\",\"p\":\"jar\",\"timestamp\":1379781976000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.0.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.0.1\",\"p\":\"jar\",\"timestamp\":1379462737000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:5.0.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"5.0.0\",\"p\":\"jar\",\"timestamp\":1379451293000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:2.0.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"2.0.1\",\"p\":\"jar\",\"timestamp\":1373844458000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:2.0.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"2.0.0\",\"p\":\"jar\",\"timestamp\":1372698348000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:1.1.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"1.1.1\",\"p\":\"jar\",\"timestamp\":1372691784000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:1.0.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"1.0.0\",\"p\":\"jar\",\"timestamp\":1372296642000,\"tags\":[\"feign\",\"developed\",\"core\",\"netflix\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.18.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.18.0\",\"p\":\"jar\",\"timestamp\":1468464572000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.17.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.17.0\",\"p\":\"jar\",\"timestamp\":1465085598000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.16.2\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.16.2\",\"p\":\"jar\",\"timestamp\":1461394026000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.16.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.16.1\",\"p\":\"jar\",\"timestamp\":1461335244000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.16.0\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.16.0\",\"p\":\"jar\",\"timestamp\":1459559673000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-sources.jar\",\"-javadoc.jar\",\".jar\",\".pom\"]},{\"id\":\"com.netflix.feign:feign-core:8.15.1\",\"g\":\"com.netflix.feign\",\"a\":\"feign-core\",\"v\":\"8.15.1\",\"p\":\"jar\",\"timestamp\":1457492349000,\"tags\":[\"feign\",\"core\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]}]},\"highlighting\":{\"io.github.openfeign:feign-core:9.5.1\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.5.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.4.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.3.1\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.3.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.2.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.1.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"io.github.openfeign:feign-core:9.0.0\":{\"fch\":[\"<em>feign<\\/em>.<em>client<\\/em>.DefaultClientTest\",\"<em>feign<\\/em>.<em>client<\\/em>.TrustingSSLSocketFactory\",\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:6.1.3\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:6.1.2\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:6.1.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:6.0.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.4.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:6.0.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.4.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.3.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.2.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.1.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.0.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:5.0.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:2.0.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:2.0.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:1.1.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:1.0.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.18.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.17.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.16.2\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.16.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.16.0\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]},\"com.netflix.feign:feign-core:8.15.1\":{\"fch\":[\"<em>feign<\\/em>.<em>Client<\\/em>\"]}}}").forEach {
            val artifactInfo = ArtifactInfo(it.groupValues[1], it.groupValues[2], it.groupValues[3], "mavenCentral", "Apache")
            println(artifactInfo.toString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun searchInNexus() {
        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&cn=org.junit.Test"
//        val url = "http://maven.aliyun.com/nexus/service/local/lucene/search?repositoryId=central&g=junit&a=junit"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/json")
        val stream = connection.inputStream ?: return
//        println(stream.bufferedReader().readText())
        val jsonResult = (JsonSlurper().parse(stream) as Map<*, *>)["data"] as List<Map<*, *>>
        jsonResult.forEach {
            val artifactInfo = ArtifactInfo(it["groupId"] as String, it["artifactId"] as String, it["latestRelease"] as String, "mavenCentral", "Apache")
            println(artifactInfo)
        }
        println(jsonResult.size)
    }
//     find e
//var e = element
//    var i = 0
//    do {
//        i++
//        e = e.parent
//        show(content = "${e::class.java}:${e.text}:$i")
//    } while ("dependencies" != e.firstChild.text && "imports" != e.firstChild.text && "plugins" != e.firstChild.text)
}