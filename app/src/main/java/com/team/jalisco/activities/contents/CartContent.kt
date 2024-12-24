package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.ProductCartItem
import com.team.jalisco.domain.util.SupabaseClientSingleton
import com.team.jalisco.domain.util.loadCartAndProducts
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CartContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val cartItems = remember { mutableStateListOf<ProductCartItem>() }
    var totalCost by remember { mutableStateOf(0f) }
    val client = SupabaseClientSingleton.getClient()
    var userId = client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("BAD!")
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    LaunchedEffect(Unit) {
        val loadedCartItems = loadCartAndProducts(client, userId)
        cartItems.addAll(loadedCartItems)
        totalCost = cartItems.fold(0f) { acc, item ->
            val cost = item.cost.toFloatOrNull() ?: 0f
            val amount = item.amount.toFloatOrNull() ?: 0f
            acc + (cost * amount)
        }
    }
    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                cardText(
                    text = "Total cost = ${totalCost} $",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                CustomButton(
                    text = "Pay",
                    onClick = {
                        val donationUrl = "https://www.donationalerts.com/r/onlylose?amount=${"%.2f".format(totalCost)}"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(donationUrl))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
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
                        text = "Your cart",
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // Распределяет оставшееся место между списком и кнопкой
                        .fillMaxWidth()
                ) {
                    items(cartItems.toList()) { cartItem ->
                        CartItemView(cartItem = cartItem)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Total: $${"%.2f".format(totalCost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp)
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CustomButton(
                        text = "Pay for cart",
                        onClick = {
                            coroutineScope.launch {
                                if (bottomSheetState.isVisible) {
                                    bottomSheetState.hide()
                                } else {
                                    bottomSheetState.show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CartItemView(
    cartItem: ProductCartItem,
) {
    val painter = rememberAsyncImagePainter(cartItem.image)
    var quantity by remember { mutableStateOf(cartItem.amount.toInt()) }

    androidx.compose.material.Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 18.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 8.dp)
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .size(80.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                cardText(text = cartItem.name)
                Spacer(modifier = Modifier.height(8.dp))
                cardText(text = "Price: $${"%.2f".format(cartItem.cost.toDoubleOrNull() ?: 0.0)}")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    cardText(text = "Amount: ${cartItem.amount.toInt()}")
                }
            }
        }
    }
}
