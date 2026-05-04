package ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ProjectByPathRequest(
    val path: String,
)
