package cn.bestwu.gdph.config

import cn.bestwu.gdph.maven.ImportMavenRepositoriesTask
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class GDPHConfigurable(private val project: Project) : Configurable {

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
            view!!.nexusSearchUrl = Settings.nexusSearchUrl
            view!!.remoteRepositories = Settings.remoteRepositories
        }

        reset()

        return view!!.dpPanel
    }

    override fun isModified(): Boolean {
        val settings = Settings.getInstance()
        return settings.useNexus != view!!.useNexus || settings.useMavenIndex != view!!.useMavenIndex || settings.nexusSearchUrl != view!!.nexusSearchUrl || settings.remoteRepositories != view!!.remoteRepositories
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = Settings.getInstance()
        val changeMavenIndex = settings.useMavenIndex != view!!.useMavenIndex || settings.remoteRepositories != view!!.remoteRepositories
        settings.useNexus = view!!.useNexus
        settings.useMavenIndex = view!!.useMavenIndex
        settings.nexusSearchUrl = view!!.nexusSearchUrl
        settings.remoteRepositories = view!!.remoteRepositories
        if (changeMavenIndex)
            ImportMavenRepositoriesTask.performTask(project)
    }

    override fun reset() {
        val settings = Settings.getInstance()
        view!!.useNexus = settings.useNexus
        view!!.useMavenIndex = settings.useMavenIndex
        view!!.nexusSearchUrl = settings.nexusSearchUrl
        view!!.remoteRepositories = settings.remoteRepositories
    }

    override fun disposeUIResources() {
        view = null
    }
}
