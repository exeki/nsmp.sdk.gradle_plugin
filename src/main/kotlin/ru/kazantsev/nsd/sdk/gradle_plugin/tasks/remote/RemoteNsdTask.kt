package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.basic_api_connector.Connector
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams
import ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector.SdkApiConnector

abstract class RemoteNsdTask : DefaultTask() {

    @get:Internal
    var connectorParamsProvider: Provider<ConnectorParams>? = null

    @get:Input
    @get:Optional
    @get:Option(option = "inst", description = "NSD installation identifier")
    abstract val installationId: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "config-file-path", description = "Path to config file")
    abstract val configurationPath: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "scheme", description = "Connection scheme, for example http or https")
    abstract val scheme: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "host", description = "NSD host")
    abstract val host: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "key", description = "NSD access key")
    abstract val accessKey: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "ignore-ssl", description = "Ignore SSL validation")
    abstract val ignoreSsl: Property<Boolean>

    protected fun createConnectorParams(): ConnectorParams {
        return if (installationId.isPresent && configurationPath.isPresent) {
            ConnectorParams.byConfigFileInPath(installationId.get(), configurationPath.get())
        } else if (installationId.isPresent && scheme.isPresent && host.isPresent && accessKey.isPresent) {
            ConnectorParams(
                installationId.get(),
                scheme.get(),
                host.get(),
                accessKey.get(),
                ignoreSsl.orNull ?: false
            )
        } else if (installationId.isPresent) {
            ConnectorParams.byConfigFile(installationId.get())
        } else {
            throw IllegalStateException("SMP installation identifier is not configured")
        }
    }

    protected fun createConnector(): SdkApiConnector {
        return SdkApiConnector(resolveConnectorParams())
    }

    protected fun resolveConnectorParams(): ConnectorParams {
        return if (installationId.isPresent) createConnectorParams()
        else connectorParamsProvider?.orNull
            ?: throw IllegalStateException("SMP connection parameters are not configured")
    }

    protected fun requireRequestedSources(scripts: List<String>, modules: List<String>) {
        if (scripts.isEmpty() && modules.isEmpty()) {
            throw IllegalArgumentException(
                "At least one of scripts or modules must be specified. " +
                        "Use --scripts=a,b and/or --modules=c,d"
            )
        }
    }

    protected fun parseCsvOption(value: String?): List<String> {
        return value
            .orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    init {
        group = "smp_sdk_remote"
    }
}
