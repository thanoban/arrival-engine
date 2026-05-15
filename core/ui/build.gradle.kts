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
    api(libs.compose.material.icons)
    implementation(libs.coil.compose)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
