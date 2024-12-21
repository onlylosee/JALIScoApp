package com.team.jalisco.activities.marketContent


import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import com.team.jalisco.R
import com.team.jalisco.domain.util.Item
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@OptIn(ExperimentalMotionApi::class)
@Composable
fun ProfileHeader(
    paddingValues: PaddingValues,
    onFilterChanged: (List<Item>) -> Unit // Лямбда для передачи отфильтрованных данных
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<Item>>(emptyList()) }

    val client = supabaseCreate()

    LaunchedEffect(Unit) {
        categories = loadCategories(client)
        items = loadItems(client)
        filteredItems = items
        onFilterChanged(filteredItems) // Передаем изначальные данные
    }

    fun applyFilters() {
        filteredItems = items.filter { item ->
            val matchesCategory = selectedCategory == null || item.categories.contains(selectedCategory!!)
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
        onFilterChanged(filteredItems) // Передаем отфильтрованные данные
    }

    LaunchedEffect(selectedCategory, searchQuery) {
        applyFilters()
    }
    val focus = LocalFocusManager.current
    var color = Color.Black
    val outlinedTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = color,
        unfocusedBorderColor = color,
        focusedLabelColor = color,
        unfocusedLabelColor = color,
        focusedTextColor = color,
        unfocusedTextColor = Color.DarkGray,
        cursorColor = color)
    Column(modifier = Modifier.fillMaxWidth().padding(paddingValues)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            colors = outlinedTextFieldColors,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .onFocusChanged {}
                .layoutId("searchbar"),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "email text",
                    tint = Color.LightGray
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focus.clearFocus()
                }
            ),
            maxLines = 1,
            placeholder = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text("Search...", color = Color.LightGray)
                }
            }
        )


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                Text(
                    text = category,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedCategory == category)  Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF8000FF)
                                )
                            )
                            else Brush.linearGradient(listOf(Color(0xFFEBEBEB), Color(0xFFEBEBEB)))
                        )
                        .clickable {
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (selectedCategory == category) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}


// Функция для загрузки категорий
suspend fun loadCategories(client: SupabaseClient): List<String> {
    val response = client.from("items").select(Columns.list("categories"))
    val jsonArray = JSONArray(response.data as String)
    val categoriesSet = mutableSetOf<String>()

    for (i in 0 until jsonArray.length()) {
        val categoryString = jsonArray.getJSONObject(i).optString("categories", "")
        if (categoryString.isNotEmpty()) {
            categoriesSet.addAll(categoryString.split(",").map { it.trim() })
        }
    }

    return categoriesSet.toList()
}

// Функция для загрузки объектов
suspend fun loadItems(client: SupabaseClient): List<Item> {
    val response = client.from("items").select(Columns.ALL)
    val jsonArray = JSONArray(response.data as String)
    val itemsList = mutableListOf<Item>()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        itemsList.add(
            Item(
                id = obj.getString("id"),
                name = obj.getString("name"),
                description = obj.getString("description"),
                amount = obj.getString("amount"),
                image = obj.getString("image"),
                cost = obj.getString("cost"),
                nickname = obj.getString("nickname"),
                categories = obj.getString("categories").split(",").map { it.trim() }.toString()
            )
        )
    }

    return itemsList
}



