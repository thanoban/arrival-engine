import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nearwake.android.library.compose")
            pluginManager.apply("nearwake.android.hilt")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:common"))
                add("implementation", "androidx.hilt:hilt-navigation-compose:1.2.0")
                add("implementation", "androidx.navigation:navigation-compose:2.8.5")
                add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", project(":core:testing"))
            }
        }
    }
}
