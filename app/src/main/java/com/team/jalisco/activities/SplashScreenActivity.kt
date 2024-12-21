package com.team.jalisco.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.team.jalisco.R
import com.team.jalisco.domain.theme.MyAppTheme
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.isDistantPast
import kotlin.math.cos
import kotlin.math.sin

class SplashScreenActivity : ComponentActivity() {
    private lateinit var supabase: SupabaseClient
    private lateinit var shared: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setDecorFitsSystemWindows(false)

        supabase = supabaseCreate()
        shared = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        setContent {
            MyAppTheme {
                SplashScreen(
                    context = this,
                    shared = shared,
                    supabase = supabase,
                    navigateToNextScreen = { nextScreenIntent ->
                        startActivity(nextScreenIntent)
                        finish()
                    }
                )
            }
        }
    }
}


@Composable
fun SplashScreen(
    context: Context,
    shared: SharedPreferences,
    supabase: SupabaseClient,
    navigateToNextScreen: (Intent) -> Unit
) {
    val sharedStart = remember { shared.getBoolean("firstStartAnApp", true) }
    var progress by remember { mutableFloatStateOf(0.0f) }
    var isLoggedIn: Boolean
    var isEmailVerified: Boolean

    LaunchedEffect(Unit) {
        val currentUser = supabase.auth.currentUserOrNull()
        val isLoggedIn = shared.getBoolean("isLoggedIn", false)
        Log.e("user", currentUser.toString())
        Log.e("logg", isLoggedIn.toString())
        isEmailVerified = currentUser?.emailConfirmedAt != null

        for (i in 0..100) {
            delay(10)
            progress = i / 100f
        }
        val intent = when (!sharedStart) {
            false -> Intent(context, SecondActivity::class.java)
            true -> {
                if (isLoggedIn && isEmailVerified) {
                    Intent(context, MainActivity::class.java)
                } else {
                    Intent(context, LoginActivity::class.java)
                }
            }
        }
        navigateToNextScreen(intent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedLettersFadeAndScale(progress = progress)
        }
    }
}

@Composable
fun AnimatedLettersFadeAndScale(
    progress: Float
) {
    val text = "JALISco"
    val animationStates = remember { text.map { Animatable(0f) } }
    val clickEffect = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val animationJobs = animationStates.mapIndexed { index, animatable ->
            launch {
                delay(index * 200L)
                animatable.animateTo(1f, animationSpec = tween(500))
            }
        }

        animationJobs.forEach { it.join() }
        clickEffect.animateTo(
            0.9f,
            animationSpec = tween(100)
        )
        clickEffect.animateTo(
            1.1f,
            animationSpec = tween(100)
        )
        clickEffect.animateTo(
            1f,
            animationSpec = tween(100)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            text.forEachIndexed { index, char ->
                val animatedFontSize = lerp(
                    start = 48.sp,
                    stop = 64.sp,
                    fraction = animationStates[index].value * progress
                ) * clickEffect.value

                Text(
                    text = char.toString(),
                    fontSize = animatedFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.honey)),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = animationStates[index].value) // Прозрачность
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


