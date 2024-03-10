plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.imagesgallery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imagesgallery"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        renderscriptTargetApi  = 21
        renderscriptSupportModeEnabled   = true


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.facebook.android:facebook-login:latest.release")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.burhanrashid52:photoeditor:3.0.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
    testImplementation("junit:junit:4.13.2")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Add the AAR dependency
    implementation(files("./libs/ds-photo-editor-sdk-v10.aar"))

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.appcompat:appcompat:1.1.0")

    implementation("io.reactivex.rxjava2:rxjava:2.1.0")

    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("me.dm7.barcodescanner:zxing:1.9")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.4.1")
}