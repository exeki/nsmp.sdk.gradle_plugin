import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
    id("java-gradle-plugin")
}

group = "ru.kazantsev.nsmp.sdk"
version = "2.2.3"

val githubUsername: Provider<String?> = providers.environmentVariable("GITHUB_USERNAME")
val githubToken: Provider<String?> = providers.environmentVariable("GITHUB_TOKEN")

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/exeki/*")
        credentials {
            username = githubUsername.orNull
            password = githubToken.orNull
        }
    }
}

gradlePlugin {
    plugins {
        create("nsmp_sdk") {
            id = "nsmp_sdk"
            version = project.version
            group = project.group
            implementationClass = "ru.kazantsev.nsmp.sdk.gradle_plugin.PluginImplementation"
        }
    }
}

tasks {

    compileJava {
        targetCompatibility = JavaVersion.VERSION_21.majorVersion
        sourceCompatibility = JavaVersion.VERSION_21.majorVersion
    }

    withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    test {
        useJUnitPlatform()
    }

}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/exeki/nsmp.sdk.gradle_plugin")
            credentials {
                username = githubUsername.orNull
                password = githubToken.orNull
            }
        }
    }
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}
