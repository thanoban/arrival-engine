plugins {
    alias(libs.plugins.nearwake.android.library.compose)
}

android {
    namespace = "com.nearwake.core.designsystem"
}

dependencies {
    implementation(libs.compose.material.icons)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.text.google.fonts)
    api(libs.compose.material3)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
