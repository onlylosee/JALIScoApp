package com.team.jalisco.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.theme.MyAppTheme

class SecondActivity : AppCompatActivity() {

    private lateinit var shared: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shared = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        setContent {
            MyAppTheme {
                WelcomeScreen(
                    navigateToNextScreen = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish() // Закрытие текущей активности
                    }
                )
            }
        }
    }
}

@SuppressLint("ResourceType", "CommitPrefEdits")
@Composable
fun WelcomeScreen(
    navigateToNextScreen: () -> Unit
) {
    val scale = remember { Animatable(0.0f) } // Для анимации масштаба
    val alpha = remember { Animatable(0.0f) } // Для анимации прозрачности

    // Анимации: увеличение масштаба и увеличение прозрачности
    LaunchedEffect(true) {
        // Масштабируем логотип
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )

        // Плавное появление текста
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            // Текст 1 с анимацией
            Text(
                text = "Welcome!",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.winter)),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = alpha.value),
                modifier = Modifier
                    .padding(top = 45.dp)
                    .animateContentSize()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Текст 2 с анимацией
            Text(
                text = "This is our clothes \nshop app!",
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.fox)),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha.value),
                modifier = Modifier.animateContentSize()
            )

            Spacer(modifier = Modifier.height(40.dp))

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.raw.sneaker), // Используйте ваш ресурс
                contentDescription = "Sneaker Image",
                modifier = Modifier
                    .size(400.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        rotationZ = 194f,
                        rotationX = 180f
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))
            val context = LocalContext.current
            Column(
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                CustomButton(
                    isLoading = false,
                    text = "FORWARD",
                    onClick = {
                        val shared = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        shared.edit().putBoolean("firstStartAnApp", false).apply()
                        navigateToNextScreen() },
                    height = 65.dp,
                    letterSpacing = 0.25f
                )
            }
        }
    }
}
