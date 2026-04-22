plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.permissions"
}

dependencies {
    implementation(project(":domain:location"))
    implementation(project(":core:datastore"))
}
