package ru.kazantsev.nsmp.sdk.gradle_plugin.connector

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request

class IntellijPluginHttpException(
    override val message: String,
) : RuntimeException(message) {
    companion object {

        suspend fun buildMessage(response: HttpResponse): String {
            val snippet = runCatching { response.bodyAsText() }.getOrNull().orEmpty().take(512)
            return buildString {
                append("Failed to execute ")
                append(response.request.method.value)
                append(" ")
                append(response.request.url.toString())
                append(": ")
                append(response.status.value)
                if (snippet.isNotEmpty()) append(" — body: $snippet")
            }
        }

        suspend fun throwIfResponseFailed(response: HttpResponse) {
            if (response.status.value in 200..299) return
            throw IntellijPluginHttpException(buildMessage(response))
        }
    }
}
