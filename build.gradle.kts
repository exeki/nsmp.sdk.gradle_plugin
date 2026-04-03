plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("maven-publish")
    id("java-gradle-plugin")
    id("org.jetbrains.dokka") version "2.1.0"
}

group = "ru.kazantsev.nsd.sdk"
version = "2.0.9"

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/exeki/*")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

gradlePlugin {
    plugins {
        create("nsd_sdk") {
            id = "nsd_sdk"
            version = project.version
            group = project.group
            implementationClass = "ru.kazantsev.nsd.sdk.gradle_plugin.Plugin"
        }
    }
}

tasks {

    compileJava {
        targetCompatibility = JavaVersion.VERSION_21.majorVersion
        sourceCompatibility = JavaVersion.VERSION_21.majorVersion
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }

    test {
        useJUnitPlatform()
    }

}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["kotlin"])
            artifact(tasks.named("sourcesJar"))
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/exeki/nsd.sdk.gradle_plugin")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    implementation("ru.kazantsev.nsd:basic_api_connector:1.5.2")
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}
