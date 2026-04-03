package ru.kazantsev.nsd.sdk.gradle_plugin

import ru.kazantsev.nsd.basic_api_connector.ConnectorParams

open class Extension {
    internal var sendFilePath: String? = null
    internal var installation: Installation? = null

    fun setSendFilePath(path: String) {
        sendFilePath = path
    }

    fun setInstallation(installationId: String) {
        installation = InstallationByConfigFile(installationId)
    }

    fun setInstallation(installationId: String, connectorParamsPath: String) {
        installation = InstallationByConfigFileInPath(installationId, connectorParamsPath)
    }

    fun setInstallation(
        installationId: String,
        scheme: String,
        host: String,
        accessKey: String,
        ignoreSsl: Boolean
    ) {
        installation = InstallationDirect(
            installationId,
            scheme,
            host,
            accessKey,
            ignoreSsl
        )
    }
}

abstract class Installation {
    abstract val installationId: String
    abstract val connectorParams: ConnectorParams
    abstract fun createConnectorParams(): ConnectorParams
}

internal class InstallationByConfigFile(
    override val installationId: String
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams.byConfigFile(installationId)
    }
}

internal class InstallationByConfigFileInPath(
    override val installationId: String,
    val pathToConfigFile: String
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams.byConfigFileInPath(installationId, pathToConfigFile)
    }
}

internal class InstallationDirect(
    override val installationId: String,
    val scheme: String,
    val host: String,
    val accessKey: String,
    val ignoreSsl: Boolean
) : Installation() {
    override val connectorParams: ConnectorParams
        get() = createConnectorParams()

    override fun createConnectorParams(): ConnectorParams {
        return ConnectorParams(
            installationId,
            scheme,
            host,
            accessKey,
            ignoreSsl
        )
    }
}
