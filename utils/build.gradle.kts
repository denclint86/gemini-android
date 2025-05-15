plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.tv.utils"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.google.gson)

    implementation(project(":shizuku"))
    implementation(kotlin("reflect"))
    implementation("com.google.ai.client.generativeai:common:niki")
    implementation("com.google.ai.client.generativeai:generativeai:niki")

    implementation(libs.zephyr.global.values)
    implementation(libs.zephyr.vbclass)
    implementation(libs.zephyr.scaling.layout)
    implementation(libs.zephyr.net)
    implementation(libs.zephyr.log)
    implementation(libs.zephyr.extension)

    implementation(libs.material)
}