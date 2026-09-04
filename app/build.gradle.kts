plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

val signingPropsFile = rootProject.file("release-signing.properties")
val signingProps = Properties().apply {
    if (signingPropsFile.exists()) {
        signingPropsFile.inputStream().use { load(it) }
    }
}

val envStoreFilePath = System.getenv("STICKHUB_STORE_FILE") ?: signingProps.getProperty("STORE_FILE")
val envStorePassword = System.getenv("STICKHUB_STORE_PASSWORD") ?: signingProps.getProperty("STORE_PASSWORD")
val envKeyAlias = System.getenv("STICKHUB_KEY_ALIAS") ?: signingProps.getProperty("KEY_ALIAS")
val envKeyPassword = System.getenv("STICKHUB_KEY_PASSWORD") ?: signingProps.getProperty("KEY_PASSWORD")

val hasReleaseSigning = !envStoreFilePath.isNullOrBlank() &&
    !envStorePassword.isNullOrBlank() &&
    !envKeyAlias.isNullOrBlank() &&
    !envKeyPassword.isNullOrBlank()

android {
    namespace = "com.hkm.stickhub"
    compileSdk {
        version = release(37)
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(envStoreFilePath!!)
                storePassword = envStorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = "com.hkm.stickhub"
        minSdk = 24
        targetSdk = 35
        // Version 5.0.2: select fires at long-press timeout, reorder survives select.
        versionCode = 47
        versionName = "5.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.mlkit.subject.segmentation)
    implementation(libs.icons.lucide)
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.16.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
