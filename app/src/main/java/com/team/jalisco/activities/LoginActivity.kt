package com.team.jalisco.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.EmailField
import com.team.jalisco.domain.PasswordField
import com.team.jalisco.domain.theme.MyAppTheme
import com.team.jalisco.domain.util.SupabaseClientSingleton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    // Создаем Supabase клиент
    val supabase = SupabaseClientSingleton.getClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyAppTheme {
                LoginScreen(
                    onRegisterClicked = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onRecoveryClicked = {
                        startActivity(Intent(this, RecoveryActivity::class.java))
                    },
                    onLoginSuccess = {
                        val shared = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        shared.edit().putBoolean("isLoggedIn", true).apply()
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                    supabase = supabase
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onRegisterClicked: () -> Unit,
    onRecoveryClicked: () -> Unit,
    onLoginSuccess: () -> Unit,
    supabase: SupabaseClient
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Title
            androidx.compose.material.Text(
                text = "Welcome!",
                fontSize = 42.sp,
                fontFamily = FontFamily(Font(R.font.winter)),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Enter your data!",
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.fox)),
                color = Color(0xFF7B7B7B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Field
                EmailField(
                    email = email,
                    onEmailChange = { email = it }
                )
                emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                PasswordField(
                    password = password,
                    onPasswordChange = { password = it }
                )
                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Spacer(modifier = Modifier.height(16.dp))

                // Recovery Password
                Text(
                    text = "Recovery password",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { onRecoveryClicked() }
                        .align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomButton(
                    isLoading = isLoading,
                    text = "Login",
                    onClick = {
                        emailError = null
                        passwordError = null

                        if (!email.contains("@")) {
                            emailError = "Incorrect email"
                            return@CustomButton
                        }
                        if (password.length < 8) {
                            passwordError = "Password must be at least 8 characters"
                            return@CustomButton
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val result = supabase.auth.signInWith(Email){
                                    this.email = email
                                    this.password = password
                                }

                                val user = supabase.auth.currentUserOrNull()
                                if (result != null) {
                                    if (user?.emailConfirmedAt != null) {
                                        onLoginSuccess()
                                    } else {
                                        emailError = "Please confirm your email before logging in."
                                        supabase.auth.signOut()
                                    }
                                } else {
                                    emailError = "Authentication failed: Check your email or password."
                                }
                            } catch (e: Exception) {
                                emailError = "Authentication failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "New user? Create account",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { onRegisterClicked() }
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
