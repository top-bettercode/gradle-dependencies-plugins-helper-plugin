package test.completion

/**
 *
 * @author Peter Wu
 * @since
 */
class DependenciesKtsCompletionTest : CodeInsightTestBase() {

    //build.gradle.kts dependencies
    fun testKtsDependencies() {
        doCheckResult(gradleKtsFileName, """dependencies{
    compile("kotlin-reflect$caret")
}""", """dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect")
}""", "org.jetbrains.kotlin:kotlin-reflect")
    }

    fun testKtsDependenciesVersion() {
        doCheckResult(gradleKtsFileName, """dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect$caret")
}""", """dependencies{
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}""", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }

    //build.gradle.kts dependencies kotlin()
    fun testKtsDependenciesKotlin() {
        doCheckResult(gradleKtsFileName, """dependencies{
    compile(kotlin("$caret"))
}""", """dependencies{
    compile(kotlin("stdlib-jre8"))
}""", "stdlib-jre8")
    }

    fun testKtsDependenciesKotlinVersion() {
        doCheckResult(gradleKtsFileName, """dependencies{
    compile(kotlin("stdlib-jre8","$caret"))
}""", """dependencies{
    compile(kotlin("stdlib-jre8","$kotlinVersion"))
}""", kotlinVersion)
    }

    //build.gradle.kts dependencies *
    fun testKtsDependenciesWildcard() {
        doCheckResult(gradleKtsFileName, """dependencies{
    testCompile("kotlin*junit$caret")
}""", """dependencies{
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}""", "org.jetbrains.kotlin:kotlin-test-junit")
    }

    //build.gradle.kts dependencies by className
    fun testDependenciesByFullyQualifiedClassName() {
        doCheckResult(gradleFileName, """dependencies{
    compile("fc:feign.Client$caret")
}""", """dependencies{
    compile("com.netflix.feign:feign-core:$feignVersion")
}""", "com.netflix.feign:feign-core:$feignVersion")
    }
    fun testDependenciesByClassName() {
        doCheckResult(gradleFileName, """dependencies{
    compile("c:feign$caret")
}""", """dependencies{
    compile("com.netflix.feign:feign-core:$feignVersion")
}""", "com.netflix.feign:feign-core:$feignVersion")
    }

}