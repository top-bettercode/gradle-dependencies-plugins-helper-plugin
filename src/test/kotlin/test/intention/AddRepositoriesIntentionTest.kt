package test.intention

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class AddRepositoriesIntentionTest : CodeInsightTestBase() {

    private val intentionAfter = """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1'
}"""
    private val intentionCenterAfter = """repositories {
    jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3'
}"""

    //build.gradle
    fun testAddRepositoriesIntention() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionAddRepos() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1'
}""")
    }

    fun testAddRepositoriesIntentionExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile 'io.github.openfeign:feign-core:9.5.1$caret'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionCenterNoRepo() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", intentionCenterAfter)
    }

    fun testAddRepositoriesIntentionCenter() {
        intentionCheckResult(gradleFileName, """repositories {
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", """repositories {
jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3'
}""")
    }

    fun testAddRepositoriesIntentionCenterExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile 'org.jsoup:jsoup:1.10.3$caret'
}""", intentionCenterAfter)
    }

}