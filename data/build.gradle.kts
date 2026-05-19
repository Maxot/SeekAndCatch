plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
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
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(platform(libs.androidx.compose.bom.get()))
            implementation("androidx.compose.ui:ui-graphics")
            implementation(platform(libs.firebase.bom.get()))
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.auth)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

android {
    namespace = "com.maxot.seekandcatch.data"
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
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
