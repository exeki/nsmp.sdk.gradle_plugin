# NSD SDK Gradle Plugin

Плагин поставляет ряд функций, упрощающих написание скриптов и модулей для NSD.
Все примеры будут указаны с использованием kotlin dsl синтаксиса, использование в файлах gradle с groovy dls так же возможно, но синтаксис может чуть отличаться.

# Функции

### Автоматическое подключение зависимостей

Плагин автоматически подключает:
1. Gradle plugin `groovy`
2. Мой GitHub репозиторий для зависимостей: https://maven.pkg.github.com/exeki/*
3. Библиотеку [nsd.sdk.global_variables](https://github.com/exeki/nsd.sdk.global_variables) - используется как плейсхолдер API системы, полезна для написания скриптов.
4. Библиотеку groovy.
5. Ищет сервер IntelliJ IDEA плагина `NSMP SDK` с целью получить конфигурацию для текущего плагина и последующего выполнения конфигурации source roots.

**Пункты 2 и 3 требует указания глобальных переменных GITHUB_USERNAME и GITHUB_TOKEN, подробнее в разделе "Подключене"**.

# Подключение плагина

1. Создайте переменную среды GITHUB_USERNAME на своем ПК для текущего пользователя. Укажите в ней почту, на которую у вас зарегистрирован аккаунт Github.
2. Создайте переменную среды GITHUB_TOKEN на своем ПК для текущего пользователя. Укажите в ней персональный токен доступа. Рекомендуется использовать [fine-grained токен](https://github.com/settings/tokens?type=beta) со стандартными настройками, но должен подойти так же и [classic токен](https://github.com/settings/tokens).
3. Переменные могут не сразу инициализироваться в терминале IDE. Обычно достаточно перезапустить процесс IDE.
4. Для использования SDK рекомендуется использовать в качестве базы сгенерированный IntelliJ IDEA проект с параметрами:
- Language - groovy
- Build system - gradle 
- JDK - 21 
- Groovy SDK - 4.*
- Gradle DSL - kotlin
  Как использовать SDK в другой IDE я не знаю, тк пользуюсь только Intellij IDEA, но gradle - это универсальный инструмент, и в теории не должно быть проблемой запустить SDK в любой другой IDE, которая будет поддерживать gradle.
  Если у вас есть подобный опыт и вы хотите им поделится - пишите мне, размещу информацию тут.
5. В вашем проекте подключите мой репозиторий в ключ pluginManagement.repositories в файл settings.gradle.kts:
```settings.gradle.kts
rootProject.name = "random_scripts"
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/exeki/*")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        mavenCentral()
        //Возможно другие репозитории, например mavenLocal()
    }
}
```
6. Подключите плагин в файле build.gradle.kts вашего проекта:
```build.gradle.kts
plugins {
    id("nsmp_sdk") version "2.3.4"
}
```

Если вы не хотите указывать pluginManagement репозиторий в каждом проекте, вы можете в домашней директории gradle **{user.home}/.gradle** создать файл **init.gradle.kts**, в нем указать что для каждого вашего проекта, необходимо внедрять данный репозиторий:
```build.gradle.kts
settingsEvaluated {
    pluginManagement {
        repositories {
            maven {
                name = "exekiGithubRepo"
                url = uri("https://maven.pkg.github.com/exeki/*")
                credentials {
                    username = System.getenv("GITHUB_USERNAME")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
            mavenCentral()
            //Возможно другие репозитории, например mavenLocal()
        }
    }
}
```
После этого не нужно будет в каждом проекте указывать репозитории для плагинов, но это не исключит возможность дополнения проекта дополнительными репозиториями.

# Конфигурация

По умолчанию все функции включены и сконфигурированы таким образом, что бы не вызывать падение сборки, если вы правильно выполнили этап подключения.
В плагине предусмотрены скудные функции по конфигурации при помощи gradle extension `nsmpSdk`, пример:

```build.gradle.kts
plugins {
    id("groovy")
    id("nsmp_sdk") version "2.3.4"
}

nsmpSdk {
    disableDependencyConfiguration()
    intellijPluginIntegrationPort(8123)
}
```

## Подключение зависимостей:

Этот функционал можно отключить при помощи метода `disableDependencyConfiguration()`.
В таком случае весь менеджмент зависимостей, кроме плагина `groovy`, останется на усмотрение пользователя.
```build.gradle.kts
nsmpSdk {
    disableDependencyConfiguration()
}
```

 ## Интеграция с плагином IntelliJ IDEA NSMP SDK
Этот функционал можно отключить при помощи метода `disableIntellijPluginIntegration()`.
В случае, если вы сменили порт сервера плагина `IntelliJ IDEA NSMP SDK`, используйте метод `intellijPluginIntegrationPort(port)`
```build.gradle.kts
nsmpSdk {
    disableIntellijPluginIntegration()
    intellijPluginIntegrationPort(8123)
}
```
**ВАЖНО**: если у вас нет плагина IntelliJ IDEA NSMP SDK, сборка настроена таким образом, что бы не падать в случае его отсутствия.