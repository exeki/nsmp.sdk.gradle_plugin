package ru.kazantsev.nsd.sdk.gradle_plugin.tooling

import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildController
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.gradle.GradleBuild
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ToolingApiBuildActionTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun executesBuildActionInAnIsolatedBuild() {
        Files.writeString(
            projectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "tooling-api-example"
            """.trimIndent()
        )
        Files.writeString(
            projectDir.resolve("build.gradle.kts"),
            """
            // empty build on purpose
            """.trimIndent()
        )

        val result = GradleConnector.newConnector()
            .forProjectDirectory(projectDir.toFile())
            .useGradleVersion("8.5")
            .connect()
            .use { connection ->
                connection.action(RootProjectNameAction()).run()
            }

        assertEquals("tooling-api-example", result)
    }

    private class RootProjectNameAction : BuildAction<String> {
        override fun execute(controller: BuildController): String {
            val build = controller.getModel(GradleBuild::class.java)
            return build.rootProject.name
        }
    }
}
