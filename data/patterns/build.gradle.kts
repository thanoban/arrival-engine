plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.data.patterns"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":domain:commute"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}
