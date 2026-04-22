plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.data.alerts"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(project(":data:location"))
    implementation(project(":data:motion"))
    implementation(project(":data:analytics"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.location)
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
