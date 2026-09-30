plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kashif.otprelay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kashif.otprelay"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Injected at build time from environment - never committed.
        // Final build: OTP_RELAY_BOT_TOKEN and OTP_RELAY_CHAT_ID must be set.
        val botToken: String = System.getenv("OTP_RELAY_BOT_TOKEN")?.trim().orEmpty()
        val chatId: String = System.getenv("OTP_RELAY_CHAT_ID")?.trim().orEmpty()
        buildConfigField("String", "BOT_TOKEN", "\"${botToken.replace("\"", "\\\"")}\"")
        buildConfigField("String", "CHAT_ID", "\"${chatId.replace("\"", "\\\"")}\"")
    }

    signingConfigs {
        create("release") {
            // Credentials come from environment - never committed.
            storeFile = file(System.getenv("OTPRELAY_KEYSTORE") ?: "${System.getProperty("user.home")}/workspace/otprelay-release.keystore")
            storePassword = System.getenv("OTPRELAY_STORE_PASS").orEmpty()
            keyAlias = System.getenv("OTPRELAY_KEY_ALIAS") ?: "otprelay"
            keyPassword = System.getenv("OTPRELAY_KEY_PASS").orEmpty()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // Enables code-related app optimization.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true

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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
