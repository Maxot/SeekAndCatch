package com.maxot.seekandcatch.feature.settings.ui

import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.core.model.UserConfig
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.SCLocaleManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var localeManager: SCLocaleManager
    private lateinit var audioManager: AudioManager
    private lateinit var vibrationManager: VibrationManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mock()
        localeManager = mock()
        audioManager = mock()
        vibrationManager = mock()

        whenever(settingsRepository.userConfig).thenReturn(
            flowOf(UserConfig(DarkThemeConfig.DARK, false))
        )
        whenever(settingsRepository.observeSoundState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeMusicState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeVibrationState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeColorblindModeEnabled()).thenReturn(flowOf(false))
        whenever(localeManager.getLocales()).thenReturn(listOf("en-US", "uk"))
        whenever(localeManager.getSelectedLocale()).thenReturn(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SettingsViewModel(settingsRepository, localeManager, audioManager, vibrationManager)

    @Test
    fun `initial darkTheme state reflects repository userConfig`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.darkTheme.value)
    }

    @Test
    fun `setSoundState saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.setSoundState(false)
        advanceUntilIdle()

        verify(settingsRepository).setSoundState(false)
    }

    @Test
    fun `setMusicState saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.setMusicState(false)
        advanceUntilIdle()

        verify(settingsRepository).setMusicState(false)
    }

    @Test
    fun `setVibrationState saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.setVibrationState(true)
        advanceUntilIdle()

        verify(settingsRepository).setVibrationState(true)
    }

    @Test
    fun `setDarkTheme saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.setDarkTheme(false)
        advanceUntilIdle()

        verify(settingsRepository).setDarkTheme(false)
    }

    @Test
    fun `light theme config maps to false`() = runTest {
        whenever(settingsRepository.userConfig).thenReturn(
            flowOf(UserConfig(DarkThemeConfig.LIGHT, false))
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.darkTheme.value)
    }
}
