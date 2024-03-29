package com.lexwilliam.order.checkout.route

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexwilliam.core.extensions.toCurrency
import com.lexwilliam.core_ui.component.ObserveAsEvents
import com.lexwilliam.core_ui.component.button.InvencePrimaryButton
import com.lexwilliam.core_ui.component.button.InvenceSecondaryButton
import com.lexwilliam.core_ui.component.topbar.InvenceTopBar
import com.lexwilliam.core_ui.theme.InvenceTheme
import com.lexwilliam.order.checkout.dialog.PaymentListDialog
import com.lexwilliam.order.checkout.navigation.CheckOutNavigationTarget
import com.lexwilliam.order.order.component.SmallOrderProductCard
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutRoute(
    viewModel: CheckOutViewModel = hiltViewModel(),
    onBackStack: () -> Unit,
    toCart: () -> Unit,
    toTransactionDetail: (UUID) -> Unit
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPayMethod.collectAsStateWithLifecycle()
    val isPaymentShown by viewModel.isPaymentShown.collectAsStateWithLifecycle()

    val total = orders.sumOf { order -> order.quantity * order.item.price }

    ObserveAsEvents(flow = viewModel.navigation) { target ->
        when (target) {
            CheckOutNavigationTarget.BackStack -> onBackStack()
            CheckOutNavigationTarget.Cart -> toCart()
            is CheckOutNavigationTarget.TransactionDetail -> toTransactionDetail(target.uuid)
        }
    }

    BackHandler {
        viewModel.onEvent(CheckOutUiEvent.BackStackClicked)
    }

    if (isPaymentShown) {
        PaymentListDialog(
            paymentMethod = paymentMethod,
            subtotal = total,
            onMethodClicked = { viewModel.onEvent(CheckOutUiEvent.PaymentSelected(it)) },
            onDismiss = { viewModel.onEvent(CheckOutUiEvent.Dismiss) }
        )
    }

    Scaffold(
        topBar = {
            InvenceTopBar(
                title = {
                    Text(
                        text = "Check Out",
                        style = InvenceTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(CheckOutUiEvent.BackStackClicked) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "nav back stack icon")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(InvenceTheme.colors.neutral30)
                            .padding(horizontal = 16.dp)
                            .clickable { viewModel.onEvent(CheckOutUiEvent.PaymentMethodClicked) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedPaymentMethod == null) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "no payment method selected icon"
                        )
                        Text(
                            text = "Select payment method",
                            style = InvenceTheme.typography.labelLarge
                        )
                    } else {
                        Text(
                            text = selectedPaymentMethod?.name.toString(),
                            style = InvenceTheme.typography.labelLarge
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = InvenceTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = total.toCurrency(), style = InvenceTheme.typography.titleLarge)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InvenceSecondaryButton(
                        modifier =
                            Modifier.wrapContentWidth(),
                        onClick = { viewModel.onEvent(CheckOutUiEvent.SaveForLaterClicked) }
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "save cart icon"
                        )
                    }
                    InvencePrimaryButton(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        onClick = { viewModel.onEvent(CheckOutUiEvent.ConfirmClicked) }
                    ) {
                        Text(
                            text = "Confirm",
                            style = InvenceTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = orders) { order ->
                SmallOrderProductCard(
                    modifier = Modifier,
                    imagePath = order.item.imagePath,
                    imageModifier = Modifier.size(50.dp),
                    name = order.item.name,
                    price = order.item.price,
                    quantity = order.quantity,
                    onQuantityChanged = {
                        viewModel.onEvent(CheckOutUiEvent.QuantityChanged(order.item.uuid, it))
                    }
                )
            }
            item {
                Column {
                    Text(
                        text = "Total Order: ${orders.size}",
                        style = InvenceTheme.typography.labelMedium
                    )
                    Text(
                        text = "Total Quantity: ${orders.sumOf { it.quantity }}",
                        style = InvenceTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}