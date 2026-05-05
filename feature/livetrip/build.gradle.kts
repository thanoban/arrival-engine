plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.livetrip"
}

dependencies {
    implementation(project(":application:trip"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:routing"))
    implementation(libs.coroutines.android)
    implementation(libs.google.maps.compose)
}
