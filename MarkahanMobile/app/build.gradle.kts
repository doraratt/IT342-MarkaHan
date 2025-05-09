plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.markahanmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.markahanmobile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true // Explicitly enable debug mode
//            buildConfigField("String", "BACKEND_URL", "\"http://10.0.2.2:8080/\"")
            buildConfigField("String", "BACKEND_URL", "\"https://rendeer-ya43.onrender.com/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BACKEND_URL", "\"https://rendeer-ya43.onrender.com/\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity)
    implementation(libs.generativeai)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation (libs.gson)

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("androidx.gridlayout:gridlayout:1.0.0")

    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}