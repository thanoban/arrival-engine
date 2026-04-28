plugins {
    alias(libs.plugins.nearwake.kotlin.library)
}

dependencies {
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
