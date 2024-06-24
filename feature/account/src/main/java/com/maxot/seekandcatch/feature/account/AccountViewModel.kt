package com.maxot.seekandcatch.feature.account

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.repository.AccountRepository
import com.maxot.seekandcatch.data.repository.ColorsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject constructor(
    private val accountRepository: AccountRepository,
    private val colorsRepository: ColorsRepository,
) : ViewModel() {

    val userName = accountRepository.observeUserName()

    val selectedColors = colorsRepository.selectedColors

    fun setUserName(name: String) {
        viewModelScope.launch {
            accountRepository.setUserName(name)
        }
    }

    fun getAvailableColors() = colorsRepository.getAvailableColors()

    fun onSelectedColorsChanged(newColors: Set<Color>) {
        viewModelScope.launch {
            colorsRepository.setSelectedColors(newColors)
        }
    }
}
