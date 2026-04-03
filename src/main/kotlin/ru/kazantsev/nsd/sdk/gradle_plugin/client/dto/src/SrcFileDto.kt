package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

import java.io.File

data class SrcFileDto(
    val code: String,
    val file: File
)
