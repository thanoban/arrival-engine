plugins {
    alias(libs.plugins.nearwake.android.library)
    alias(libs.plugins.nearwake.android.hilt)
}

android {
    namespace = "com.nearwake.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))

    api(libs.junit)
    api(libs.mockk)
    api(libs.mockk.android)
    api(libs.turbine)
    api(libs.truth)
    api(libs.coroutines.test)
    api(libs.androidx.test.core)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.rules)
    api(libs.workmanager.testing)
    api(libs.room.testing)
}
