package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

/**
 * Информация о исходнике
 */
class SrcInfo(
    val checksum: String,
    val code: String
) {
    constructor() : this("", "")
}