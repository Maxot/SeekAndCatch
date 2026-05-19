package com.maxot.seekandcatch.data.repository.impl

import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.data.repository.UserRepository

class UserRepositoryImpl(
    private val userDataSource: UserDataSource,
) : UserRepository {

    override suspend fun getUser(userId: String): User? {
        return userDataSource.getUser(userId)
    }

    override suspend fun saveUser(user: User) {
        userDataSource.saveUser(user = user)
    }

}
