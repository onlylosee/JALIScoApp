package com.team.jalisco.domain.presentation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.team.jalisco.R
import com.team.jalisco.domain.model.NavigationItem
import com.team.jalisco.domain.util.UserProfile
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.Column


@Composable
fun CustomDrawer(
    selectedNavigationItem: NavigationItem,
    onNavigationItemClick: (NavigationItem) -> Unit,
    onCloseClick: () -> Unit,
) {
    var supabaseImageUrl by remember { mutableStateOf<String?>(null) }
    var supabase = supabaseCreate()
    LaunchedEffect(Unit) {
        loadDataFromSupabase(supabase) {loadedImageUrl ->
            supabaseImageUrl = loadedImageUrl
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.6f)
            .fillMaxHeight()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back Arrow Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        val painter: Painter =
            if (supabaseImageUrl != null) {
                rememberAsyncImagePainter(supabaseImageUrl)
            } else {
                painterResource(id = R.drawable.profile)
            }
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier
                .size(125.dp)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp)),
            painter = painter,
            contentDescription = "Zodiac Image"
        )
        Spacer(modifier = Modifier.height(40.dp))
        NavigationItem.entries.toTypedArray().take(5).forEach { navigationItem ->
            NavigationItemView(
                navigationItem = navigationItem,
                selected = navigationItem == selectedNavigationItem,
                onClick = { onNavigationItemClick(navigationItem) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

suspend fun loadDataFromSupabase(
    client: SupabaseClient = supabaseCreate(),
    onDataLoaded: (String?) -> Unit
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
                val imageUrl = userProfile.image

                onDataLoaded(imageUrl)
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