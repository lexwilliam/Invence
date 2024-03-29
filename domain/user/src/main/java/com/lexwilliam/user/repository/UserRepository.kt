package com.lexwilliam.user.repository

import arrow.core.Either
import com.lexwilliam.user.model.User
import com.lexwilliam.user.util.FetchUserFailure
import com.lexwilliam.user.util.UpsertUserFailure
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(uuid: String): Flow<User?>

    suspend fun fetchUser(uuid: String): Either<FetchUserFailure, User>

    suspend fun upsertUser(user: User): Either<UpsertUserFailure, User>
}