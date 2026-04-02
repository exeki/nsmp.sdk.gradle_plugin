package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

/**
 * Данные о полученном исходнике
 */
class SrcDto(
    /**
     * Информация о полученном исходнике
     */
    val info: SrcInfo,
    /**
     * Текст исходника
     */
    val text: String
)