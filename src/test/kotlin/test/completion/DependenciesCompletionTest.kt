package test.completion

/**
 *
 * @author Peter Wu
 * @since
 */
class DependenciesCompletionTest : CodeInsightTestBase() {

    //build.gradle dependencies
    fun testStdDependencies() {
        doCheckResult(gradleFileName, """dependencies {
    compile 'kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect'
}""", "org.jetbrains.kotlin:kotlin-reflect")
    }

    fun testStdDependenciesVersion() {
        doCheckResult(gradleFileName, """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect$caret'
}""", """dependencies {
    compile 'org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion'
}""", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }

    //build.gradle dependencies map notation

    fun testDependenciesMapGroup() {
        doCheckResult(gradleFileName, """dependencies {
    compile group:'kotlin$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin'
}""", "org.jetbrains.kotlin")
    }

    fun testDependenciesMapName() {
        doCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect'
}""", "kotlin-reflect")
    }

    fun testDependenciesMapVersion() {
        doCheckResult(gradleFileName, """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$caret'
}""", """dependencies {
    compile group:'org.jetbrains.kotlin',name:'kotlin-reflect',version:'$kotlinVersion'
}""", kotlinVersion)
    }


    //build.gradle dependencies *
    fun testDependenciesWildcard() {
        doCheckResult(gradleFileName, """dependencies{
    testCompile 'kotlin*junit$caret'
}""", """dependencies{
    testCompile 'org.jetbrains.kotlin:kotlin-test-junit'
}""", "org.jetbrains.kotlin:kotlin-test-junit")
    }

    //build.gradle dependencies by className
    fun testDependenciesByFullyQualifiedClassName() {
        doCheckResult(gradleFileName, """dependencies{
    compile 'fc:feign.Client$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }

    fun testDependenciesByClassName() {
        doCheckResult(gradleFileName, """dependencies{
    compile 'c:feign$caret'
}""", """dependencies{
    compile 'com.netflix.feign:feign-core:$feignVersion'
}""", "com.netflix.feign:feign-core:$feignVersion")
    }


}