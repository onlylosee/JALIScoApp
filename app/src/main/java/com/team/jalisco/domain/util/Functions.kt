package com.team.jalisco.domain.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONArray
import java.io.InputStream

@Serializable
data class UserProfile(
    val user_id: String,
    val nickname: String?,
    val name: String?,
    val bio: String?,
    val address: String?,
    val phone: String?,
    val image: String,
)

@Serializable
data class ProductItem(
    val user_id: String,
    val nickname: String,
    val name: String,
    val amount: String,
    val categories: String,
    val description: String,
    val isConfirmed: Boolean,
    val cost: String,
    val image: String,
)

@Serializable
data class Item(
    val name: String,
    val amount: String,
    val image: String,
    val cost: String,
    val categories: String,
    val description: String,
    val nickname: String,
    val id: String
)


@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = composed {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparent = color.copy(alpha = 0f).toArgb()
    this.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparent
            frameworkPaint.setShadowLayer(
                shadowRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor
            )
            it.drawRoundRect(
                0f,
                0f,
                this.size.width,
                this.size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
        }
    }
}


suspend fun fetchItemsFromSupabase(user: UserInfo?): List<Item> {
    return withContext(Dispatchers.IO) {
        val client = supabaseCreate()
        val userId = user?.id ?: throw Exception("asd")
        val itemsResponse = client.postgrest.from("items").select(Columns.ALL) {
            filter {
                eq("user_id", userId)
            }
        }



        val jsonString = itemsResponse.data ?: "[]"
        Log.e("RawJsonString", jsonString)

        val jsonArray = JSONArray(jsonString)
        val itemList = mutableListOf<Item>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val item = Item(
                amount = jsonObject.optString("amount", "0"),
                name = jsonObject.optString("name", "Unknown"),
                cost = jsonObject.optString("cost", "0"),
                image = jsonObject.optString("image", ""),
                description = jsonObject.optString("description", ""),
                nickname = jsonObject.optString("nickname", ""),
                categories = jsonObject.optString("categories"),
                id = jsonObject.optString("id", "")
            )
            itemList.add(item)
        }

        Log.e("ParsedItems", itemList.toString())
        return@withContext itemList
    }
}


fun supabaseCreate(): SupabaseClient {
    return createSupabaseClient(
        supabaseUrl = "https://miysqzrlswurvlayaicr.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1peXNxenJsc3d1cnZsYXlhaWNyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQzMTAwNzEsImV4cCI6MjA0OTg4NjA3MX0.kppDYE0f1Bcdzm19bKJbbz7H31AbKjVvewcvSev76W8"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}

suspend fun uploadDataToSupabase(
    context: Context,
    imageUri: Uri?,
    bio: String,
    name: String,
    nickname: String,
    supabase: SupabaseClient,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val user = supabase.auth.currentSessionOrNull()?.user
        if (user != null) {
            val userId = user.id

            if (imageUri != null) {
                uploadImageToSupabaseStorage(context, imageUri, userId, supabase, { imageUrl ->
                    val userData = mapOf(
                        "user_id" to userId,
                        "bio" to bio,
                        "name" to name,
                        "nickname" to nickname,
                        "image" to imageUrl.toString()
                    )

                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            supabase.postgrest["profile"].upsert(userData)
                            onSuccess()
                        }
                    } catch (e: Exception) {
                        onError("Ошибка при добавлении данных: ${e.message}")
                    }
                }, { errorMessage ->
                    onError("Ошибка при загрузке изображения: $errorMessage")
                })
            }
        } else {
            onError("Нет авторизованного пользователя.")
        }
    } catch (e: Exception) {
        onError("Ошибка загрузки данных: ${e}")
    }
}

suspend fun uploadProductDataToSupabase(
    context: Context,
    imageUri: Uri?,
    description: String,
    isConfirmed: Boolean,
    amount: String,
    name: String,
    nickname: String,
    categories: String,
    cost: String,
    supabase: SupabaseClient,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val user = supabase.auth.currentSessionOrNull()?.user
        if (user != null) {
            val userId = user.id
            Log.e("0", "err")
            if (imageUri != null) {
                Log.e("1", "err")
                uploadProductImageToSupabaseStorage(
                    context,
                    imageUri,
                    userId,
                    supabase,
                    { imageUrl ->
                        val userData = ProductItem(
                            userId,
                            nickname,
                            name,
                            amount,
                            categories,
                            description,
                            isConfirmed,
                            cost,
                            imageUrl
                        )

                        try {
                            Log.e("2", "err")
                            CoroutineScope(Dispatchers.IO).launch {
                                supabase.postgrest["items"].insert(userData)
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            onError("Ошибка при добавлении данных: ${e.message}")
                        }
                    },
                    { errorMessage ->
                        onError("Ошибка при загрузке изображения: $errorMessage")
                    })
            }
        } else {
            onError("Нет авторизованного пользователя.")
        }
    } catch (e: Exception) {
        onError("Ошибка загрузки данных: ${e}")
    }
}

