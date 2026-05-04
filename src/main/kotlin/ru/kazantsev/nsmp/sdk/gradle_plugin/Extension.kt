package ru.kazantsev.nsmp.sdk.gradle_plugin


open class Extension {
    internal var intellijPluginIntegrationEnabled = true
    internal var dependencyConfigurationEnabled = true
    internal var port = Constants.DEFAULT_INTELLIJ_PLUGIN_PORT

    @Suppress("unused")
    fun disableDependencyConfiguration() {
        this.dependencyConfigurationEnabled = false
    }

    @Suppress("unused")
    fun disableIntellijPluginIntegration() {
        this.intellijPluginIntegrationEnabled = false
    }

    @Suppress("unused")
    fun intellijPluginIntegrationPort(port: Int) {
        this.port = port
    }
}
