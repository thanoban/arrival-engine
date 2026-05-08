plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.places"
}

dependencies {
    implementation(project(":application:trip"))
    implementation(project(":domain:location"))
    implementation(libs.coroutines.android)
}
