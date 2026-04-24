plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.alerts"
}

dependencies {
    implementation(project(":domain:location"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:routing"))
    implementation(project(":core:database"))
    implementation(project(":data:alerts"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
