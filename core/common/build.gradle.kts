plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.core.common"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
    api(libs.kotlinx.datetime)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.coroutines.test)
}
