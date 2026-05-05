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