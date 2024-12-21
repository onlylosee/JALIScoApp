import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.EmailField


@Composable
fun RecoveryScreen(
    onRecoverClick: (String) -> Unit,
    showPopup: Boolean,
    onDismissPopup: () -> Unit
) {
    val foxFont = FontFamily(Font(R.font.fox))
    val winterFont = FontFamily(Font(R.font.winter))
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var focus: FocusManager = LocalFocusManager.current
    rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Title
            Text(
                text = "Forgot password",
                fontSize = 42.sp,
                fontFamily = winterFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Subtitle
            Text(
                text = "Enter your email account to \nreset your password!",
                fontSize = 20.sp,
                fontFamily = foxFont,
                color = Color(0xFF7B7B7B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Input & Button container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Input Field
                EmailField(
                    email,
                    onEmailChange = { email = it }
                )

                Spacer(modifier = Modifier.height(45.dp))

                // Reset Password Button
                CustomButton(
                    isLoading = isLoading,
                    text = "Reset password",
                    onClick = {
                        onRecoverClick(email)
                        isLoading = true
                        focus.clearFocus()
                    }
                )
            }
        }
    }

    // Popup
    if (showPopup) {
        BlurredPopupWithBackground(onDismissPopup)
    }
}

@Composable
fun BlurredPopupWithBackground(
    onDismiss: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .blur(26.dp)
                .clickable { onDismiss() }
        )

        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(30.dp)
                .widthIn(max = 300.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Иконка в попапе
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(255, 29, 0), shape = RoundedCornerShape(12.dp))
                        .padding(5.dp)
                ) {
                    Icon(
                        painter = CustomMenuIcon("email"),
                        contentDescription = "Mail icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Текст 1 в попапе
                Text(
                    text = "Check your Email",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Default,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Текст 2 в попапе
                Text(
                    text = "We have sent password recovery \ncode in your Email",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

