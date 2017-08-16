package cn.bestwu.intellij.plugins.gradle.codeInsight.completion.config

import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.ImportMavenRepositoriesTask
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class GDPHConfigurable(val project: Project) : Configurable {

    private var view: ConfigurationView? = null

    @Nls
    override fun getDisplayName(): String {
        return "Gradle Dependencies And Plugins Helper"
    }

    override fun getHelpTopic(): String? {
        return "Configure the default settings for the Gradle Dependencies And Plugins Helper"
    }

    override fun createComponent(): JComponent? {
        if (view == null) {
            view = ConfigurationView()
        }

        // Reset on click.
        view!!.resetButton.addActionListener {
            view!!.useNexus = Settings.useNexus
            view!!.useMavenIndex = Settings.useMavenIndex
            view!!.nexusSearchUrlField = Settings.nexusSearchUrl
            view!!.remoteRepositories = Settings.remoteRepositories
        }

        reset()

        return view!!.dpPanel
    }

    override fun isModified(): Boolean {
        val settings = Settings.getInstance(project)
        return settings.useNexus != view!!.useNexus || settings.useMavenIndex != view!!.useMavenIndex || settings.nexusSearchUrl != view!!.nexusSearchUrlField || settings.remoteRepositories == view!!.remoteRepositories
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = Settings.getInstance(project)
        settings.useNexus = view!!.useNexus
        settings.useMavenIndex = view!!.useMavenIndex
        settings.nexusSearchUrl = view!!.nexusSearchUrlField
        settings.remoteRepositories = view!!.remoteRepositories
        ImportMavenRepositoriesTask.performTask(project)
    }

    override fun reset() {
        val settings = Settings.getInstance(project)
        view!!.useNexus = settings.useNexus
        view!!.useMavenIndex = settings.useMavenIndex
        view!!.nexusSearchUrlField = settings.nexusSearchUrl
        view!!.remoteRepositories = settings.remoteRepositories
    }

    override fun disposeUIResources() {
        view = null
    }
}
