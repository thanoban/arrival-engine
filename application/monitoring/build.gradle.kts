plugins {
    alias(libs.plugins.nearwake.kotlin.library)
}

dependencies {
    implementation(project(":ports:monitoring"))
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.coroutines.core)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
