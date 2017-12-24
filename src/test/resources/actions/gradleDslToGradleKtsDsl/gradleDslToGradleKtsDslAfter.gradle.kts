plugins {
    groovy
    id("cn.bestwu.plugin-publish") version "0.0.12"
}

group = "cn.bestwu.gradle"
version = "0.0.12"

tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "1.7"
    targetCompatibility = "1.7"
    options.encoding = "UTF-8"
}




repositories {
    jcenter()
    maven("https://dl.bintray.com/cbeust/maven")
}

dependencies {
    compile(gradleApi())
    compile("com.google.code.gson:gson:2.1")
    compile("commons-beanutils:commons-beanutils-core:1.7.0")
    compile("commons-beanutils:commons-beanutils-core:1.7.0:jdk5")
    compile("commons-lang:commons-lang:2.4")
    compile("org.apache.zookeeper:zookeeper:3.4.6") {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
    compile("org.slf4j:slf4j-api:1.6.1")
    compileOnly("javax.servlet:servlet-api:2.5")
    compileOnly("org.springframework:spring:2.5.6.SEC03")
    testCompile("ch.qos.logback:logback-classic:0.9.29")
    testCompile("ch.qos.logback:logback-core:0.9.29")
    testCompile("junit:junit:4.5")
    testCompile("org.unitils:unitils:2.3")
}
