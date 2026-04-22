plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.onboarding"
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":core:datastore"))
}
