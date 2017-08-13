plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "0.2.16"
}
group = "cn.bestwu"
version = "0.0.1"
intellij {
    pluginName = "Gradle Dependencies And Plugins Helper"
    updateSinceUntilBuild = false
    version = "IC-2017.2.1"
//    version = "145.184.1"
    setPlugins("Groovy", "gradle", "Kotlin")

//    publish {
//        pluginId "YOUR_PLUGIN_ID"
//    }

}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jsoup:jsoup:1.10.3")

    testCompile(kotlin("test-junit"))
}

