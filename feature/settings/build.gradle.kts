plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:model"))
            implementation(project(":singleselectionlazyrow"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(project(":core:media"))
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.androidx.hilt.navigation.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.hilt.android)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "com.maxot.seekandcatch.feature.settings"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    add("kspAndroid", libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
