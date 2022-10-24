import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "io.wollinger"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets {
    create("api")
    create("cli") {
        compileClasspath = sourceSets.main.get().compileClasspath
        runtimeClasspath = sourceSets.main.get().runtimeClasspath
        dependencies {
            implementation(sourceSets["api"].output)
        }
    }
}

tasks.create("api", Jar::class) {
    this.group = "Zipper"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets["api"].output)
    archiveBaseName.set("ZipperAPI.jar")
}

tasks.create("cli", Jar::class) {
    this.group = "Zipper"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("ZipperCLI.jar")

    manifest { attributes["Main-Class"] = "io.wollinger.zipper.MainKt" }

    dependsOn(configurations.runtimeClasspath)
    from(sourceSets["api"].output)
    from(sourceSets["cli"].output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}