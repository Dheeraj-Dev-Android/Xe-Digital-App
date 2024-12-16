plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "app.xedigital.ai"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.xedigital.ai"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true

    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.legacy.support.v4)
    implementation(libs.activity)
    implementation(libs.filament.android)
    implementation(libs.tracing.perfetto.handshake)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.protolite.well.known.types)
    implementation(libs.media3.common)
    implementation(libs.play.services.location)
    implementation(libs.compilercommon)
    implementation(libs.cronet.embedded)
    implementation(libs.compiler)
    implementation(libs.transport.api)
    implementation(libs.core.i18n)
    implementation(libs.leanback)
    implementation(libs.rendering)
    implementation(libs.ui.text.android)
    implementation(libs.firebase.vertexai)
    implementation(libs.impress)
//    implementation(libs.firebase.inappmessaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converterGson)
    implementation(libs.cardView)
    implementation(libs.mpandroidchart)
    implementation(libs.recyclerView)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.location)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
}