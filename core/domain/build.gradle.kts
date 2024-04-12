plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.maxot.seekandcatch.core.domain"
    compileSdk = 34
    defaultConfig {
        minSdk = 30
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
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.material3)
    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)

    testImplementation(project(":data-test"))
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)

}