suspend fun loadDataFromSupabase(
    client: SupabaseClient = supabaseCreate(),
    onDataLoaded: (String, String, String, String, String, String) -> Unit
) {
    try {
        val userId = client.auth.currentSessionOrNull()?.user?.id
        if (userId != null) {
            val postgrestResponse = client.postgrest["profile"]
                .select(Columns.ALL) {
                    filter { eq("user_id", userId) }
                }
            val userProfiles: List<UserProfile> = postgrestResponse.decodeList()

            val userProfile = userProfiles.firstOrNull()

            if (userProfile != null) {
                val nickname = userProfile.nickname ?: ""
                val name = userProfile.name ?: ""
                val bio = userProfile.bio ?: ""
                val imageUrl = userProfile.image
                val address = userProfile.address ?: ""
                val phone = userProfile.phone ?: ""

                onDataLoaded(nickname, name, bio, address, phone, imageUrl)
            } else {
                Log.e("SupabaseError", "No profile found for user_id $userId")
            }
        } else {
            Log.e("AuthError", "No authenticated user found.")
        }
    } catch (e: Exception) {
        Log.e("SupabaseError", "Error loading data: ${e.message}")
    }
}

suspend fun loadNickDataFromSupabase(
    client: SupabaseClient = supabaseCreate(),
    onDataLoaded: (String) -> Unit
) {
    try {
        Log.e("10", "err")
        val userId = client.auth.currentSessionOrNull()?.user?.id
        if (userId != null) {
            Log.e("11", "err")
            val postgrestResponse = client.postgrest["profile"]
                .select(Columns.ALL) {
                    filter { eq("user_id", userId) }
                }
            Log.e("12", "err")
            val userProfiles: List<UserProfile> = postgrestResponse.decodeList()

            val userProfile = userProfiles.firstOrNull()

            Log.e("13", "err")
            if (userProfile != null) {
                val nickname = userProfile.nickname ?: ""

                Log.e("14", "err")
                onDataLoaded(nickname)
            } else {
                Log.e("SupabaseError", "No profile found for user_id $userId")
            }
        } else {
            Log.e("AuthError", "No authenticated user found.")
        }
    } catch (e: Exception) {
        Log.e("SupabaseError", "Error loading data: ${e.message}")
    }
}

suspend fun uploadImageToSupabaseStorage(
    context: Context,
    imageUri: Uri,
    userId: String,
    supabase: SupabaseClient,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

        if (inputStream == null) {
            onError("Не удалось открыть поток для изображения")
            return
        }

        val byteArray = inputStream.readBytes()
        try {
            supabase.storage.from("avatars").delete("$userId/avatar.jpg")
        } finally {
            supabase.storage.from("avatars").upload("$userId/avatar.jpg", byteArray)
        }

        val publicUrl: String = supabase.storage.from("avatars").publicUrl("$userId/avatar.jpg")

        onSuccess(publicUrl)
    } catch (e: Exception) {

        onError("Ошибка загрузки: ${e.localizedMessage}")
    }
}

suspend fun uploadProductImageToSupabaseStorage(
    context: Context,
    imageUri: Uri,
    userId: String,
    supabase: SupabaseClient,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        Log.e("3", "err")

        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

        if (inputStream == null) {
            onError("Не удалось открыть поток для изображения")
            return
        }
        Log.e("4", "err")
        val systemCurrTime = System.currentTimeMillis()
        val byteArray = inputStream.readBytes()
        supabase.storage.from("itemImages").upload("$userId/${systemCurrTime}.jpg", byteArray)

        Log.e("5", "err")
        val publicUrl: String =
            supabase.storage.from("itemImages").publicUrl("$userId/${systemCurrTime}.jpg")
        Log.e("6", "err")

        onSuccess(publicUrl)
    } catch (e: Exception) {
        onError("Ошибка загрузки: ${e.localizedMessage}")
    }
}


suspend fun uploadStringToSupabase(
    string: String?,
    tableString: String?,
    client: SupabaseClient = supabaseCreate(),
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val user = client.auth.currentSessionOrNull()?.user
        if (user != null) {
            Handler(Looper.getMainLooper()).post {
                CoroutineScope(Dispatchers.IO).launch {
                    val userId = user.id
                    val userData = mapOf(
                        "user_id" to userId, // Ensure user_id is used as the primary key
                        "$tableString" to string
                    )
                    try {
                        client.postgrest["profile"].upsert(userData)
                        onSuccess()
                    } catch (e: Exception) {
                        e.message?.let { Log.e("err", it) }
                    }
                }
            }

        } else {
            onError("No authenticated user found.")
        }
    } catch (e: Exception) {
        onError("Failed to upload data: ${e.localizedMessage}")
    }
}
