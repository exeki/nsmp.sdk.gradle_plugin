package ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

class SdkApiConnectorArchiveTest {

    private val connector = SdkApiConnector(
        ConnectorParams(
            TEST_INSTALLATION_ID,
            TEST_SCHEME,
            TEST_HOST,
            TEST_ACCESS_KEY,
            true
        )
    )

    @Test
    fun downloadsArchiveAndSavesItToTestResourcesAsZip() {
        val base64 = connector.getScripts()
        val archiveBytes = Base64.getDecoder().decode(base64)
        val target = Paths.get("src", "test", "resources", "src.zip")
        Files.createDirectories(target.parent)
        Files.write(target, archiveBytes)

        assertTrue(Files.exists(target))
        assertTrue(Files.size(target) > 0)
    }

    companion object {
        private const val TEST_INSTALLATION_ID = "EXEKI1"
        private const val TEST_SCHEME = "https"
        private const val TEST_HOST = "nsd1.exeki.local"
        private const val TEST_ACCESS_KEY = "69f8d9c0-56bd-4c87-8010-4a95e2cb4b14"
    }
}
