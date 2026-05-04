package ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectInfoDto(
    val name: String,
    val basePath: String? = null,
    val isDisposed: Boolean,
    val isInitialized: Boolean,
    val nsmpSdk : ProjectPluginInfoDto?
)
