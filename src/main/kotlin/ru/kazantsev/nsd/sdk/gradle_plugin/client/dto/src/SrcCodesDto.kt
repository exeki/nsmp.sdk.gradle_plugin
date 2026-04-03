package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

data class SrcCodesDto(
    val scripts: List<String> = emptyList(),
    val modules: List<String> = emptyList()
)