plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.alerts"
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":application:trip"))
    implementation(project(":domain:location"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:routing"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
