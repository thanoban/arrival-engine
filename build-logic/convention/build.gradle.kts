import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.nearwake.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.plugins.android.application.get().let { "${it.pluginId}:${it.version}" })
    compileOnly(libs.plugins.android.library.get().let { "${it.pluginId}:${it.version}" })
    compileOnly(libs.plugins.kotlin.android.get().let { "org.jetbrains.kotlin:kotlin-gradle-plugin:${it.version}" })
    compileOnly(libs.plugins.kotlin.jvm.get().let { "org.jetbrains.kotlin:kotlin-gradle-plugin:${it.version}" })
    compileOnly(libs.plugins.ksp.get().let { "com.google.devtools.ksp:symbol-processing-gradle-plugin:${it.version}" })
    compileOnly(libs.plugins.hilt.get().let { "com.google.dagger:hilt-android-gradle-plugin:${it.version}" })
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nearwake.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "nearwake.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "nearwake.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "nearwake.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "nearwake.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidFeature") {
            id = "nearwake.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "nearwake.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
    }
}
