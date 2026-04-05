package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import java.io.FileWriter
import java.nio.file.Path

/**
 * Сервис, который описывает и создаёт стандартные source root плагина.
 */
class SrcFoldersService(private val projectRootPath: Path) {
    companion object {
        const val SCRIPT_SOURCE_SET_PATH: String = "src\\main\\scripts"
        const val GROOVY_SOURCE_SET_PATH: String = "src\\main\\groovy"
        const val MODULES_SOURCE_SET_PATH: String = "src\\main\\modules"

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
           // Скрипт из этого файла может быть отправлен в NSD.
           // Для отправки скрипта выполните задачу "send_script".
           // Результат выполнения скрипта отобразится в консоли.

           import static ru.kazantsev.nsd.sdk.global_variables.ApiPlaceholder.*
           import static ru.kazantsev.nsd.sdk.global_variables.GlobalVariablesPlaceholder.*
           import ru.naumen.core.server.script.spi.*
        """.trimIndent()
    }

    val groovy = SrcFolder(projectRootPath, GROOVY_SOURCE_SET_PATH)
    val modules = SrcFolder(projectRootPath, MODULES_SOURCE_SET_PATH)
    val scripts = SrcFolder(projectRootPath, SCRIPT_SOURCE_SET_PATH)
    val roots: Set<SrcFolder> = setOf(groovy, modules, scripts)

    /**
     * Создаёт базовую структуру каталогов плагина и console-скрипт.
     */
    fun createProjectStructure(consoleFilePath: String) {
        roots.forEach { it.create() }
        createScriptPackageFolders()
        createConsoleFile(consoleFilePath)
    }

    /**
     * Создаёт набор стандартных подпапок внутри scripts source root.
     */
    fun createScriptPackageFolders() {
        scripts.create()
        PACKAGE_FOLDERS.forEach {
            projectRootPath.resolve(SCRIPT_SOURCE_SET_PATH).resolve(it).toFile().mkdirs()
        }
    }

    /**
     * Создаёт console-файл, если он ещё не существует.
     */
    fun createConsoleFile(consoleFilePath: String) {
        val consoleFile = projectRootPath.resolve(consoleFilePath).toFile()
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
