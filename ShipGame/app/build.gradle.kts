plugins {
    id("com.android.application")
    kotlin("android") // Elimina la referencia al plugin de Compose
}

android {
    compileSdk = 33

    // Especificar el namespace aquí
    namespace = "com.example.shipgame"  // Agregar el namespace

    defaultConfig {
        applicationId = "com.example.shipgame"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    // Elimina el bloque de 'buildFeatures' y 'composeOptions'
    buildFeatures {
        compose = false // Deshabilitar Compose
    }

    kotlinOptions {
        jvmTarget = "1.8"  // Establecer la versión JVM para Kotlin
    }
}

dependencies {
    // Elimina las dependencias de Jetpack Compose
    // Si no estás usando Compose, puedes eliminar todo lo relacionado con ello
    // implementation("androidx.compose.ui:ui:1.4.3")
    // implementation("androidx.compose.material3:material3:1.0.0")
    // implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    // implementation("androidx.activity:activity-compose:1.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Otras dependencias necesarias
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))  // Establecer la versión JVM para Java
    }
}
