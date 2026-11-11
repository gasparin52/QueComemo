import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name).orEmpty()

fun String.asBuildConfigString(): String {
    return "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.quecomemo.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.quecomemo.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "SPOONACULAR_API_KEY",
            localProperty("SPOONACULAR_API_KEY").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "GEMINI_MODEL",
            localProperty("GEMINI_MODEL").ifBlank { "gemini-3.8-flash" }.asBuildConfigString()
        )
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

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.material)
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ai)
    implementation(libs.google.play.services.auth)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)

    kapt(libs.hilt.android.compiler)
    kapt(libs.room.compiler)
    debugImplementation(libs.compose.ui.tooling)
}

kapt {
    correctErrorTypes = true
}
