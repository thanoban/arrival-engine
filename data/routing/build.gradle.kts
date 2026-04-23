plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.data.routing"

    defaultConfig {
        val mapsApiKey = project.findProperty("MAPS_API_KEY")?.toString().orEmpty()
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(project(":domain:trip"))

    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coroutines.android)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
}
