package com.team.jalisco.uicomponents

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team.jalisco.activities.LoginActivity
import com.team.jalisco.activities.contents.MarketContent
import com.team.jalisco.activities.contents.ProfileContent
import com.team.jalisco.activities.contents.SellerContent
import com.team.jalisco.activities.contents.SettingsContent
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.NavigationItem
import com.team.jalisco.domain.model.isOpened
import com.team.jalisco.domain.presentation.CustomDrawer
import com.team.jalisco.domain.util.coloredShadow
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Preview
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<NavigationItem>(NavigationItem.Home) }
    val context = LocalContext.current

    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    var selectedNavigationItem by remember { mutableStateOf(NavigationItem.Home) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * density).roundToInt() }
    }
    val offsetValue by remember { derivedStateOf { (screenWidth.value / 5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
        label = "Animated offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.85f else 1f,
        label = "Animated Scale"
    )
    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CustomDrawerState.Closed
    }

    Box(
        modifier = Modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFF8000FF)
                    )
                )
            )
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()

    ) {
        CustomDrawer(
            selectedNavigationItem = selectedNavigationItem,
            onNavigationItemClick = {
                selectedNavigationItem = it
                when (selectedNavigationItem) {
                    NavigationItem.Logout -> {
                        val shared = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        shared.edit().clear().apply()
                        shared.edit().putBoolean("firstStartAnApp", false).apply()
                        CoroutineScope(Dispatchers.IO).launch {
                            val supabase = supabaseCreate()
                            supabase.auth.signOut()
                            Log.e("authOrNot", supabase.auth.currentUserOrNull().toString())
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    }

                    else -> currentScreen = selectedNavigationItem
                }
            },
            onCloseClick = { drawerState = CustomDrawerState.Closed }
        )
        val cornerRadius by animateDpAsState(
            targetValue = if (drawerState.isOpened()) 24.dp else 0.dp,
            animationSpec = tween(durationMillis = 300)
        )
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = Color.Black,
                    alpha = 0.1f,
                    shadowRadius = 50.dp
                )
                .clip(RoundedCornerShape(cornerRadius))
                .fillMaxSize()
                .clickable {
                    drawerState = CustomDrawerState.Closed
                },
        ) {
            when (currentScreen) {
                NavigationItem.Home -> MarketContent(
                    modifier = Modifier,
                    drawerState = drawerState,
                    onDrawerClick = { drawerState = it }
                )

                NavigationItem.Profile -> ProfileContent(
                    modifier = Modifier,
                    drawerState = drawerState,
                    onDrawerClick = { drawerState = it },
                    supabase = supabaseCreate(),
                    onClick = { TODO() }
                )

                NavigationItem.Settings -> SettingsContent(
                    modifier = Modifier,
                    drawerState = drawerState,
                    onDrawerClick = { drawerState = it }
                )

                NavigationItem.Seller -> SellerContent(
                    modifier = Modifier,
                    drawerState = drawerState,
                    onDrawerClick = { drawerState = it }
                )

                else -> MarketContent(
                    modifier = Modifier,
                    drawerState = drawerState,
                    onDrawerClick = { drawerState = it }
                )
            }
        }
    }
}









