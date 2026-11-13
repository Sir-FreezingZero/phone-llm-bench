plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val buildNumber = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "dev.phonelab.bench"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.phonelab.bench"
        minSdk = 29
        targetSdk = 35
        versionCode = buildNumber
        versionName = "build-$buildNumber"
        ndk { abiFilters += listOf("arm64-v8a") }
    }

    signingConfigs {
        create("lab") {
            // Fixed key so each new build installs as an update over the last one.
            storeFile = file("lab.keystore")
            storePassword = "phonelab"
            keyAlias = "lab"
            keyPassword = "phonelab"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("lab")
        }
        debug {
            signingConfig = signingConfigs.getByName("lab")
        }
    }

    // The llama-bench executables ship as lib*.so so Android extracts them
    // to nativeLibraryDir, the one app folder where files may be executed.
    packaging {
        jniLibs { useLegacyPackaging = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
