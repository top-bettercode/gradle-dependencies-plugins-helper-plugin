package test.intention

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class MapNotationAddRepositoriesIntentionTest : CodeInsightTestBase() {

    private val intentionAfter = """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}"""

    //build.gradle
    fun testAddRepositoriesIntention() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionAddRepos() {
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
        intentionCheckResult(gradleFileName, """dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", """repositories {
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1'
}""")
    }

    fun testAddRepositoriesIntentionExist() {
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core', version: '9.5.1$caret'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: 'feign-core$caret', version: '9.5.1'
}""", intentionAfter)
        intentionCheckResult(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: 'feign-core', version: '9.5.1'
}""", intentionAfter)
    }

    fun testAddRepositoriesIntentionEmpty() {
        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign', name: '$caret', version: '9.5.1'
}""")
        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: 'io.github.openfeign$caret', name: '', version: '9.5.1'
}""")

        intentionEmpty(gradleFileName, """repositories {
    jcenter()
    maven { url 'https://dl.bintray.com/openfeign/maven' }
}

dependencies{
    compile group: '$caret', name: 'feign-core', version: '9.5.1'
}""")
    }

}