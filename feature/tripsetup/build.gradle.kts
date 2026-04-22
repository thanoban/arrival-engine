plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.tripsetup"
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
}
