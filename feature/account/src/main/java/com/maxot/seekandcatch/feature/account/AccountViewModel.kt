package com.maxot.seekandcatch.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val userName = accountRepository.observeUserName()

    fun setUserName(name: String) {
        viewModelScope.launch {
            accountRepository.setUserName(name)
        }
    }


}
