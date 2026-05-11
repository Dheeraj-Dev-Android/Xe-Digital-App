# ═══════════════════════════════════════════════════════════════════════════════
# ProGuard / R8 Rules — app.xedigital.ai
# ═══════════════════════════════════════════════════════════════════════════════

# Uncomment to preserve line numbers in crash stack traces (recommended for prod)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ── WorkManager ───────────────────────────────────────────────────────────────
# CRITICAL: Keep full class name so WorkManager can find it by string at runtime.
# -keepclassmembers alone is NOT enough — R8 will rename the class itself.

-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Explicit keep for your worker by full class name (belt + suspenders)
-keep class app.xedigital.ai.utills.ShiftTrackingWorker { *; }


# ── Data Models ───────────────────────────────────────────────────────────────
# Prevents R8 from renaming fields like startTime → a, which breaks JSON parsing

-keep class app.xedigital.ai.model.** { *; }


# ── API Layer ─────────────────────────────────────────────────────────────────
-keep class app.xedigital.ai.api.** { *; }
-keep interface app.xedigital.ai.api.APIInterface { *; }


# ── Retrofit ──────────────────────────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}


# ── OkHttp ────────────────────────────────────────────────────────────────────
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**


# ── Gson ──────────────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keep class * extends com.google.gson.TypeAdapterFactory { *; }
-keep class * extends com.google.gson.JsonDeserializer { *; }
-keep class * extends com.google.gson.JsonSerializer { *; }
-keepattributes *Annotation*
-dontwarn sun.misc.**


# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**


# ── ML Kit Face Detection ─────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**


# ── CameraX ───────────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**


# ── Filament / ARCore (unused but referenced via transitive deps) ──────────────
-dontwarn com.google.android.filament.**
-dontwarn com.google.ar.sceneform.**
-dontwarn com.google.ar.schemas.**
-keep class com.google.android.filament.** { *; }
-keep class com.google.ar.sceneform.** { *; }


# ── Glide ─────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}


# ── Lottie ────────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**


# ── Biometric ─────────────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }


# ── General Android / Reflection Safety ──────────────────────────────────────
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}