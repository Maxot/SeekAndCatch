package com.maxot.seekandcatch.core.domain.user

import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.UserRepository
import javax.inject.Inject

class UserUseCase
@Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) {

    suspend fun setName(name: String) {
        val user = userRepository.getUser(authRepository.getUserId())?.copy(name = name)
        user?.let { userRepository.saveUser(user) }
    }

    suspend fun getUser(): User? {
        return userRepository.getUser(authRepository.getUserId())
    }
}
