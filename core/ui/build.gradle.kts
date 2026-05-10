plugins {
    alias(libs.plugins.nearwake.android.library.compose)
}

android {
    namespace = "com.nearwake.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.compose.material.icons)
    implementation(libs.coil.compose)
}
