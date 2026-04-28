plugins {
    alias(libs.plugins.nearwake.kotlin.library)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(project(":domain:trip"))
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
