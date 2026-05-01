import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.3.0"
}

compose.resources { }

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


