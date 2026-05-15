import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.isFile) {
        keystorePropertiesFile.inputStream().use(::load)
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

fun configuredPrivacyPolicyUrl(): String =
    ((findProperty("PRIVACY_POLICY_URL") as? String) ?: localProperties.getProperty("PRIVACY_POLICY_URL"))
        ?.trim()
        .orEmpty()

fun configuredReleaseSigning(propertyName: String): String =
    ((findProperty(propertyName) as? String) ?: keystoreProperties.getProperty(propertyName))
        ?.trim()
        .orEmpty()

val releaseSigningStoreFile = configuredReleaseSigning("storeFile")
val releaseSigningStorePassword = configuredReleaseSigning("storePassword")
val releaseSigningKeyAlias = configuredReleaseSigning("keyAlias")
val releaseSigningKeyPassword = configuredReleaseSigning("keyPassword")
val releaseSigningConfigured =
    listOf(
        releaseSigningStoreFile,
        releaseSigningStorePassword,
        releaseSigningKeyAlias,
        releaseSigningKeyPassword,
    ).all(String::isNotBlank)

val releaseSigningError = """
    Release signing is not configured.
    
    Create an untracked keystore.properties file at the repo root with:
    storeFile=C:\\path\\to\\nearwake-release.keystore
    storePassword=REPLACE_ME
    keyAlias=nearwake
    keyPassword=REPLACE_ME
    
    See APP_SIGNING_SETUP_GUIDE.md for the full setup steps.
""".trimIndent()

plugins {
    alias(libs.plugins.nearwake.android.application)
    alias(libs.plugins.nearwake.android.application.compose)
    alias(libs.plugins.nearwake.android.hilt)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.nearwake.app"

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = rootProject.file(releaseSigningStoreFile)
                storePassword = releaseSigningStorePassword
                keyAlias = releaseSigningKeyAlias
                keyPassword = releaseSigningKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.nearwake.app"
        versionCode = 1
        versionName = "0.1.0"

        manifestPlaceholders["MAPS_API_KEY"] = configuredMapsApiKey()
        manifestPlaceholders["PRIVACY_POLICY_URL"] = configuredPrivacyPolicyUrl()
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
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

val requestedTasks = gradle.startParameter.taskNames.map { it.lowercase() }
val releaseSigningRequired = requestedTasks.any { taskName ->
    "release" in taskName &&
        listOf("assemble", "bundle", "package", "install").any(taskName::contains)
}

if (releaseSigningRequired && !releaseSigningConfigured) {
    throw GradleException(releaseSigningError)
}

dependencies {
    implementation(project(":application:monitoring"))
    implementation(project(":application:trip"))
    implementation(project(":ports:analytics"))
    debugImplementation(project(":ports:persistence"))
    debugImplementation(libs.room.runtime)

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
    implementation(libs.compose.material.icons)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.profileinstaller)

    // Logging
    implementation(libs.timber)
    implementation(libs.sentry.android)

    // Coroutines
    implementation(libs.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    add("baselineProfile", project(":core:benchmark"))

    ksp(libs.hilt.work.compiler)
}
