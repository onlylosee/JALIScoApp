package com.team.jalisco.activities.contents

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.R
import com.team.jalisco.domain.CustomBottomButton
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.CustomTextFieldForProduct
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.Item
import com.team.jalisco.domain.util.fetchItemsFromSupabase
import com.team.jalisco.domain.util.loadNickDataFromSupabase
import com.team.jalisco.domain.util.supabaseCreate
import com.team.jalisco.domain.util.uploadProductDataToSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SellerContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit
) {
    var context = LocalContext.current
    var client: SupabaseClient = supabaseCreate()
    var focus = LocalFocusManager.current
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }


    val cropLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val resultUri = result.data?.let { com.yalantis.ucrop.UCrop.getOutput(it) }
                if (resultUri != null) {
                    croppedImageUri = resultUri
                    Log.d("ImageUpdate", "Cropped Image URI: $croppedImageUri")
                } else {
                    Log.e("CropError", "UCrop resultUri is null")
                    Toast.makeText(context, "Failed to retrieve cropped image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startImageCrop(it, context, cropLauncher) }
        }


    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                IconButton(onClick = {
                    focus.clearFocus()
                    onDrawerClick(drawerState.opposite())
                }) {
                    Icon(
                        painter = CustomMenuIcon("menu"),
                        contentDescription = "Menu Icon"
                    )
                }
                Text(
                    text = "Your products",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.flameregular))
                )
            }
        }
    ) {
        var items by remember { mutableStateOf<List<Item>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isTrue by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(key1 = Unit) {
            isLoading = false
            loadNickDataFromSupabase(client) { loadedNickname ->
                nickname = loadedNickname
            }
            val user = client.auth.currentSessionOrNull()?.user
            if (user != null) {
                val userId = user.id
                Log.d("userId", userId)
                items = fetchItemsFromSupabase(user)
                try {
                    val response = client.postgrest["profile"]
                        .select(columns = Columns.list("phone")) {
                            filter { eq("user_id", userId) }
                            limit(1)
                        }

                    val phone = response.data[0].toString() ?: ""
                    isTrue = phone.isNotBlank()
                    Log.d("Phone check", "Phone exists: $isTrue")
                } catch (e: Exception) {
                    Log.e("SupabaseError", "Error checking phone: ${e.localizedMessage}")
                }
            }
        }
        if (isTrue) {
            var isSheetVisible by remember { mutableStateOf(false) }
            val sheetHeight by animateDpAsState(targetValue = if (isSheetVisible) 650.dp else 0.dp)

            Box(modifier = Modifier.fillMaxSize())
            {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isSheetVisible) isSheetVisible = false
                        }
                ) {

                    items?.let { ItemList(it) }

                    if (!isSheetVisible) {
                        CustomBottomButton(
                            height = 75.dp,
                            text = "Sell your product",
                            onClick = { isSheetVisible = !isSheetVisible },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }


                Surface(
                    elevation = 16.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(sheetHeight),
                    shape = RoundedCornerShape(topStart = 64.dp, topEnd = 64.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(sheetHeight)
                            .background(
                                MaterialTheme.colorScheme.background.copy(0.1f),
                                RoundedCornerShape(topStart = 64.dp, topEnd = 64.dp)
                            )
                            .padding(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                            },
                    ) {
                        if (isSheetVisible) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                val painter: Painter = if (croppedImageUri != null) {
                                    rememberAsyncImagePainter(croppedImageUri)
                                } else {
                                    painterResource(id = R.drawable.sell)
                                }
                                Column {
                                    Row {
                                        Surface(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                                modifier = Modifier
                                                    .height(150.dp)
                                                    .width(150.dp)
                                                    .fillMaxSize()
                                                    .border(
                                                        BorderStroke(
                                                            4.dp,
                                                            MaterialTheme.colorScheme.onPrimary
                                                        ),
                                                        shape = RoundedCornerShape(24.dp)
                                                    )
                                                    .clickable {
                                                        launcher.launch("image/*")
                                                    }
                                                    .clip(RoundedCornerShape(24.dp))
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            CustomTextFieldForProduct(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                textValue = name,
                                                onValueChange = { name = it },
                                                labelText = "Product name",
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
                                                isValid = null,
                                                keyboardType = KeyboardType.Text
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            CustomTextFieldForProduct(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                textValue = amount,
                                                onValueChange = { amount = it },
                                                labelText = "Amount",
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
                                                isValid = null,
                                                keyboardType = KeyboardType.Number
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    CustomTextFieldForProduct(
                                        maxlines = 3,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        textValue = description,
                                        onValueChange = { description = it },
                                        labelText = "Description",
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
                                        isValid = null,
                                        keyboardType = KeyboardType.Text
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    CustomTextFieldForProduct(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textValue = categories,
                                        onValueChange = { categories = it },
                                        labelText = "Categories",
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
                                        isValid = null,
                                        keyboardType = KeyboardType.Text
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    CustomTextFieldForProduct(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textValue = cost,
                                        onValueChange = { cost = it },
                                        labelText = "Cost per item in $",
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
                                        isValid = null,
                                        keyboardType = KeyboardType.Number
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        var isSelected by remember { mutableStateOf(false) }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {},
                                            modifier = Modifier
                                                .scale(1.5f),
                                            interactionSource = remember { MutableInteractionSource() },
                                        )
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            text = "Is you're confirmed?",
                                            fontSize = 22.sp,
                                            fontFamily = FontFamily(Font(R.font.flamesans))
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.BottomCenter,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                    ) {
                                        CustomButton(
                                            text = "Try to create a product",
                                            onClick = {
                                                if (description.isNotBlank() && name.isNotBlank() && amount.isNotBlank() && categories.isNotBlank()) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            Log.d(
                                                                "Supabase",
                                                                "API Key: ${client.supabaseKey}"
                                                            )
                                                            uploadProductDataToSupabase(
                                                                context,
                                                                croppedImageUri,
                                                                description,
                                                                isConfirmed,
                                                                amount,
                                                                name,
                                                                nickname,
                                                                categories,
                                                                cost,
                                                                client,
                                                                onSuccess = {
                                                                    name = ""
                                                                    amount = ""
                                                                    description = ""
                                                                    categories = ""
                                                                    cost = ""

                                                                    croppedImageUri = Uri.EMPTY
                                                                    CoroutineScope(Dispatchers.IO).launch {
                                                                        withContext(Dispatchers.Main) {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Data uploaded successfully",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }
                                                                },
                                                                onError = { errorMessage ->
                                                                    CoroutineScope(Dispatchers.IO).launch {
                                                                        withContext(Dispatchers.Main) {
                                                                            Log.e(
                                                                                "Err",
                                                                                errorMessage
                                                                            )
                                                                            Toast.makeText(
                                                                                context,
                                                                                errorMessage,
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    }

                                                                }
                                                            )
                                                        } catch (e: Exception) {
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Upload failed: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    }


                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Please fill all fields and paste image",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }


                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                Text(
                    text = "Please add and confirm\n your phone in profile",
                    fontFamily = FontFamily(Font(R.font.flameregular)),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ItemCard(item: Item, onDelete: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val cardHeight by animateDpAsState(targetValue = if (isExpanded) 160.dp else 100.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                isExpanded = !isExpanded
            },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContentColor = MaterialTheme.colorScheme.onSecondary
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.image.let { imageUrl ->
                    Surface(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(8.dp))
                                .size(80.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Column {
                        cardText(text = "Name: ${item.name}")
                        cardText(text = "Cost per item: ${item.cost}$")
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = "Amount: \n${item.amount}",
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(R.font.flamesans)),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
                Button(
                    modifier = Modifier
                        .size(65.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    onClick = {
                        onDelete(item.id.toString()) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(2f)
                    )
                }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = "Description: ${item.description}",
                    fontFamily = FontFamily(Font(R.font.flamesans)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = "Nickname: ${item.nickname}",
                    fontFamily = FontFamily(Font(R.font.flamesans)),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

suspend fun deleteItemFromSupabase(id: String): PostgrestResult {
    val client = supabaseCreate()
    val table = client.postgrest.from("items")

    return table.delete() {
        filter {
            eq("id", id)
        }
    }

}

@Composable
fun cardText(
    modifier: Modifier? = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    text: String,
) {
    Text(
        modifier = modifier ?: Modifier,
        style = MaterialTheme.typography.bodyLarge,
        text = text,
        textAlign = textAlign,
        fontFamily = FontFamily(Font(R.font.flamesans)),
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSecondary,
    )
}

@Composable
fun ItemList(items: List<Item>) {
    val itemListState = remember { mutableStateOf(items) }

    LazyColumn{
        items(itemListState.value) { item ->
            ItemCard(
                item = item,
                onDelete = { itemId ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            deleteItemFromSupabase(itemId)
                            itemListState.value = itemListState.value.filter { it.id != itemId }
                        } catch (e: Exception) {
                        }
                    }
                }
            )
        }
    }
}
