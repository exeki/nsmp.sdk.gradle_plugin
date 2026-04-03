package ru.kazantsev.nsd.sdk.gradle_plugin.services

import com.fasterxml.jackson.databind.ObjectMapper

class Utilities {
    companion object {
        val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()
    }
}