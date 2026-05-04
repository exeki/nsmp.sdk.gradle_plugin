package ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectPluginInfoDto(
    val installationId: String,
    val installationHost: String,
    val srcFoldersParams : SrcFoldersParamsDto
)