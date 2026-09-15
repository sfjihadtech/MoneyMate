// MoneyMate configuration file. Sections are grouped and commented to explain build/runtime responsibilities.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.moneymate.app"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.moneymate.app"

        minSdk = 24
        targetSdk = 37

        versionCode = 6
        versionName = "2.5.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    // ========================================================
    // Release signing configuration
    // ========================================================
    signingConfigs {
        create("release") {
            storeFile = file("../moneymate-release-key.jks")

            storePassword =
                providers.gradleProperty("MONEYMATE_KEYSTORE_PASSWORD").orNull

            keyAlias = "moneymate"

            keyPassword =
                providers.gradleProperty("MONEYMATE_KEY_PASSWORD").orNull
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://10.0.2.2:5000/\""
            )
        }

        release {
            // Do not fail during Android Studio sync / project configuration.
            // A real production URL is still required before packaging a release.
            val productionApi =
                providers.gradleProperty("MONEYMATE_API_BASE_URL")
                    .orElse(
                        providers.environmentVariable(
                            "MONEYMATE_API_BASE_URL"
                        )
                    )
                    .orElse("https://invalid.moneymate.local/")
                    .get()

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"$productionApi\""
            )

            // Sign release APK/AAB using the MoneyMate release keystore.
            signingConfig =
                signingConfigs.getByName("release")

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true

        // Generate BuildConfig class
        buildConfig = true
    }
}


// Production safety guard:
// Android Studio sync and debug builds can run without a production URL,
// but actual release packaging is blocked until MONEYMATE_API_BASE_URL is set.
val productionApiConfigured =
    providers.gradleProperty("MONEYMATE_API_BASE_URL")
        .orElse(
            providers.environmentVariable(
                "MONEYMATE_API_BASE_URL"
            )
        )

tasks.configureEach {
    if (
        name == "assembleRelease" ||
        name == "bundleRelease" ||
        name == "packageRelease"
    ) {
        doFirst {
            if (!productionApiConfigured.isPresent) {
                throw GradleException(
                    "MONEYMATE_API_BASE_URL must be configured before creating a release build. " +
                            "Example: ./gradlew bundleRelease -PMONEYMATE_API_BASE_URL=https://api.yourdomain.com/"
                )
            }
        }
    }
}

dependencies {

    // Google Play Billing: product prices/entitlements come from Play Console.
    implementation("com.android.billingclient:billing-ktx:9.1.0")


    // ========================================================
    // Navigation
    // ========================================================
    implementation(
        "androidx.navigation:navigation-compose:2.10.0"
    )


    // ========================================================
    // Jetpack Compose
    // ========================================================
    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )


    // ========================================================
    // Android Core
    // ========================================================
    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )


    // ========================================================
    // Retrofit
    // ========================================================
    implementation(
        "com.squareup.retrofit2:retrofit:3.0.0"
    )


    // ========================================================
    // Gson Converter
    // ========================================================
    implementation(
        "com.squareup.retrofit2:converter-gson:3.0.0"
    )


    // ========================================================
    // OkHttp
    // ========================================================
    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )


    // ========================================================
    // HTTP Logging
    // ========================================================
    implementation(
        "com.squareup.okhttp3:logging-interceptor:4.12.0"
    )


    // Java time backport for minSdk 24
    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.5"
    )

    // Lifecycle owner integration used for automatic app lock
    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.11.0"
    )

    // Profile photo loading
    implementation("io.coil-kt:coil-compose:2.7.0")


    // ========================================================
    // Unit Tests
    // ========================================================
    testImplementation(
        libs.junit
    )


    // ========================================================
    // Android Tests
    // ========================================================
    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )


    // ========================================================
    // Debug
    // ========================================================
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}