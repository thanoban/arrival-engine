plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.companion"
}

dependencies {
    implementation(project(":core:database"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
