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


import java.io.File
import java.util.*
import kotlin.system.exitProcess

println("更新 Maven Index")

val workDir = File("/data/tech/maven-index")
val centralRepoUrl = "http://repo1.maven.org/maven2/.index"
val indexerCliJar = "/data/repositories/bestwu/settings/sys/lib/indexer-cli/indexer-cli-6.0.0.jar"
val localRepository = "/home/peter/.m2/repository/"

val indexDir = File(workDir, "index")
val nexusDir = File(workDir, "nexus-index")
val localDir = File(workDir, "local").absoluteFile

val localIndexDir = File(indexDir, "local")
val centralIndexDir = File(indexDir, "central")

nexusDir.mkdirs()

//更新 local index
unlock(localIndexDir)
exec("java -jar $indexerCliJar -r $localRepository -i ${localIndexDir.absolutePath} -d $localDir", "更新 local index")

val fullIndex = File(nexusDir, "nexus-maven-repository-index.gz")
val fullIndexProperties = File(nexusDir, "nexus-maven-repository-index.properties")
val fullIndexSha1 = File(nexusDir, "nexus-maven-repository-index.gz.sha1")
if (fullIndex.exists()) {
    exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.properties.last $centralRepoUrl/nexus-maven-repository-index.properties", "下载最新 nexus-maven-repository-index.properties")
    val lastProperties = "${nexusDir.absolutePath}/nexus-maven-repository-index.properties"
    val lastIncremental = getLastIncremental(lastProperties)
    val newLastProperties = "${nexusDir.absolutePath}/nexus-maven-repository-index.properties.last"
    val newLastIncremental = getLastIncremental(newLastProperties)
    if (lastIncremental == newLastIncremental) {
        println("central index 是最新的")
        File(newLastProperties).delete()
    } else {
        val incrementral = lastIncremental + 1
        for (i in incrementral..newLastIncremental) {
            val indexFile = File("${nexusDir.absolutePath}/nexus-maven-repository-index.${i}.gz")
            val indexSha1File = File("${nexusDir.absolutePath}/nexus-maven-repository-index.${i}.gz.sha1")
            if (!indexFile.exists()) {
                exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.${i}.gz.sha1 $centralRepoUrl/nexus-maven-repository-index.${i}.gz.sha1", "下载 nexus-maven-repository-index.${i}.gz.sha1")
                exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.${i}.gz $centralRepoUrl/nexus-maven-repository-index.${i}.gz", "下载 nexus-maven-repository-index.${i}.gz", {
                    if (sha1(indexFile) != indexSha1File.readText()) {
                        indexFile.delete()
                        indexSha1File.delete()
                        System.err.println("${indexFile.absolutePath}下载失败")
                        exitProcess(-1)
                    }
                }, {
                    indexFile.delete()
                    indexSha1File.delete()
                    System.err.println("${indexFile.absolutePath}下载失败")
                    exitProcess(-1)
                })
            }
        }
        unlock(centralIndexDir)
        for (i in incrementral..newLastIncremental) {
            exec("cd $nexusDir && java -jar $indexerCliJar -u nexus-maven-repository-index.${i}.gz -d ${centralIndexDir.absolutePath}", "更新 central index $i")
        }
        exec("mv $newLastProperties $lastProperties", "更新 nexus-maven-repository-index.properties")
    }
} else {
    exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.properties $centralRepoUrl/nexus-maven-repository-index.properties", "下载 nexus-maven-repository-index.properties")
    exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.gz.sha1 $centralRepoUrl/nexus-maven-repository-index.gz.sha1", "下载 nexus-maven-repository-index.gz.sha1")
    exec("curl -o ${nexusDir.absolutePath}/nexus-maven-repository-index.gz $centralRepoUrl/nexus-maven-repository-index.gz", "下载 nexus-maven-repository-index.gz", {
        if (sha1(fullIndex) == fullIndexSha1.readText()) {
            unlock(centralIndexDir)
            exec("cd $nexusDir && java -jar $indexerCliJar -u nexus-maven-repository-index.gz -d ${centralIndexDir.absolutePath}", "更新 central index")
        } else {
            cleanCentralIndex()
        }
    }, {
        cleanCentralIndex()
    })
}

fun unlock(index: File) {
    val writeLock = File(index, "write.lock")
    writeLock.delete()
    writeLock.writeText("")
}

fun exec(cmd: String, tip: String = "", success: (String) -> Unit = {}, fail: (String) -> Unit = {}): String {
    println("----------------------------------------")
    println("$tip ... ")
    val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
    process.waitFor()

    return if (process.exitValue() == 0) {
        println("执行成功")
        val result = process.inputStream.reader().readText()
        if (result.isNotBlank()) {
            println("执行结果：")
            println(result)
        }
        println("----------------------------------------")
        success(result)
        result
    } else {
        System.err.println("执行失败")
        val result = process.errorStream.reader().readText()
        if (result.isNotBlank()) {
            System.err.println("执行结果：")
            System.err.println(result)
        }
        println("----------------------------------------")
        fail(result)
        result
    }
}

fun sha1(file: File): String {
    return exec("sha1sum ${file.absolutePath} |cut -d' ' -f1", "计算 ${file.absolutePath} sha1").trimEnd('\n')
}

fun getLastIncremental(filePath: String): Int {
    val properties = Properties()
    properties.load(File(filePath).inputStream())
    return properties.getProperty("nexus.index.last-incremental").toInt()
}

fun cleanCentralIndex() {
    fullIndex.delete()
    fullIndexProperties.delete()
    fullIndexSha1.delete()
}
