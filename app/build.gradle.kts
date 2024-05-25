import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    //extra
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    //kotlin("kapt")
}

android {
    namespace = "com.example.nework"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nework"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        //MAPKIT
        val properties = Properties()
        val file = rootProject.file("maps.properties")
        if (file.exists()) {
            properties.load(file.inputStream())
        }
        val mapkitApiKey = properties.getProperty("MAPKIT_API_KEY","")
        buildConfigField ("String", "MAPKIT_API_KEY", "\"${mapkitApiKey}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["useClearTextTraffic"] = false
            buildConfigField ("String", "BASE_URL", "\"https://netomedia.ru\"")
        }
        debug {
            manifestPlaceholders["useClearTextTraffic"] = true
            buildConfigField ("String", "BASE_URL", "\"http://94.228.125.136:8080/api\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    //default libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //extra libs, ksp-version = "1.9.22-1.0.17"
    val room_version = "2.6.1"
    implementation ("androidx.room:room-runtime:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")
    implementation ("androidx.room:room-paging:$room_version")
    ksp ("androidx.room:room-compiler:$room_version")

    val gson_version = "2.10.1"
    implementation ("com.google.code.gson:gson:$gson_version")

    val okhttp_version = "4.12.0"
    implementation ("com.squareup.okhttp3:okhttp:$okhttp_version")
    implementation ("com.squareup.okhttp3:logging-interceptor:$okhttp_version")

    val retrofit_version = "2.9.0"
    implementation ("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation ("com.squareup.retrofit2:converter-gson:$retrofit_version")

    //hilt-version = "2.49"
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //https://github.com/bumptech/glide
    val glide_version = "4.16.0"
    implementation ("com.github.bumptech.glide:glide:$glide_version")

    //https://github.com/Dhaval2404/ImagePicker
    val imagepicker_version = "2.1"
    implementation ("com.github.dhaval2404:imagepicker:$imagepicker_version")

    val swipe_refresh_version = "1.1.0"
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:$swipe_refresh_version")

    val coroutines_version = "1.7.3"
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")

    val nav_version = "2.7.5"
    implementation ("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation ("androidx.navigation:navigation-fragment-ktx:$nav_version")

    val material3_version = "1.2.1"
    implementation ("androidx.compose.material3:material3:$material3_version")

    val coordinatorlayout_version = "1.2.0"
    implementation ("androidx.coordinatorlayout:coordinatorlayout:$coordinatorlayout_version")

    val paging_version = "3.2.1"
    implementation ("androidx.paging:paging-runtime-ktx:$paging_version")

    val yandex_map_version =  "4.6.1-lite"
    implementation ("com.yandex.android:maps.mobile:$yandex_map_version")
}