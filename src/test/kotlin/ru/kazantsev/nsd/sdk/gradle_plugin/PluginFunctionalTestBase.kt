package ru.kazantsev.nsd.sdk.gradle_plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class PluginFunctionalTestBase {

    companion object {
        protected const val TEST_INSTALLATION_ID = "EXEKI1"
        protected const val TEST_SCHEME = "https"
        protected const val TEST_HOST = "nsd1.exeki.local"
        protected const val TEST_ACCESS_KEY = "69f8d9c0-56bd-4c87-8010-4a95e2cb4b14"
        protected const val IGNORE_SSL = "true"
        protected const val CLEAN_TEST_PROJECT_DIR = true
    }

    lateinit var testProjectDir: Path

    @BeforeEach
    fun setUpTestProject() {
        testProjectDir = Paths.get("build", "functional-test-project")
        if (CLEAN_TEST_PROJECT_DIR) {
            deleteRecursively(testProjectDir)
        }
        Files.createDirectories(testProjectDir)
    }

    protected fun writeConsumerProject() {
        writeConsumerProject(
            """
            smpSdk {
                setInstallation(
                    "$TEST_INSTALLATION_ID",
                    "$TEST_SCHEME",
                    "$TEST_HOST",
                    "$TEST_ACCESS_KEY",
                    $IGNORE_SSL
                )
            }
            """.trimIndent()
        )
    }

    protected fun writeConsumerProject(sdkConfiguration: String) {
        Files.writeString(
            testProjectDir.resolve("settings.gradle.kts"),
            """
            rootProject.name = "consumer"
            """.trimIndent()
        )

        Files.writeString(
            testProjectDir.resolve("build.gradle.kts"),
            """
            plugins {
                id("nsd_sdk")
            }

            $sdkConfiguration

            tasks.register("printImplementationDeps") {
                doLast {
                    configurations.getByName("implementation").dependencies.forEach {
                        println("${'$'}{it.group}:${'$'}{it.name}:${'$'}{it.version}")
                    }
                }
            }
            """.trimIndent()
        )
    }

    protected fun runner(vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(*arguments)
            .withPluginClasspath()
    }

    protected fun remoteServerArgs(): Array<String> {
        return arrayOf(
            "--inst", TEST_INSTALLATION_ID,
            "--scheme", TEST_SCHEME,
            "--host", TEST_HOST,
            "--key", TEST_ACCESS_KEY,
            "--ignore-ssl"
        )
    }

    private fun deleteRecursively(path: Path) {
        if (!Files.exists(path)) {
            return
        }

        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }
}
