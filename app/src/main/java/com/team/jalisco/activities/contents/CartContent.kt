package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.R
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.ProductCartItem
import com.team.jalisco.domain.util.SupabaseClientSingleton
import com.team.jalisco.domain.util.loadCartAndProducts
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.Serializable

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CartContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
){
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val cartItems = remember { mutableStateListOf<ProductCartItem>() }
    var totalCost by remember { mutableStateOf(0f) }
    val client = SupabaseClientSingleton.getClient()
    var userId = client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("BAD!")
    LaunchedEffect(Unit) {
        var loadedCartItems = loadCartAndProducts(client, userId)
        cartItems.addAll(loadedCartItems)
        totalCost = cartItems.sumOf { it.cost.toInt() * it.amount.toInt() }.toFloat()
    }



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
    ){ paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cartItems.toList()) { cartItem -> // Преобразуем cartItems в обычный список
                    CartItemView(
                        cartItem = cartItem,
                        onQuantityChange = { newAmount ->
                            cartItem.amount = newAmount.toString()
                            totalCost = cartItems.sumOf { it.cost.toInt() * it.amount.toInt() }.toFloat()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total: $${"%.2f".format(totalCost)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun CartItemView(
    cartItem: ProductCartItem,
    onQuantityChange: (Int) -> Unit
) {
    val painter = rememberAsyncImagePainter(cartItem.image)
    var quantity by remember { mutableStateOf(cartItem.amount.toString()) }

    androidx.compose.material.Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Image(
                painter = painter,
                contentDescription = cartItem.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name and cost
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.name, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Price: $${"%.2f".format(cartItem.cost)}", style = MaterialTheme.typography.bodyMedium)
            }

            // Quantity and buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newQuantity = (quantity.toIntOrNull() ?: 0) - 1
                        if (newQuantity >= 0) {
                            quantity = newQuantity.toString()
                            onQuantityChange(newQuantity)
                        }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Decrease quantity")
                    }

                    TextField(
                        value = quantity,
                        onValueChange = { newQuantity ->
                            val newIntValue = newQuantity.toIntOrNull() ?: 0
                            if (newIntValue >= 0) {
                                quantity = newQuantity
                                onQuantityChange(newIntValue)
                            }
                        },
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.width(50.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    IconButton(onClick = {
                        val newQuantity = (quantity.toIntOrNull() ?: 0) + 1
                        quantity = newQuantity.toString()
                        onQuantityChange(newQuantity)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                    }
                }
            }
        }
    }
}