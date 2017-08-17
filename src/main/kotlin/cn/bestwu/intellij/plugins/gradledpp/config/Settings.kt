package cn.bestwu.intellij.plugins.gradle.codeInsight.completion.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import java.util.*


@com.intellij.openapi.components.State(
        name = "gdphConfig",
        storages = arrayOf(Storage("gdphConfig.xml"))
)
class Settings(var first: Boolean = true, var originRemoteRepositories: Set<String> = mutableSetOf(), var useNexus: Boolean = Settings.useNexus, var nexusSearchUrl: String = Settings.nexusSearchUrl, var useMavenIndex: Boolean = Settings.useMavenIndex, var remoteRepositories: MutableSet<String> = Settings.remoteRepositories) : PersistentStateComponent<Settings> {
    override fun loadState(state: Settings?) {
        this.useNexus = state?.useNexus ?: Settings.useNexus
        this.useMavenIndex = state?.useMavenIndex ?: Settings.useMavenIndex
        this.remoteRepositories = state?.remoteRepositories ?: Settings.remoteRepositories
        this.nexusSearchUrl = state?.nexusSearchUrl ?: Settings.nexusSearchUrl
    }

    override fun getState(): Settings? {
        return this
    }

    companion object {
        val useNexus: Boolean = Locale.getDefault() == Locale.CHINA
        val useMavenIndex: Boolean = false
        val nexusSearchUrl: String = "http://maven.aliyun.com/nexus/service/local/lucene/search"
        val mavenCentralRemoteRepository = "https://repo1.maven.org/maven2/"
        val remoteRepositories = mutableSetOf(mavenCentralRemoteRepository)

        fun getInstance(project: Project): Settings {
            return ServiceManager.getService(project, Settings::class.java)
        }
    }


}
