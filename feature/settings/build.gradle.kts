plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.maxot.seekandcatch.feature.settings"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":core:designsystem"))
    implementation(project(":singleselectionlazyrow"))

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.appcompat)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)

    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
}