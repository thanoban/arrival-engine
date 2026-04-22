plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
}

android {
    namespace = "com.nearwake.data.motion"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain:location"))
    implementation(project(":domain:trip"))

    implementation(libs.play.services.location)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)
    implementation(libs.kotlinx.datetime)
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
