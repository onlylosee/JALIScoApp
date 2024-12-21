package com.team.jalisco.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    private val supabase: SupabaseClient = supabaseCreate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyAppTheme {
                RegisterScreen(
                    onLoginClicked = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    },
                    onRegisterSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                    supabase = supabase,
                    context = LocalContext.current
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onLoginClicked: () -> Unit,
    onRegisterSuccess: () -> Unit,
    supabase: SupabaseClient,
    context: Context
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Welcome!",
                fontSize = 42.sp,
                fontFamily = FontFamily(Font(R.font.winter)),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Here you can register \nyour JALISco account!",
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
                EmailField(
                    email = email,
                    onEmailChange = { email = it }
                )
                emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    password = password,
                    onPasswordChange = { password = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    password = confirmPassword,
                    onPasswordChange = { confirmPassword = it },
                    mainPass = false,
                    labelPass = "Confirm Password"
                )

                if (password != confirmPassword) {
                    confirmPasswordError = "Passwords do not match"
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(45.dp))

                CustomButton(
                    isLoading = isLoading,
                    text = "Register",
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
                        if (password != confirmPassword) {
                            confirmPasswordError = "Passwords do not match"
                            return@CustomButton
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val result = supabase.auth.signUpWith(Email){
                                    this.email = email
                                    this.password = password
                                }

                                if (result != null) {
                                    Toast.makeText(
                                        context,
                                        "Registration successful. Check your email for verification!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onLoginClicked()
                                } else {
                                    emailError = "Registration failed: User not created."
                                }
                            } catch (e: Exception) {
                                emailError = "Registration failed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Already registered? Log In!",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable { onLoginClicked() }
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
