plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)
}

android {
    namespace = "app.xedigital.ai"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.xedigital.ai"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    signingConfigs {
        create("release") {
            //Replace path, password and alias with your credentials
            storeFile = file("C:\\Users\\Dheeraj.t\\AndroidStudioProjects\\Xe Digital\\key.jks")
            storePassword = "xedigital"
            keyAlias = "key"
            keyPassword = "xedigital"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.engage.core)
    implementation(libs.car.ui.lib)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
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
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.airbnb.android:lottie:5.2.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}