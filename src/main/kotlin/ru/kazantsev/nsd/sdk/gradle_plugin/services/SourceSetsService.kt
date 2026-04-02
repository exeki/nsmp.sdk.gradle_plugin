package ru.kazantsev.nsd.sdk.gradle_plugin.services

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import java.io.FileWriter

class SourceSetsService(private val project: Project) {
    companion object {

        const val SCRIPT_SOURCE_SET_PATH: String = "src\\main\\scripts"
        private const val GROOVY_SOURCE_SET_PATH: String = "src\\main\\groovy"
        private const val MODULES_SOURCE_SET_PATH: String = "src\\main\\modules"

        private val PACKAGE_FOLDERS: Set<String> = setOf(
            "attrFiltration",
            "calculationOnEdit",
            "eventActionConditions",
            "eventActions",
            "migration\\console",
            "migration\\scheduledTasks",
            "permissions",
            "scheduledTasks",
            "stateActions\\fromState",
            "stateActions\\fromStateCondition",
            "stateActions\\inState",
            "stateActions\\inStateCondition"
        )

        val NEW_CONSOLE_FILE_TEXT = """
           //РЎРєСЂРёРїС‚ РёР· СЌС‚РѕРіРѕ С„Р°Р№Р»Р° РјРѕР¶РµС‚ Р±С‹С‚СЊ РѕС‚РїСЂР°РІР»РµРЅ РІ NSD.
           //Р”Р»СЏ РѕС‚РїСЂР°РІРєРё СЃРєСЂРёРїС‚Р° РІС‹РїРѕР»РЅРёС‚Рµ Р·Р°РґР°С‡Сѓ "send_script".
           //Р РµР·СѓР»СЊС‚Р°С‚ РІС‹РїРѕР»РЅРµРЅРёСЏ СЃРєСЂРёРїС‚Р° РѕС‚РѕР±СЂР°Р·РёС‚СЊСЃСЏ РІ РєРѕРЅСЃРѕР»Рё.  
           
           import static ru.kazantsev.nsd.sdk.global_variables.ApiPlaceholder.*
           import static ru.kazantsev.nsd.sdk.global_variables.GlobalVariablesPlaceholder.*
           import ru.naumen.core.server.script.spi.*
        """.trimIndent()
    }

    val groovy = SourceRoot(project, GROOVY_SOURCE_SET_PATH)
    val modules = SourceRoot(project, MODULES_SOURCE_SET_PATH)
    val scripts = SourceRoot(project, SCRIPT_SOURCE_SET_PATH)
    val roots: Set<SourceRoot> = setOf(groovy, modules, scripts)

    fun configureSourceSets() {
        val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
        roots.forEach { main.java.srcDir(it.getRelativePath()) }
    }

    fun createProjectStructure(consoleFilePath: String) {
        roots.forEach { it.create() }
        createScriptPackageFolders()
        createConsoleFile(consoleFilePath)
    }

    fun createScriptPackageFolders() {
        scripts.create()
        PACKAGE_FOLDERS.forEach {
            project.file("$SCRIPT_SOURCE_SET_PATH/$it").mkdirs()
        }
    }

    fun createConsoleFile(consoleFilePath: String) {
        val consoleFile = project.file(consoleFilePath)
        if (!consoleFile.exists()) {
            val parent = consoleFile.parentFile
            if (!parent.exists()) parent.mkdirs()
            consoleFile.createNewFile()
            val writer = FileWriter(consoleFile)
            writer.write(NEW_CONSOLE_FILE_TEXT)
            writer.close()
        }
    }
}
