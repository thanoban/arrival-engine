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
    compileOnly("com.android.tools.build:gradle:${libs.plugins.android.application.get().version}")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.plugins.kotlin.android.get().version}")
    compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:${libs.plugins.ksp.get().version}")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:${libs.plugins.hilt.get().version}")
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
