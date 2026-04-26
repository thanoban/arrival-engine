import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun configuredMapsApiKey(): String =
    ((findProperty("MAPS_API_KEY") as? String) ?: localProperties.getProperty("MAPS_API_KEY"))
        ?.takeUnless { value -> value.isBlank() || value == "REPLACE_WITH_YOUR_KEY" }
        .orEmpty()

plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.data.location"

    defaultConfig {
        buildConfigField("String", "MAPS_API_KEY", "\"${configuredMapsApiKey()}\"")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":domain:location"))
    implementation(project(":domain:trip"))

    implementation(libs.play.services.location)
    implementation(libs.google.places)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
    implementation(libs.kotlinx.datetime)
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}
