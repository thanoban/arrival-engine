plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.settings"
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":core:datastore"))
    implementation(libs.coroutines.android)
}
