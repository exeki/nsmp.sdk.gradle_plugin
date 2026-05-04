package ru.kazantsev.nsmp.sdk.gradle_plugin

class Constants {
    companion object {
        const val DEFAULT_INTELLIJ_PLUGIN_PORT = 8123
        const val REPOSITORY_BASE_URI = "https://maven.pkg.github.com/exeki"
        const val REPOSITORY_URI = "$REPOSITORY_BASE_URI/*"
        val DEV_DEPENDENCY_IDS = setOf(
            "ru.kazantsev.nsd.sdk:global_variables:1.5.0",
            "org.apache.groovy:groovy:4.0.14"
        )
    }
}