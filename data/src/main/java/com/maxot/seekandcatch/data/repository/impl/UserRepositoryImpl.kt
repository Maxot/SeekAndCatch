package com.maxot.seekandcatch.data.repository.impl

import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
@Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataSource: UserDataSource,
) : UserRepository {

    override suspend fun getUser(): User? {
        return userDataSource.getUser(authRepository.getUserId())
    }

}
