plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.livetrip"
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))
    implementation(project(":core:database"))
    implementation(project(":data:alerts"))
    implementation(libs.coroutines.android)
    implementation(libs.google.maps.compose)
}
