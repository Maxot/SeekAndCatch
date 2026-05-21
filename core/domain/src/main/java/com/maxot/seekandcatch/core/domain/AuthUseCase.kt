package com.maxot.seekandcatch.core.domain

import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.UserRepository
import javax.inject.Inject

class AuthUseCase
@Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    suspend fun autoRegisterIfNeeded() {
        val user = authRepository.getOrCreateUser()
        user?.id?.let { uid ->
            val existingUser = userRepository.getUser(uid)
            if (existingUser == null) {
                val user = User(id = uid)
                userRepository.saveUser(user)
            }
        }
    }

    suspend fun signOut() {
        authRepository.signOut()
    }

    suspend fun deleteAccountAndData() {
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            userRepository.deleteUser(userId)
        }
        authRepository.deleteAccount()
    }

}
