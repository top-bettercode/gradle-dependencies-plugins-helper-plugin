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
    //since-build 162.1
//    version = "2016.2.1"
    setPlugins("Groovy", "gradle", "Kotlin")

//    publish {
//        pluginId "YOUR_PLUGIN_ID"
//        // (optional) apply from: "YOUR_CUSTOM_PROPERTIES_FILE.properties"
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

