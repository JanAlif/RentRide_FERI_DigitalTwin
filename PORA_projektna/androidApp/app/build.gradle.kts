plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.poraproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.poraproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding{
        enable = true
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

    packaging {
        resources {
            excludes.addAll(
                arrayOf(
                    "META-INF/native-image/org.mongodb/bson/native-image.properties"
                )
            )
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":myCore"))
    implementation (libs.play.services.maps.v1810)
    implementation (libs.okhttp)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.okhttp.v490) // Or the version you are using
    // Kotlin coroutine dependency
    implementation(libs.kotlinx.coroutines.core)

    // MongoDB Kotlin driver dependency
    implementation(libs.mongodb.driver.kotlin.coroutine)
    implementation (libs.mongo.kotlin.extensions)

}