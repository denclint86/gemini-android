plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

//    id("com.google.gms.google-services")
}

android {
    namespace = "com.tv.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tv.gemini"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {
    implementation(project(":shizuku"))
    implementation(project(":tool"))
    implementation(project(":settings"))
    implementation(project(":utils"))

//    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-vertexai")

    implementation("com.google.ai.client.generativeai:common:niki")
    implementation("com.google.ai.client.generativeai:generativeai:niki")

    implementation(libs.shizuku.provider)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.runtime)

    implementation(libs.markwon.core)

    implementation(libs.google.gson)

    implementation(libs.converter.gson)
    implementation(libs.retrofit)

    implementation(libs.zephyr.vbclass)
    implementation(libs.zephyr.scaling.layout)
    implementation(libs.zephyr.global.values)
    implementation(libs.zephyr.net)
    implementation(libs.zephyr.log)
    implementation(libs.zephyr.extension)

    implementation(libs.material)
    implementation(kotlin("reflect"))
}