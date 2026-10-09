import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val signingPropertiesFile = rootProject.file("keystore.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.sipun.superiorwalls"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sipun.superiorwalls"
        minSdk = 29
        targetSdk = 37
        versionCode = 7
        versionName = "1.5"
    }

    signingConfigs {
        create("release") {
            val keystorePath = signingProperties.getProperty("storeFile")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEYSTORE_FILE")
            val keystorePassword = signingProperties.getProperty("storePassword")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val keyAliasValue = signingProperties.getProperty("keyAlias")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEY_ALIAS")
            val keyPasswordValue = signingProperties.getProperty("keyPassword")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEY_PASSWORD")

            if (!keystorePath.isNullOrBlank()) storeFile = rootProject.file(keystorePath)
            if (!keystorePassword.isNullOrBlank()) storePassword = keystorePassword
            if (!keyAliasValue.isNullOrBlank()) keyAlias = keyAliasValue
            if (!keyPasswordValue.isNullOrBlank()) keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.palette)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
