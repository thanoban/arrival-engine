import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun configuredMapsApiKey(): String =
    ((findProperty("MAPS_API_KEY") as? String) ?: localProperties.getProperty("MAPS_API_KEY"))
        ?.takeUnless { value -> value.isBlank() || value == "REPLACE_WITH_YOUR_KEY" }
        .orEmpty()

fun configuredSentryDsn(): String =
    ((findProperty("SENTRY_DSN") as? String) ?: localProperties.getProperty("SENTRY_DSN"))
        ?.trim()
        .orEmpty()

plugins {
    alias(libs.plugins.nearwake.android.application)
    alias(libs.plugins.nearwake.android.application.compose)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.app"

    defaultConfig {
        applicationId = "com.nearwake.app"
        versionCode = 1
        versionName = "0.1.0"

        manifestPlaceholders["MAPS_API_KEY"] = configuredMapsApiKey()
        buildConfigField("String", "SENTRY_DSN", "\"${configuredSentryDsn()}\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        create("qa") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":application:trip"))
    implementation(project(":ports:analytics"))

    // Core
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:network"))

    // Domain
    implementation(project(":domain:trip"))
    implementation(project(":domain:location"))
    implementation(project(":domain:routing"))

    // Data
    implementation(project(":data:location"))
    implementation(project(":data:motion"))
    implementation(project(":data:routing"))
    implementation(project(":data:alerts"))
    implementation(project(":data:analytics"))

    // Features
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:permissions"))
    implementation(project(":feature:places"))
    implementation(project(":feature:tripsetup"))
    implementation(project(":feature:livetrip"))
    implementation(project(":feature:alerts"))
    implementation(project(":feature:history"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:diagnostics"))
    implementation(project(":feature:walkfinish"))
    implementation(project(":feature:companion"))
    implementation(project(":feature:departure"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)

    // Logging
    implementation(libs.timber)
    implementation(libs.sentry.android)

    // Coroutines
    implementation(libs.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    ksp(libs.hilt.work.compiler)
}
