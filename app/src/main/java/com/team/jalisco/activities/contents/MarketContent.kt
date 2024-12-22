package com.team.jalisco.activities.contents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.ProductsCard
import com.team.jalisco.R
import com.team.jalisco.activities.marketContent.ProfileHeader
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.CustomTextFieldForProduct
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.Item
import com.team.jalisco.domain.util.SupabaseClientSingleton
import com.team.jalisco.domain.util.addProductToCart
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MarketContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    val myHomeFeedScrollState = rememberLazyGridState()
    val toolbarProgress = remember { mutableStateOf(0f) }
    val focus = LocalFocusManager.current
    val client = SupabaseClientSingleton.getClient()
    val cartItems = remember { mutableStateListOf<Map<String, String>>() }
    var thisContext = LocalContext.current
    var amountOfProduct by remember { mutableStateOf("") }
    var totallyCost by remember { mutableStateOf(0f) } // Общее значение стоимости

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

    var filteredItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<Item?>(null) }

    ModalBottomSheetLayout(
        modifier = Modifier
            .wrapContentHeight()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { },
        sheetState = sheetState,
        sheetContent = {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            ) {
                selectedProduct?.let { product ->
                    val painter = rememberAsyncImagePainter(product.image)
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(18.dp)
                    ) {
                        Row {
                            Surface(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(150.dp)
                                        .width(150.dp)
                                        .border(
                                            BorderStroke(
                                                4.dp,
                                                MaterialTheme.colorScheme.onPrimary
                                            ),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(
                                Modifier
                                    .height(150.dp)
                                    .padding(top = 12.dp, bottom = 12.dp)
                            ) {
                                cardText(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = "Item: \n${product.name}"
                                )
                                Spacer(Modifier.weight(1f))
                                cardText(
                                    modifier = Modifier.fillMaxWidth(),
                                    TextAlign.Center,
                                    text = "Seller: \n${product.nickname}"
                                )
                            }
                        }

                        Row(
                            Modifier.padding(top = 24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                cardText(
                                    Modifier,
                                    TextAlign.Center,
                                    "Amount of product: \n${product.amount}"
                                )
                                Spacer(Modifier.height(12.dp))
                                CustomTextFieldForProduct(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    textValue = amountOfProduct,
                                    onValueChange = {
                                        amountOfProduct = it
                                        val amount = it.toIntOrNull() ?: 0
                                        totallyCost = amount * (product.cost.toFloatOrNull() ?: 0f)
                                    },
                                    labelText = "Max = ${product.amount}",
                                    enabledOrNot = true,
                                    outlinedTextFieldColors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                        unfocusedTextColor = Color.DarkGray,
                                        focusedTextColor = Color.Black,
                                        focusedIndicatorColor = Color.Gray,
                                        unfocusedIndicatorColor = Color.Gray,
                                        unfocusedLabelColor = Color.Black,
                                        focusedLabelColor = Color.DarkGray,
                                    ),
                                    isValid = true,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Column(
                                Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                cardText(
                                    Modifier.fillMaxWidth(),
                                    TextAlign.Center,
                                    "Cost for one item = \n${product.cost.toFloatOrNull()} $"
                                )
                                Spacer(Modifier.height(12.dp))
                                cardText(
                                    Modifier.fillMaxWidth(),
                                    TextAlign.Center,
                                    "Total cost = \n$totallyCost $"
                                )
                            }
                        }
                        CustomButton(
                            modifier = Modifier.padding(top = 32.dp),
                            text = "Add to cart",
                            onClick = {
                                if (amountOfProduct.toInt() <= product.amount.toInt() && amountOfProduct.toInt() >= 1) {
                                    coroutineScope.launch {
                                        val userId = client.auth.currentUserOrNull()?.id
                                        if (userId == null) {
                                            println("User is not authenticated")
                                            return@launch
                                        }

                                        addProductToCart(
                                            supabaseClient = client,
                                            userId = userId,
                                            productId = product.id,
                                            amount = amountOfProduct,
                                            product.amount,
                                            context = thisContext
                                        )
                                    }
                                } else {
                                    println("Invalid amount. Please enter a valid quantity.")
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Market",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.flameregular))
                    )
                }
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .background(Color.White)
                ) {
                    ProductsGrid(
                        lazyListState = myHomeFeedScrollState,
                        products = filteredItems,
                        onProductClick = { product ->
                            println("Product clicked: $product")
                            selectedProduct = product
                            coroutineScope.launch {
                                println("Showing sheet")
                                sheetState.show()
                            }
                        }
                    )
                }
                Column {
                    ProfileHeader(padding, onFilterChanged = { newFilteredItems ->
                        filteredItems = newFilteredItems
                    })
                }
            }
        }
    }
}

@Composable
fun ProductsGrid(
    lazyListState: LazyGridState,
    products: List<Item>,
    onProductClick: (Item) -> Unit
) {
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
                    image = product.image,
                    onClick = {
                        onProductClick(product)
                        println("Products count: ${products.size}")
                    },
                )
            }
        }
    }
}