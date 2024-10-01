plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")

}

android {
    namespace = "com.univalle.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.univalle.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Dependencias existentes...
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Dependencias para Ktor
    implementation ("io.ktor:ktor-server-core:2.1.3")
    implementation ("io.ktor:ktor-server-cio:2.1.3")// CIO es más ligero, perfecto para este tipo de tareas
    implementation ("io.ktor:ktor-server-content-negotiation:2.1.3")
    implementation ("io.ktor:ktor-serialization:2.1.3")
    implementation ("io.ktor:ktor-server-html-builder:2.1.3")
    implementation ("ch.qos.logback:logback-classic:1.2.10" )// Para logging, si es necesario

    // Dependencias para Room
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.lifecycle.service)
    kapt ("androidx.room:room-compiler:2.6.1") // Kapt para el procesamiento de anotaciones de Room
    implementation ("androidx.room:room-ktx:2.6.1") // Para usar corrutinas con Room

    // Dependencias para Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dependencia de Gson
    implementation("com.google.code.gson:gson:2.8.9")

    // Koin para Ktor
    implementation("io.insert-koin:koin-ktor:3.4.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.4.0")

    // navigation compose
    implementation("androidx.navigation:navigation-compose:2.7.7")


}

// Add the kapt block for Room schema export
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}