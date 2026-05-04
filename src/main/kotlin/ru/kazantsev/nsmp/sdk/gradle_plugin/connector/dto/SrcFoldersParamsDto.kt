package ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto

import kotlinx.serialization.Serializable

@Serializable
data class SrcFoldersParamsDto(
    val projectAbsolutePath: String,
    val scriptsRelativePath: String,
    val modulesRelativePath: String,
    val advImportsRelativePath: String,
)
