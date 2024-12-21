package com.team.jalisco.activities

import RecoveryScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.team.jalisco.domain.util.supabaseCreate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RecoveryActivity : AppCompatActivity() {
    private lateinit var supabase: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabase = supabaseCreate()

        setContent {
            var showPopup by remember { mutableStateOf(false) }

            RecoveryScreen(
                onRecoverClick = { email ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val emailExists = checkEmailInDatabase(email)
                        runOnUiThread {
                            if (emailExists) {
                                showPopup = true
                                Toast.makeText(this@RecoveryActivity, "Recovery email sent!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@RecoveryActivity, "Email does not exist!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                showPopup = showPopup,
                onDismissPopup = { showPopup = false }
            )
        }
    }

    private suspend fun checkEmailInDatabase(email: String): Boolean {
        return try {
            if (!isValidEmail(email)) return false

            supabase.auth.resetPasswordForEmail(email)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
