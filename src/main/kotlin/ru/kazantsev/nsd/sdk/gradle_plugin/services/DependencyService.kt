package ru.kazantsev.nsd.sdk.gradle_plugin.services

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class DependencyService(private val project: Project) {

    companion object {
        private const val REPOSITORY_URI = "https://maven.pkg.github.com/exeki/*"

        private val DEV_DEPENDENCY_IDS = setOf(
            "ru.kazantsev.nsd:json_rpc_connector:1.+",
            "ru.kazantsev.nsd.sdk:global_variables:1.+"
        )
    }

    private val repositoryUsername: String? = System.getenv("GITHUB_USERNAME")
    private val repositoryPassword: String? = System.getenv("GITHUB_TOKEN")

    fun addRepositoriesToProject() {
        val repositoryUri = project.uri(REPOSITORY_URI)
        val existingRepository = project.repositories
            .withType(MavenArtifactRepository::class.java)
            .find { it.url == repositoryUri }
        if (existingRepository != null) {
            return
        }

        project.repositories.maven {
            it.url = repositoryUri
            if (repositoryUsername != null) {
                it.credentials.username = repositoryUsername
                it.credentials.password = repositoryPassword
            }
        }
    }

    fun addDevDependenciesToProject() {
        val implementation = project.configurations.findByName("implementation") ?: return
        DEV_DEPENDENCY_IDS.forEach {
            val alreadyAdded = implementation.dependencies.any { dependency ->
                "${dependency.group}:${dependency.name}:${dependency.version}" == it
            }
            if (!alreadyAdded) {
                project.dependencies.add(implementation.name, it)
            }
        }
    }
}
