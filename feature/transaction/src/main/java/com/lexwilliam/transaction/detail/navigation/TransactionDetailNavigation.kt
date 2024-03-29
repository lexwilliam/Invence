package com.lexwilliam.transaction.detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lexwilliam.core.navigation.Screen
import com.lexwilliam.transaction.detail.route.TransactionDetailRoute
import java.util.UUID

fun NavGraphBuilder.transactionDetailNavigation(onBackStack: () -> Unit) {
    composable(
        route = "${Screen.TRANSACTION_DETAIL}?transactionUUID={transactionUUID}",
        arguments =
            listOf(
                navArgument("transactionUUID") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
    ) {
        TransactionDetailRoute(
            onBackStack = onBackStack
        )
    }
}

fun NavController.navigateToTransactionDetail(
    transactionUUID: UUID,
    options: NavOptions? = null
) {
    this.navigate("${Screen.TRANSACTION_DETAIL}?transactionUUID=$transactionUUID", options)
}