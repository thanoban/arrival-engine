# Preserve metadata used by Compose, Room, Hilt, and serialization at runtime.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room uses generated implementations and annotated schemas.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Hilt and WorkManager integrations rely on generated entry points and workers.
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep Kotlin serialization companion serializers that are resolved reflectively.
-if @kotlinx.serialization.Serializable class **
-keep class <1>$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclassmembers class **$$serializer {
    *;
}

# Google Places pulls Guava annotations that are safe to ignore during shrinking.
-dontwarn com.google.j2objc.annotations.ReflectionSupport
-dontwarn com.google.j2objc.annotations.RetainedWith
