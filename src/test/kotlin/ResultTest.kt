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

import cn.bestwu.gdph.AbstractGradlePluginsCompletionContributor
import cn.bestwu.gdph.kotlin.GradleKtsPluginsCompletionContributor
import cn.bestwu.gdph.search.ArtifactInfo
import cn.bestwu.gdph.search.compareVersion
import cn.bestwu.gdph.search.regex
import org.junit.Assert
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

/**
 *
 * @author Peter Wu
 * @since
 */
class ParseResultTest {

    @Test
    fun regexVersion() {
        val regex = AbstractGradlePluginsCompletionContributor.regex
        Assert.assertEquals("org.springframework.boot", "id(\"org.springframework.boot\") version( \"1.5.6.RELEASE\")".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id \"org.springframework.boot\" version( \"1.5.6.RELEASE\")".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id \"org.springframework.boot\" version \"1.5.6.RELEASE\"".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id('org.springframework.boot') version( '1.5.6.RELEASE')".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id 'org.springframework.boot' version( '1.5.6.RELEASE')".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id 'org.springframework.boot' version '1.5.6.RELEASE'".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id(\"org.springframework.boot\") version (\"1.5.6.RELEASE\")".replace(regex, "$1"))
        Assert.assertEquals("org.springframework.boot", "id(\"org.springframework.boot\") version \"1.5.6.RELEASE\"".replace(regex, "$1"))
    }

    @Test
    fun grRegex() {
        Assert.assertEquals("org.springframework.boot:spr", "org.springframework.boot:sprIntellijIdeaRulezzz ing-boot-dependencies".substringBefore("IntellijIdeaRulezzz "))
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
                "1.3.0.M",
                "1.3.0",
                "1.3.0.1",
                "1.3.1",
                "1.3.0.M2",
                "1.3.0.M5",
                "1.3.0.M1"
        )
        versions.sortWith(kotlin.Comparator { o1, o2 ->
            compareVersion(o1, o2)
        })
        Assert.assertEquals(arrayListOf(
                "1.3.0.SNAPSHOTS",
                "1.3.0.ALPHA",
                "1.3.0.BETA",
                "1.3.0.M",
                "1.3.0.M1",
                "1.3.0.M2",
                "1.3.0.M3",
                "1.3.0.M4",
                "1.3.0.M5",
                "1.3.0.RC1",
                "1.3.0.RC2",
                "1.3.0.RELEASE",
                "1.3.0",
                "1.3.0.1",
                "1.3.1"
        ), versions)
    }

    @Test
    fun mavenRegex() {

        regex.findAll(File(this::class.java.getResource("/result.json").path).readText()).forEach {
            val artifactInfo = ArtifactInfo(it.groupValues[1], it.groupValues[2], it.groupValues[3], "mavenCentral", "Apache")
            Assert.assertTrue(artifactInfo.version.isNotBlank())
            Assert.assertTrue(artifactInfo.artifactId.isNotBlank())
            Assert.assertTrue(artifactInfo.groupId.isNotBlank())
        }
    }


//    @Test
//    fun local() {
//        Assert.assertTrue(Locale.getDefault() == Locale.CHINA)
//        Assert.assertFalse(Locale.getDefault() == Locale.CHINESE)
//        Assert.assertTrue(Locale.getDefault() == Locale.SIMPLIFIED_CHINESE)
//    }

    @Test
    fun testkotlinRegex() {
        assertEquals("org.jetbrains.kotlin.jvm", "kotlin(\"jvm\", \"1.1.4\")".replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
        assertEquals("org.jetbrains.kotlin.jvm", "kotlin(\"jvm\") version (\"IntellijIdeaRulezzz\$\")".replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
        assertEquals("org.jetbrains.kotlin.jvm", "kotlin(\"jvm\") version \"IntellijIdeaRulezzz\$\"".replace(GradleKtsPluginsCompletionContributor.kotlinRegex, "${GradleKtsPluginsCompletionContributor.kotlinPrefix}$1"))
    }

}