plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.danyjoe.lebanesemonateries"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.danyjoe.lebanesemonateries"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Firebase
//    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.auth.ktx)
//    implementation(libs.firebase.firestore.ktx)
//    implementation(libs.firebase.storage.ktx)      LATEST CHANGE
    implementation(platform(libs.firebase.bom.v3270)) // Use latest BoM

    // Firestore with KTX (Kotlin extensions)
    implementation(libs.google.firebase.firestore.ktx) // No version needed (BoM manages it)

    // Other Firebase dependencies (no versions)
    implementation(libs.google.firebase.auth.ktx)

    // Phone number input
    implementation(libs.ccp) // Country Code Picker

    // Image loading
    implementation(libs.glide)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    // Google Maps
//    implementation(libs.play.services.maps)
//    implementation(libs.play.services.location)

    implementation(libs.androidx.drawerlayout)
    implementation(libs.material.v1100)


    implementation (libs.glide.v4151)
    annotationProcessor (libs.compiler)


    implementation(libs.play.services.maps.v1820)
    implementation(libs.play.services.location.v2130)
//    implementation(libs.firebase.firestore)
//    implementation(libs.firebase.auth)


    implementation(platform(libs.firebase.bom.v3270)) // Use latest BoM

    // Firestore with KTX (Kotlin extensions)
    implementation(libs.google.firebase.firestore.ktx) // No version needed (BoM manages it)

    // Other Firebase dependencies (no versions)
    implementation(libs.google.firebase.auth.ktx)

    implementation (libs.google.firebase.storage.ktx)

    implementation(libs.gson)


    // Kotlin coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    // Coroutines with Firebase
    implementation (libs.kotlinx.coroutines.play.services)




}