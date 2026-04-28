plugins {
    alias(libs.plugins.nearwake.kotlin.library)
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(project(":domain:trip"))
    implementation(project(":ports:persistence"))
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
