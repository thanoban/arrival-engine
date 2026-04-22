plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain:trip"))
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.coroutines.test)
}
