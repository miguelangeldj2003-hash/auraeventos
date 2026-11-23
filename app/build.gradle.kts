import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val keystorePropertiesFile = rootProject.file("keystore.properties").takeIf { it.exists() }
    ?: file("keystore.properties").takeIf { it.exists() }
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile != null && keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    namespace = "com.aura.eventos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aura.eventos"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.2.0"
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
    }

    signingConfigs {
        create("release") {
            val alias = keystoreProperties.getProperty("keyAlias")
            val keyPass = keystoreProperties.getProperty("keyPassword")
            val storePath = keystoreProperties.getProperty("storeFile")
            val storePass = keystoreProperties.getProperty("storePassword")

            if (alias.isNullOrBlank() || keyPass.isNullOrBlank() || storePath.isNullOrBlank() || storePass.isNullOrBlank()) {
                throw GradleException("Error: No se encontró el archivo 'keystore.properties' o faltan propiedades (keyAlias, keyPassword, storeFile, storePassword). Copia 'keystore.properties.example' a 'keystore.properties' y configura tus credenciales de release.")
            }

            keyAlias = alias
            keyPassword = keyPass
            storeFile = rootProject.file(storePath)
            storePassword = storePass
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    // Room + WorkManager
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    // Red
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Cámara, GPS, imágenes
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Pruebas unitarias
    testImplementation("junit:junit:4.13.2")
}
