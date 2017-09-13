package test.completion

import test.CodeInsightTestBase

/**
 *
 * @author Peter Wu
 * @since
 */
class DependenciesCompletionTest : CodeInsightTestBase() {

    //build.gradle dependencies
    fun testStdDependencies() {
        completionCheckResult(gradleFileName, """dependencies {
    compile 'kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect'
}""", "org.jetbrains.kotlin:kotlin-reflect")
    }

    fun testStdDependenciesVersion() {
        completionCheckResult(gradleFileName, """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion'
}""", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }

    //build.gradle dependencies map notation

    fun testDependenciesMapGroup() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'kotlin$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin'
}""", "org.jetbrains.kotlin")
    }

    fun testDependenciesMapName() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect'
}""", "kotlin-reflect")
    }

    fun testDependenciesMapVersion() {
        completionCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$kotlinVersion'
}""", kotlinVersion)
    }


    //build.gradle dependencies *
    fun testDependenciesWildcard() {
        completionCheckResult(gradleFileName, """dependencies{
    testCompile 'kotlin*junit$caret'
}""", """dependencies{
    testCompile 'org.jetbrains.kotlin:kotlin-test-junit'
}""", "org.jetbrains.kotlin:kotlin-test-junit")
    }

    //build.gradle dependencies by className
    fun testDependenciesByFullyQualifiedClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile 'fc:feign.Client$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }

    fun testDependenciesByClassName() {
        completionCheckResult(gradleFileName, """dependencies{
    compile 'c:feign$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }


}