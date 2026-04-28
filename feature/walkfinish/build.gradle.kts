plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.walkfinish"
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":domain:location"))
    implementation(project(":domain:trip"))
    implementation(project(":core:database"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
