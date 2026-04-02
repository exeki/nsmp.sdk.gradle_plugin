package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.basic_api_connector.Connector
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams

abstract class RemoteNsdTask : DefaultTask() {

    @get:Input
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
                if (ignoreSsl.isPresent) ignoreSsl.get() else false
            )
        } else ConnectorParams.byConfigFile(installationId.get())
    }


    protected fun createConnector(): Connector {
        return Connector(createConnectorParams())
    }

    init {
        group = "nsd_sdk_remote"
    }
}
