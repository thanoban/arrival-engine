fun configuredGitSha(): String =
    runCatching {
        providers.exec {
            commandLine("git", "rev-parse", "--short=12", "HEAD")
        }.standardOutput.asText.get().trim()
    }.getOrDefault("unknown")

plugins {
    alias(libs.plugins.nearwake.android.feature)
}

android {
    namespace = "com.nearwake.feature.diagnostics"

    defaultConfig {
        buildConfigField("String", "BUILD_GIT_SHA", "\"${configuredGitSha()}\"")
    }
}

dependencies {
    implementation(project(":domain:trip"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
