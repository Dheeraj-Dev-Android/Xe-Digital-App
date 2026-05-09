# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Ignore missing Filament classes (ARCore/Sceneform dependencies)
-dontwarn com.google.android.filament.**
-dontwarn com.google.ar.sceneform.**
-dontwarn com.google.ar.schemas.**

# Keep Sceneform and Filament classes from being stripped
-keep class com.google.android.filament.** { *; }
-keep class com.google.ar.sceneform.** { *; }

# Keep your data models so GSON can map them
-keep class app.xedigital.ai.model.** { *; }

# Keep Retrofit and OkHttp internal classes
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# Keep GSON specific attributes
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.TypeAdapter

# 1. Prevent WorkManager from losing your Worker class
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 2. Prevent Retrofit/Gson from renaming your API response fields
# Replace 'app.xedigital.ai.model' with your actual model package
-keep class app.xedigital.ai.model.** { *; }

# 3. Keep the API Interface methods
-keep interface app.xedigital.ai.api.APIInterface { *; }

# 1. Keep your Worker class so WorkManager can find it by name
-keep class app.xedigital.ai.utills.ShiftTrackingWorker { *; }

# 2. Keep all Workers in general (best practice)
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 3. Keep your API Data Models (CRITICAL)
# If R8 renames 'startTime' to 'a', the JSON parsing will fail and return null
-keep class app.xedigital.ai.model.** { *; }

# 4. Keep Retrofit and Gson internal structures
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.google.gson.** { *; }