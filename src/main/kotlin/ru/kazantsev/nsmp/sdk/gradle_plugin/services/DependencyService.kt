package ru.kazantsev.nsmp.sdk.gradle_plugin.services

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import ru.kazantsev.nsmp.sdk.gradle_plugin.Constants.Companion.DEV_DEPENDENCY_IDS
import ru.kazantsev.nsmp.sdk.gradle_plugin.Constants.Companion.REPOSITORY_BASE_URI
import ru.kazantsev.nsmp.sdk.gradle_plugin.Constants.Companion.REPOSITORY_URI
import java.net.URI

/**
 * Сервис, который добавляет в проект репозитории и dev-зависимости, нужные плагину.
 */
class DependencyService(private val project: Project) {

    companion object {

    }

    private data class ModuleId(val group: String, val name: String)

    private val repositoryUsername: String? = System.getenv("GITHUB_USERNAME")
    private val repositoryPassword: String? = System.getenv("GITHUB_TOKEN")

    private fun normalizeRepositoryUrl(uri: URI): String = uri.toString().trimEnd('/', '*')

    /**
     * Добавляет в проект репозиторий GitHub Packages, если его ещё нет.
     */
    fun addRepositoriesToProject() {
        if (repositoryUsername == null || repositoryPassword == null) return
        val repositoryUri = project.uri(REPOSITORY_URI)
        val repositoryBaseUri = normalizeRepositoryUrl(project.uri(REPOSITORY_BASE_URI))
        val existingRepository = project.repositories
            .withType(MavenArtifactRepository::class.java)
            .find { repository ->
                val existingUrl = normalizeRepositoryUrl(repository.url)
                existingUrl == repositoryBaseUri || existingUrl.startsWith("$repositoryBaseUri/")
            }
        if (existingRepository != null) return

        project.repositories.maven {
            it.url = repositoryUri
            it.credentials.username = repositoryUsername
            it.credentials.password = repositoryPassword
        }
    }

    /**
     * Добавляет в проект зависимости, которые нужны для работы с NSD в режиме разработки.
     */
    fun addDependenciesToProject() {
        if (repositoryUsername == null || repositoryPassword == null) return
        val implementation = project.configurations.findByName("implementation") ?: return
        val dependenciesInProject = project.configurations
            .flatMap { configuration -> configuration.dependencies }
            .mapNotNull { dependency ->
                val group = dependency.group ?: return@mapNotNull null
                ModuleId(group = group, name = dependency.name)
            }
            .toSet()

        DEV_DEPENDENCY_IDS.forEach { dependencyNotation ->
            val dependencyCoordinates = dependencyNotation.substringBeforeLast(":")
            val (group, name) = dependencyCoordinates.split(":", limit = 2)
            val alreadyAdded = ModuleId(group = group, name = name) in dependenciesInProject
            if (!alreadyAdded) project.dependencies.add(implementation.name, dependencyNotation)
        }
    }
}
