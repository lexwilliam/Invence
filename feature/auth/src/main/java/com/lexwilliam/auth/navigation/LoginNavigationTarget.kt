package com.lexwilliam.auth.navigation

sealed interface LoginNavigationTarget {
    data object Inventory : LoginNavigationTarget
}