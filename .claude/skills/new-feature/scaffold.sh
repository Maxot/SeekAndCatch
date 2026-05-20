#!/bin/bash
# Scaffolds a new feature module's standard file set.
# Usage: scaffold.sh <feature-name>
# Example: scaffold.sh statistics
# Produces files matching the conventions in docs/TECH_SPEC.md and the existing codebase.

set -euo pipefail

REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
BASE_PACKAGE="com.maxot.seekandcatch"

if [[ $# -lt 1 ]]; then
  echo "Usage: scaffold.sh <feature-name>"
  echo "  Example: scaffold.sh statistics"
  exit 1
fi

FEATURE_RAW="$1"
# Convert to lowercase for directory/package
FEATURE_LOWER=$(echo "$FEATURE_RAW" | tr '[:upper:]' '[:lower:]')
# Capitalise first letter for class names
FEATURE_CAP="$(echo "${FEATURE_LOWER:0:1}" | tr '[:lower:]' '[:upper:]')${FEATURE_LOWER:1}"

FEATURE_DIR="$REPO_ROOT/feature/$FEATURE_LOWER"
PACKAGE="${BASE_PACKAGE}.feature.${FEATURE_LOWER}"
SRC_DIR="$FEATURE_DIR/src/main/java/$(echo "$PACKAGE" | tr '.' '/')"

echo "Scaffolding feature: $FEATURE_CAP"
echo "  Package:    $PACKAGE"
echo "  Directory:  $FEATURE_DIR"
echo ""

# ── Create directory structure ─────────────────────────────────
mkdir -p "$SRC_DIR/navigation"
mkdir -p "$SRC_DIR/ui"
mkdir -p "$SRC_DIR/di"
mkdir -p "$FEATURE_DIR/src/test/java/$(echo "$PACKAGE" | tr '.' '/')"
mkdir -p "$FEATURE_DIR/src/androidTest/java/$(echo "$PACKAGE" | tr '.' '/')"

# ── UiState ───────────────────────────────────────────────────
cat > "$SRC_DIR/${FEATURE_CAP}UiState.kt" << EOF
package $PACKAGE

data class ${FEATURE_CAP}UiState(
    val isLoading: Boolean = false,
)
EOF

# ── Event ─────────────────────────────────────────────────────
cat > "$SRC_DIR/${FEATURE_CAP}Event.kt" << EOF
package $PACKAGE

sealed class ${FEATURE_CAP}Event {
    // Add events here
}
EOF

# ── ViewModel ─────────────────────────────────────────────────
cat > "$SRC_DIR/${FEATURE_CAP}ViewModel.kt" << EOF
package $PACKAGE

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ${FEATURE_CAP}ViewModel @Inject constructor(
    // Inject repositories and use cases here
) : ViewModel() {

    private val _uiState = MutableStateFlow(${FEATURE_CAP}UiState())
    val uiState: StateFlow<${FEATURE_CAP}UiState> get() = _uiState

    fun onEvent(event: ${FEATURE_CAP}Event) {
        when (event) {
            // Handle events
        }
    }
}
EOF

# ── Screen ────────────────────────────────────────────────────
cat > "$SRC_DIR/ui/${FEATURE_CAP}Screen.kt" << EOF
package $PACKAGE.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import ${PACKAGE}.${FEATURE_CAP}Event
import ${PACKAGE}.${FEATURE_CAP}UiState
import ${PACKAGE}.${FEATURE_CAP}ViewModel

@Composable
fun ${FEATURE_CAP}Screen(
    viewModel: ${FEATURE_CAP}ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ${FEATURE_CAP}ScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ${FEATURE_CAP}ScreenContent(
    modifier: Modifier = Modifier,
    uiState: ${FEATURE_CAP}UiState,
    onEvent: (${FEATURE_CAP}Event) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "${FEATURE_CAP}")
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
private fun ${FEATURE_CAP}ScreenPreviewLight() {
    SeekAndCatchTheme(darkTheme = false) {
        ${FEATURE_CAP}ScreenContent(
            uiState = ${FEATURE_CAP}UiState(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ${FEATURE_CAP}ScreenPreviewDark() {
    SeekAndCatchTheme(darkTheme = true) {
        ${FEATURE_CAP}ScreenContent(
            uiState = ${FEATURE_CAP}UiState(),
            onEvent = {}
        )
    }
}
EOF

# ── Navigation ────────────────────────────────────────────────
ROUTE_CONST="${FEATURE_LOWER^^}_ROUTE"
cat > "$SRC_DIR/navigation/${FEATURE_CAP}Navigation.kt" << EOF
package $PACKAGE.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import ${PACKAGE}.ui.${FEATURE_CAP}Screen

const val ${ROUTE_CONST} = "${FEATURE_LOWER}"

fun NavController.navigateTo${FEATURE_CAP}(navOptions: NavOptions? = null) {
    navigate(${ROUTE_CONST}, navOptions)
}

fun NavGraphBuilder.${FEATURE_LOWER}Screen() {
    composable(route = ${ROUTE_CONST}) {
        ${FEATURE_CAP}Screen()
    }
}
EOF

# ── Repository interface ───────────────────────────────────────
cat > "$SRC_DIR/${FEATURE_CAP}Repository.kt" << EOF
package $PACKAGE

interface ${FEATURE_CAP}Repository {
    // Define suspend functions and Flow-returning functions here
}
EOF

# ── Repository implementation ─────────────────────────────────
REPO_SRC="$REPO_ROOT/data/src/main/java/$(echo "${BASE_PACKAGE}.data.repository" | tr '.' '/')"
mkdir -p "$REPO_SRC"
cat > "$REPO_SRC/${FEATURE_CAP}RepositoryImpl.kt" << EOF
package ${BASE_PACKAGE}.data.repository

import ${PACKAGE}.${FEATURE_CAP}Repository
import javax.inject.Inject

class ${FEATURE_CAP}RepositoryImpl @Inject constructor(
    // Inject FirebaseFirestore or DataStore here as needed
) : ${FEATURE_CAP}Repository {
    // Implement methods
}
EOF

# ── Hilt module ───────────────────────────────────────────────
cat > "$SRC_DIR/di/${FEATURE_CAP}Module.kt" << EOF
package $PACKAGE.di

import ${PACKAGE}.${FEATURE_CAP}Repository
import ${BASE_PACKAGE}.data.repository.${FEATURE_CAP}RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ${FEATURE_CAP}Module {

    @Binds
    fun bind${FEATURE_CAP}Repository(impl: ${FEATURE_CAP}RepositoryImpl): ${FEATURE_CAP}Repository
}
EOF

# ── build.gradle.kts ──────────────────────────────────────────
cat > "$FEATURE_DIR/build.gradle.kts" << EOF
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "$PACKAGE"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":data"))

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
EOF

echo "Scaffolded files:"
echo "  $SRC_DIR/${FEATURE_CAP}UiState.kt"
echo "  $SRC_DIR/${FEATURE_CAP}Event.kt"
echo "  $SRC_DIR/${FEATURE_CAP}ViewModel.kt"
echo "  $SRC_DIR/${FEATURE_CAP}Repository.kt"
echo "  $SRC_DIR/ui/${FEATURE_CAP}Screen.kt"
echo "  $SRC_DIR/navigation/${FEATURE_CAP}Navigation.kt"
echo "  $SRC_DIR/di/${FEATURE_CAP}Module.kt"
echo "  $REPO_SRC/${FEATURE_CAP}RepositoryImpl.kt"
echo "  $FEATURE_DIR/build.gradle.kts"
echo ""
echo "Next steps:"
echo "  1. Add ':feature:${FEATURE_LOWER}' to settings.gradle.kts"
echo "  2. Add implementation(project(':feature:${FEATURE_LOWER}')) to :app/build.gradle.kts"
echo "  3. Wire ${FEATURE_LOWER}Screen() into SeekCatchNavHost"
echo "  4. Bind ${FEATURE_CAP}RepositoryImpl in DataModule or ${FEATURE_CAP}Module"

exit 0
