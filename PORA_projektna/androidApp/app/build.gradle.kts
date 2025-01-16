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
            excludes += "META-INF/native-image/**"
        }
    }

    /*packaging {
        resources {
            excludes.addAll(
                arrayOf(
                    "META-INF/native-image/org.mongodb/bson/native-image.properties"
                )
            )
        }
    }*/
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
    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.2.0")
    implementation("org.mongodb:bson-kotlin:5.2.0")
    implementation ("org.mongodb:mongodb-driver-reactivestreams:5.2.1")
    implementation ("dnsjava:dnsjava:3.6.2")
    implementation ("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation ("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

}