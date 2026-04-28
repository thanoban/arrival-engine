pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NearWake"

include(":app")

// Application
include(":application:monitoring")

// Core
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:network")
include(":core:testing")
include(":core:ui")

// Domain
include(":domain:trip")
include(":domain:location")
include(":domain:routing")
include(":domain:commute")

// Ports
include(":ports:monitoring")

// Data
include(":data:location")
include(":data:motion")
include(":data:routing")
include(":data:alerts")
include(":data:analytics")
include(":data:patterns")

// Features
include(":feature:onboarding")
include(":feature:permissions")
include(":feature:places")
include(":feature:tripsetup")
include(":feature:livetrip")
include(":feature:alerts")
include(":feature:history")
include(":feature:settings")
include(":feature:diagnostics")
include(":feature:walkfinish")
include(":feature:companion")
include(":feature:departure")
