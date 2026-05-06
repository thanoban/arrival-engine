plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.tripsetup"
}

dependencies {
    implementation(project(":application:trip"))
    implementation(project(":domain:trip"))
    implementation(project(":domain:routing"))
    implementation(project(":core:datastore"))
    implementation(libs.coroutines.android)
}
