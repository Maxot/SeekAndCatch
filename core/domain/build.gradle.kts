plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksp)
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
            implementation(project(":data"))
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.hilt.android)
        }
    }
}

android {
    namespace = "com.maxot.seekandcatch.core.domain"
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

    testImplementation(project(":data-test"))
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
