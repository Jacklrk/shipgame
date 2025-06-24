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
    // Otras dependencias necesarias
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))  // Establecer la versión JVM para Java
    }
}
