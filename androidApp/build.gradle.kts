import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.mattschoe.apptemplate"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mattschoe.apptemplate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        // release-please owns the version; CI passes it in via the release tag.
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
    }

    // Used for automating builds in CI. The keystore is decoded from a base64
    // secret by .github/workflows/release-artifact-upload.yml.
    val keystoreFile = file("keystore.jks")
    val hasKeystore = keystoreFile.exists()

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            // Only wired up when a keystore is actually present, so a plain local
            // `./gradlew build` produces an unsigned release APK instead of failing.
            if (hasKeystore) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Ship every locale in one artifact rather than splitting per-language.
    @Suppress("UnstableApiUsage")
    bundle {
        language {
            enableSplit = false
        }
    }
}
