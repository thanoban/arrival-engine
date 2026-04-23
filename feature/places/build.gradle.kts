plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.places"
}

dependencies {
    implementation(project(":domain:location"))
    implementation(project(":core:database"))
    implementation(libs.google.places)
    implementation(libs.j2objc.annotations)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
    implementation(libs.kotlinx.datetime)
}
