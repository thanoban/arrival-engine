plugins {
    alias(libs.plugins.nearwake.android.library.compose)
}

android {
    namespace = "com.nearwake.core.designsystem"
}

dependencies {
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    api(libs.compose.material3)
}
