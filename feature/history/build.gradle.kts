plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.history"
}

dependencies {
    implementation(project(":application:trip"))
    implementation(libs.coroutines.android)
}
