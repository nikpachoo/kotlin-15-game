import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.3.0"
}

compose.resources { }

abstract class GenerateBuildConfigTask : DefaultTask() {
    @get:Input
    abstract val env: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile.resolve("com/glycin/koita")
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            """
            |package com.glycin.koita
            |
            |object BuildConfig {
            |    const val BUILD_ENV: String = "${env.get()}"
            |    val isDev: Boolean = BUILD_ENV == "dev"
            |    val isTest: Boolean = BUILD_ENV == "test"
            |    val isProd: Boolean = BUILD_ENV == "prod"
            |}
            """.trimMargin()
        )
    }
}

val generateBuildConfig by tasks.registering(GenerateBuildConfigTask::class) {
    env.set(providers.gradleProperty("buildEnv").getOrElse("dev"))
    outputDir.set(layout.buildDirectory.dir("generated/buildconfig"))
}

kotlin {
    //TODO: Disabling so syntax highlighting works correct again, see https://youtrack.jetbrains.com/projects/KTIJ/issues/KTIJ-37075/K2-False-positive-on-ByteArray-methods-call-Cannot-access-Cloneable-which-is-a-supertype-of-ByteArray
    /*js {
        browser()
        binaries.executable()
    }*/

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateBuildConfig.map { it.outputs.files.singleFile })
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.ktor:ktor-client-core:3.1.1")
            implementation("io.ktor:ktor-client-js:3.1.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}


