# Corrections & Issues Log

> Apply these fixes when development resumes. Do NOT touch code until ready to implement.

---

## C-001 — Dead/broken code in `AndroidApplicationComposeConventionPlugin.kt`

**File:** `build-logic/convention/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt`  
**Lines:** 18–19  
**Severity:** Compile error — will break build-logic compilation

**Problem:**
```kotlin
val libs = extensions.getByType<org.gradle.api.plugins.ExtraPropertiesExtension>()
    .let { rootProject.extensions.getByType(org.gradle.api.artifacts.dsl.DependencyHandler::class) }
```
- `DependencyHandler` is not registered as a project extension — `extensions.getByType()` will throw at runtime
- The variable `libs` is never used anywhere in the function
- The `getByType` import is also unused after this is removed

**Fix:** Delete lines 18–19 and remove the unused `import org.gradle.kotlin.dsl.getByType` import.

**Result after fix:**
```kotlin
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nearwake.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                configureAndroidCompose(this)
            }

            dependencies {
                val bom = project.dependencies.platform("androidx.compose:compose-bom:2024.12.01")
                add("implementation", bom)
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("implementation", "androidx.compose.material3:material3")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
            }
        }
    }
}
```

---

## C-002 — JUnit version inconsistency across modules

**Severity:** Test configuration mismatch — some modules use JUnit 4, some use JUnit 5

**Situation:**
- `domain/trip`, `core/database`, `core/datastore`, `data/routing` → switched to **JUnit 5** (`libs.junit.jupiter`)
- `core/common`, `core/testing`, `data/location`, `data/motion`, `data/analytics` → still on **JUnit 4** (`libs.junit`)
- `KotlinLibraryConventionPlugin.kt` already has `useJUnitPlatform()` → JUnit 5 works for `:domain:*` modules
- Android library modules use `testInstrumentationRunner` — JUnit 5 on Android requires an additional runner dependency

**Decision needed (pick one):**
- **Option A (Recommended):** Standardize all modules on JUnit 5. Add `useJUnitPlatform()` to Android test tasks in `AndroidLibraryConventionPlugin`. Add `junit-jupiter-engine` to all test runtimes.
- **Option B:** Standardize all modules back on JUnit 4. Simpler for Android instrumentation tests.

**Fix for Option A — update `AndroidLibraryConventionPlugin.kt`:**
```kotlin
// Add inside configureAndroidCommon or in the plugin body:
tasks.withType<Test> {
    useJUnitPlatform()
}
```
And update remaining modules (`core/common`, `data/location`, etc.) to use `libs.junit.jupiter` + `libs.junit.jupiter.engine`.

---

## C-003 — `core/testing` still references JUnit 4

**File:** `core/testing/build.gradle.kts`  
**Line:** `api(libs.junit)`  
**Severity:** Low — functional but inconsistent if choosing JUnit 5 (C-002 Option A)

**Fix (if Option A chosen):** Replace `api(libs.junit)` with:
```kotlin
api(libs.junit.jupiter)
api(libs.junit.jupiter.engine)
```

---

## C-004 — `data/routing` references `libs.junit.jupiter.engine` but uses `testRuntimeOnly`

**File:** `data/routing/build.gradle.kts`  
**Lines:** 25–26  
**Severity:** Informational — correct pattern for JUnit 5, but needs to be applied consistently

```kotlin
testImplementation(libs.junit.jupiter)
testRuntimeOnly(libs.junit.jupiter.engine)   // ← correct: engine is runtime-only
```
This is the correct JUnit 5 split. Apply the same pattern to all modules when standardizing.

---

## Status

| ID | File | Severity | Status |
|----|------|----------|--------|
| C-001 | `AndroidApplicationComposeConventionPlugin.kt` | **Compile error** | Pending fix |
| C-002 | Multiple modules | Medium | Decision needed |
| C-003 | `core/testing/build.gradle.kts` | Low | Depends on C-002 |
| C-004 | `data/routing/build.gradle.kts` | Informational | Correct pattern to follow |
