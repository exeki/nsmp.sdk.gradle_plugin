package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Тип исходника
 */
enum class SrcType(
    @JsonValue
    val code: String
) {
    /**
     * Скрипт
     */
    SCRIPT("script"),

    /**
     * Модуль
     */
    MODULE("module");

    companion object {
        @JsonCreator
        fun byCode(code: String) = entries.find { it.code == code }
    }
}