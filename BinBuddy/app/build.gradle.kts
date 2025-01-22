plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.binbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.binbuddy"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui) // Ensure correct version in libs.versions.toml
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.play.services.maps)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.google.firebase:firebase-firestore:24.8.1")
    implementation ("com.google.firebase:firebase-analytics:21.3.0")
    implementation ("com.google.android.gms:play-services-location:19.0.1")
    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")
    //implementation ("com.google.android.gms:play-services-auth:20.0.1")
    implementation ("com.google.firebase:firebase-auth:21.0.3")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-firestore:24.7.0")



}



