plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.aiexpensetracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.aiexpensetracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Android UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Charts
    implementation(libs.mpchart)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.play.services.auth)
    implementation("com.google.firebase:firebase-firestore")
}