plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.livetrip"
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":application:trip"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(project(":core:database"))
    implementation(libs.coroutines.android)
    implementation(libs.google.maps.compose)
}
