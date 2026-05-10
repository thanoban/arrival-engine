plugins {
    alias(libs.plugins.nearwake.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:remoteconfig"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
