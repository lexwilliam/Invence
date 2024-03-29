package com.lexwilliam.user.util

sealed interface LoginFailure

sealed interface LogoutFailure

sealed interface FetchUserFailure {
    data object UserIsNull : FetchUserFailure

    data object DocumentNotFound : FetchUserFailure
}

sealed interface UpsertUserFailure

data class UnknownFailure(
    val message: String? = null
) : LoginFailure, LogoutFailure, UpsertUserFailure, FetchUserFailure