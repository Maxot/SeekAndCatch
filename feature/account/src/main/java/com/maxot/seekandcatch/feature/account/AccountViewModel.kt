package com.maxot.seekandcatch.feature.account

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.base.BaseViewModel
import com.maxot.seekandcatch.core.domain.AuthUseCase
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.ColorsRepository
import com.maxot.seekandcatch.feature.account.ui.model.AccountScreenEvent
import com.maxot.seekandcatch.feature.account.ui.model.AccountScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject constructor(
    private val userUseCase: UserUseCase,
    private val colorsRepository: ColorsRepository,
    private val authUseCase: AuthUseCase,
) : BaseViewModel<AccountScreenUIState, AccountScreenEvent>(AccountScreenUIState()) {

    init {
        loadUserState()
    }

    override fun onEvent(event: AccountScreenEvent) {
        when (event) {
            is AccountScreenEvent.ChangeName -> setUserName(event.name)
            is AccountScreenEvent.ChangeSelectedColors -> onSelectedColorsChanged(event.colors)
            AccountScreenEvent.DeleteAccount -> deleteAccount()
        }
    }

    private fun loadUserState() {
        viewModelScope.launch {
            val user = userUseCase.getUser()
            val selectedColors = colorsRepository.selectedColors.first()
            updateState {
                it.copy(
                    user = user,
                    selectedColors = selectedColors,
                    availableColors = colorsRepository.getAvailableColors()
                )
            }
        }
    }

    private fun onSelectedColorsChanged(newColors: Set<Color>) {
        viewModelScope.launch {
            colorsRepository.setSelectedColors(newColors)
        }
    }

    private fun setUserName(name: String) {
        viewModelScope.launch {
            userUseCase.setName(name)
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            try {
                authUseCase.deleteAccountAndData()
            } catch (_: Exception) {
                // deletion failed — session stays active, re-register below
            }
            authUseCase.autoRegisterIfNeeded()
            loadUserState()
        }
    }

}
