package com.maxot.seekandcatch.feature.account

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.domain.AuthUseCase
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.ColorsRepository
import com.maxot.seekandcatch.feature.account.ui.model.AccountScreenEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authUseCase: AuthUseCase
    private lateinit var userUseCase: UserUseCase
    private lateinit var colorsRepository: ColorsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authUseCase = mock()
        userUseCase = mock()
        colorsRepository = mock()
        whenever(colorsRepository.selectedColors).thenReturn(flowOf(emptySet()))
        whenever(colorsRepository.getAvailableColors()).thenReturn(emptySet())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AccountViewModel(userUseCase, colorsRepository, authUseCase)

    @Test
    fun `delete account calls authUseCase deleteAccountAndData and autoRegisterIfNeeded`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(AccountScreenEvent.DeleteAccount)
        advanceUntilIdle()
        verify(authUseCase).deleteAccountAndData()
        verify(authUseCase).autoRegisterIfNeeded()
    }

    @Test
    fun `delete account does not crash when deleteAccountAndData throws`() = runTest {
        whenever(authUseCase.deleteAccountAndData()).thenThrow(RuntimeException("network error"))
        val viewModel = createViewModel()
        viewModel.onEvent(AccountScreenEvent.DeleteAccount)
        advanceUntilIdle()
        // no exception propagated — test passes if we reach here
    }
}
