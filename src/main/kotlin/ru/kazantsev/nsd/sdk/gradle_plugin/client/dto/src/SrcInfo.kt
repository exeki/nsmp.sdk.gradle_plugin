package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

/**
 * Информация о исходнике
 */
class SrcInfo {
    /**
     * Чексумма исходника
     */
    val checksum: String = ""
    /**
     * Код исходника
     */
    val code: String = ""
    /**
     * Тип исходника
     */
    val type : SrcType = SrcType.MODULE
}