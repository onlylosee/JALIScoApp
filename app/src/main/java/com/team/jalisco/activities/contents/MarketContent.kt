package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.team.jalisco.ProductsCard
import com.team.jalisco.activities.marketContent.ProfileHeader
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.isOpened
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.Item
import com.team.jalisco.domain.util.fetchItemsFromSupabase

@Composable
fun MarketContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    val myHomeFeedScrollState = rememberLazyGridState()
    val toolbarProgress = remember { mutableStateOf(0f) }
    val focus = LocalFocusManager.current
    val nestedScrollConnection = object : NestedScrollConnection {

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (myHomeFeedScrollState.firstVisibleItemIndex == 0) {
                toolbarProgress.value =
                    (myHomeFeedScrollState.firstVisibleItemScrollOffset / 100f).coerceIn(0f, 1f)
            } else {
                toolbarProgress.value = 1f
            }
            return Offset.Zero
        }
    }

    var filteredItems by remember { mutableStateOf<List<Item>>(emptyList()) } // Отфильтрованные элементы

    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            IconButton(onClick = {
                focus.clearFocus()
                onDrawerClick(drawerState.opposite())
            }) {
                Icon(
                    painter = CustomMenuIcon("menu"),
                    contentDescription = "Menu Icon"
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Color.White)) {
            Column(
                Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .background(Color.White)
            ) {
                ProductsGrid(lazyListState = myHomeFeedScrollState, products = filteredItems)
            }
            Column {
                // Передаем лямбду для обновления `filteredItems`
                ProfileHeader(padding, onFilterChanged = { newFilteredItems ->
                    filteredItems = newFilteredItems
                })
            }
        }
    }
}
@Composable
fun ProductsGrid(lazyListState: LazyGridState, products: List<Item>) { // Assuming Product is a data class

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(top = 130.dp, bottom = 16.dp),
        state = lazyListState
    ) {

        items(products) { product ->

            Card(
                elevation = 4.dp,
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {

                ProductsCard(
                    modifier = Modifier.fillMaxSize(),
                    name = product.name,
                    cost = product.cost,
                    image = product.image
                )
            }

        }
    }
}
