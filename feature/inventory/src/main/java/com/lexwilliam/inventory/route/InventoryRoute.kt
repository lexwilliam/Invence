package com.lexwilliam.inventory.route

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexwilliam.core_ui.R
import com.lexwilliam.core_ui.component.ObserveAsEvents
import com.lexwilliam.core_ui.component.button.InvenceFloatingActionButton
import com.lexwilliam.core_ui.component.chip.InvenceChip
import com.lexwilliam.core_ui.component.chip.InvenceChipQuantityIndicator
import com.lexwilliam.core_ui.component.textfield.InvenceSearchTextField
import com.lexwilliam.core_ui.theme.InvenceTheme
import com.lexwilliam.inventory.component.InventoryColumnCard
import com.lexwilliam.inventory.navigation.InventoryNavigationTarget
import com.lexwilliam.product.util.FilterStrategy
import com.lexwilliam.product.util.ProductQueryStrategy

@Composable
fun InventoryRoute(
    viewModel: InventoryViewModel = hiltViewModel(),
    toProductForm: (String?) -> Unit,
    toProductDetail: (String) -> Unit,
    toBarcode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiProducts by viewModel.uiProducts.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    ObserveAsEvents(viewModel.navigation) { target ->
        when (target) {
            is InventoryNavigationTarget.ProductForm -> toProductForm(target.productUUID)
            is InventoryNavigationTarget.ProductDetail -> toProductDetail(target.productUUID)
            is InventoryNavigationTarget.Barcode -> toBarcode()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InvenceSearchTextField(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    value = uiState.query.query,
                    onValueChange = { viewModel.onEvent(InventoryUiEvent.QueryChanged(it)) },
                    placeholder = {
                        Text(
                            text = "Search",
                            style = InvenceTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "search icon",
                            tint = InvenceTheme.colors.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.onEvent(InventoryUiEvent.BarcodeScannerClicked) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.barcode_scanner),
                                contentDescription = "barcode scan icon",
                                tint = InvenceTheme.colors.primary
                            )
                        }
                    },
                    singleLine = true
                )
                Icon(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.account_circle),
                    contentDescription = "account icon",
                    tint = InvenceTheme.colors.primary
                )
            }
        },
        floatingActionButton = {
            InvenceFloatingActionButton(
                onClick = { viewModel.onEvent(InventoryUiEvent.FabClicked) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "plus fab icon"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InventoryFilterChipGroup(
                    modifier = Modifier.padding(start = 16.dp),
                    productQueryStrategy = uiState.query,
                    onFilterClick = { },
                    onSortByClick = { },
                    onCategoryClick = { }
                )
            }
            items(items = uiProducts) { product ->
                InventoryColumnCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    product = product.product,
                    onClick = {
                        viewModel.onEvent(InventoryUiEvent.ProductClicked(product.product))
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun InventoryFilterChipGroup(
    modifier: Modifier = Modifier,
    productQueryStrategy: ProductQueryStrategy,
    onFilterClick: () -> Unit,
    onSortByClick: () -> Unit,
    onCategoryClick: () -> Unit
) {
    Row(
        modifier =
            modifier
                .scrollable(
                    rememberScrollableState { it },
                    orientation = Orientation.Horizontal
                ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter Chip
        InvenceChip(
            onClick = onFilterClick,
            label = { Text(text = "Filter", style = InvenceTheme.typography.labelMedium) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = R.drawable.tune),
                    contentDescription = "filter icon"
                )
            },
            trailingIcon = {
                if (productQueryStrategy.filter.isNotEmpty()) {
                    InvenceChipQuantityIndicator(quantity = productQueryStrategy.filter.size)
                }
            },
            enabled = productQueryStrategy.filter.isNotEmpty()
        )
        // Sort Chip
        InvenceChip(
            onClick = onSortByClick,
            label = { Text(text = "Sort by", style = InvenceTheme.typography.labelMedium) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = R.drawable.sort),
                    contentDescription = "sort icon"
                )
            },
            enabled = productQueryStrategy.sortBy != null
        )
        // Category Chip
        InvenceChip(
            onClick = onCategoryClick,
            label = { Text(text = "Category", style = InvenceTheme.typography.labelMedium) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = R.drawable.category),
                    contentDescription = "category icon"
                )
            },
            enabled =
                productQueryStrategy.filter.any {
                        filter ->
                    filter is FilterStrategy.CategoryFilter
                }
        )
    }
}