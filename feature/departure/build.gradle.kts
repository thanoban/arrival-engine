plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.departure"
}

dependencies {
    implementation(project(":domain:commute"))
    implementation(project(":data:patterns"))
    implementation(project(":core:database"))
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
}
