package test.intention

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class KtsAddRepositoriesIntentionTest : CodeInsightTestBase() {

    private val intentionAfter = """repositories {
    jcenter()
    maven { url = uri("https://dl.bintray.com/openfeign/maven") }
}

dependencies{
    compile("io.github.openfeign:feign-core:9.5.1")
}"""

    //build.gradle
    fun testAddRepositoriesIntention() {
        intentionCheckResult(gradleKtsFileName, """repositories {
    jcenter()
}

dependencies{
    compile("io.github.openfeign:feign-core:9.5.1$caret")
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionAddRepos() {
        intentionCheckResult(gradleKtsFileName, """dependencies{
    compile("io.github.openfeign:feign-core:9.5.1$caret")
}""", """repositories {
    maven { url = uri("https://dl.bintray.com/openfeign/maven") }
}

dependencies{
    compile("io.github.openfeign:feign-core:9.5.1")
}""")
    }

    fun testAddRepositoriesIntentionExist() {
        intentionCheckResult(gradleKtsFileName, """repositories {
    jcenter()
    maven { url = uri("https://dl.bintray.com/openfeign/maven") }
}

dependencies{
    compile("io.github.openfeign:feign-core:9.5.1$caret")
}""", intentionAfter)
    }

}