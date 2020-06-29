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

package cn.bestwu.gdph.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class GDPHConfigurable : Configurable {

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
            view!!.useMavenCentral = Settings.useMavenCentral
            view!!.useNexus = Settings.useNexus
            view!!.nexusSearchUrl = Settings.nexusSearchUrl
        }

        reset()

        return view!!.dpPanel
    }

    override fun isModified(): Boolean {
        val settings = Settings.getInstance()
        return settings.useNexus != view!!.useNexus || settings.useMavenCentral != view!!.useMavenCentral || settings.nexusSearchUrl != view!!.nexusSearchUrl
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = Settings.getInstance()
        settings.useMavenCentral = view!!.useMavenCentral
        settings.useNexus = view!!.useNexus
        settings.nexusSearchUrl = view!!.nexusSearchUrl
    }

    override fun reset() {
        val settings = Settings.getInstance()
        view!!.useMavenCentral = settings.useMavenCentral
        view!!.useNexus = settings.useNexus
        view!!.nexusSearchUrl = settings.nexusSearchUrl
    }

    override fun disposeUIResources() {
        view = null
    }
}
