plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.diagnostics"
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":core:database"))
}
