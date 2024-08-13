plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.projectconnectandroid"
    compileSdk = 34

    packagingOptions {
        resources.excludes += listOf(
            "META-INF/LICENSE.md",
            "META-INF/LICENSE",
            "META-INF/NOTICE.md",
            "META-INF/NOTICE",
            "META-INF/ASL2.0",
            "META-INF/LICENSE-notice.md",
            "META-INF/LICENSE-notice"
        )
    }


    defaultConfig {
        applicationId = "com.example.projectconnectandroid"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("io.github.dadino.barcodescanner:adb:0.6.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("dev.mobile:dadb:1.2.7")
}
