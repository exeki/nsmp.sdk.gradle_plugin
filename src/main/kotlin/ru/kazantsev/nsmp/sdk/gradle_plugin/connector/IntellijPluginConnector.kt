package ru.kazantsev.nsmp.sdk.gradle_plugin.connector

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.request.ProjectByPathRequest
import ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.response.ProjectByPathResponse

class IntellijPluginConnector(
    project: Project,
    port: Int,
) : AutoCloseable {

    private val logger = project.logger

    private val baseUrl = "http://127.0.0.1:$port"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(Java) {
        install(HttpTimeout) {
            requestTimeoutMillis = 5_000
            connectTimeoutMillis = 5_000
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    fun checkPluginStatus(): Boolean = runBlocking {
        try {
            val response = httpClient.get(baseUrl)
            IntellijPluginHttpException.throwIfResponseFailed(response)
            true
        } catch (e: Exception) {
            logger.info("Catch exception while checking plugin status: ${e.javaClass.name}: ${e.message}")
            false
        }
    }

    fun getProjectByPath(path: String): ProjectByPathResponse = runBlocking {
        val response = httpClient.post("$baseUrl/projects/by-path") {
            contentType(ContentType.Application.Json)
            setBody(ProjectByPathRequest(path = path))
        }
        IntellijPluginHttpException.throwIfResponseFailed(response)
        response.body<ProjectByPathResponse>()
    }

    override fun close() {
        httpClient.close()
    }
}
