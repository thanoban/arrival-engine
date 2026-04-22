plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.data.routing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))

    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coroutines.android)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
