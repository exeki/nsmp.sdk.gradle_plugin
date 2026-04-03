plugins {
    id("java")
    id("nsd_sdk") version "2.0.8"

}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

repositories {
    mavenCentral()
    mavenLocal()
}

sdk {
    setSendFilePath("src/main/groovy/console.groovy")
    setInstallation("EXEKI1")
}
