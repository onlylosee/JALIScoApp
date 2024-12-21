package com.team.jalisco.domain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.jalisco.R


@Composable
fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    focus: FocusManager = LocalFocusManager.current
) {
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$".toRegex()
    val isValidEmail = emailPattern.matches(email)

    val outlinedTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (email.isEmpty()) Color.Black else if (isValidEmail) Color.Green else Color.Red,
        unfocusedBorderColor = if (email.isEmpty()) Color.Black else if (isValidEmail) Color.Green else Color.Red,
        focusedLabelColor = if (email.isEmpty()) Color.Black else if (isValidEmail) Color.Green else Color.Red,
        unfocusedLabelColor = if (email.isEmpty()) Color.Black else if (isValidEmail) Color.Green else Color.Red,
        cursorColor = if (email.isEmpty()) Color.Black else if (isValidEmail) Color.Green else Color.Red,
    )

    OutlinedTextField(
        maxLines = 1,
        value = email,
        onValueChange = onEmailChange,
        label = { Text("E-mail") },
        isError = !isValidEmail && email.isNotEmpty(),
        colors = outlinedTextFieldColors,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focus.clearFocus()
            }
        ),
        textStyle = TextStyle(
            color = if (isValidEmail) Color.Black else Color.Gray,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.flamesans))
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = CustomMenuIcon("email"),
                contentDescription = "email text"
            )
        },
    )
}

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    mainPass: Boolean = true,
    willBeMatch: Boolean = true,
    labelPass: String = "Password",
    focus: FocusManager = LocalFocusManager.current
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isValidPassword = password.length >= 8
    val outlinedTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (password.isEmpty()) Color.Black else if (willBeMatch && isValidPassword) Color.Green else Color.Red,
        unfocusedBorderColor = if (password.isEmpty()) Color.Black else if (willBeMatch && isValidPassword) Color.Green else Color.Red,
        focusedLabelColor = if (password.isEmpty()) Color.Black else if (willBeMatch && isValidPassword) Color.Green else Color.Red,
        unfocusedLabelColor = if (password.isEmpty()) Color.Black else if (willBeMatch && isValidPassword) Color.Green else Color.Red,
        cursorColor = if (password.isEmpty()) Color.Black else if (willBeMatch && isValidPassword) Color.Green else Color.Red,
    )

    OutlinedTextField(
        maxLines = 1,
        shape = RoundedCornerShape(20.dp),
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(labelPass) },
        leadingIcon = {
            Icon(
                painter = CustomMenuIcon("lock"),
                contentDescription = "Password Icon"
            )
        },
        trailingIcon = {
            if (mainPass) IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    painter = if (isPasswordVisible) CustomMenuIcon("vTrue") else CustomMenuIcon("vFalse"),
                    contentDescription = "Toggle Password Visibility"
                )
            }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focus.clearFocus()
            }
        ),
        isError = !isValidPassword && password.isNotEmpty(),
        colors = outlinedTextFieldColors,
        textStyle = TextStyle(
            color = if (willBeMatch && isValidPassword) Color.Black else Color.Gray,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.flamesans))
        ),
        modifier = Modifier.fillMaxWidth(),


        )

    if (mainPass && !isValidPassword && password.isNotEmpty()) {
        Text(
            text = "Password must be at least 8 characters",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CustomTextField(
    textValue: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    onIconClick: (String) -> Unit,
    painterForIcon: Painter?,
    enabledOrNot: Boolean? = true,
    focus: FocusManager = LocalFocusManager.current,
    outlinedTextFieldColors: TextFieldColors,
    icon: String,
    isValid: Boolean?
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        maxLines = 1,
        shape = RoundedCornerShape(20.dp),
        value = textValue,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        leadingIcon = {
            Icon(
                painter = CustomMenuIcon(icon),
                contentDescription = "Icon"
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { onIconClick(icon) }
            ) {
                if (painterForIcon != null) {
                    Icon(
                        painter = painterForIcon,
                        contentDescription = null
                    )
                }
            }
        },
        enabled = enabledOrNot ?: true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = when (icon) {
                "phone" -> KeyboardType.Phone
                "map" -> KeyboardType.Text
                else -> KeyboardType.Text
            }
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focus.clearFocus()
            }
        ),
        colors = outlinedTextFieldColors,
        textStyle = TextStyle(
            color = if (isValid == null || isValid == true) Color.Black else Color.Gray,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.flamesans))
        ),
    )
}

@Composable
fun CustomTextFieldForProduct(
    modifier: Modifier,
    textValue: String,
    maxlines: Int = 1,
    onValueChange: (String) -> Unit,
    labelText: String,
    enabledOrNot: Boolean? = true,
    focus: FocusManager = LocalFocusManager.current,
    outlinedTextFieldColors: TextFieldColors,
    isValid: Boolean?,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        modifier = modifier,
        maxLines = maxlines,
        shape = RoundedCornerShape(20.dp),
        value = textValue,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        enabled = enabledOrNot ?: true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focus.clearFocus()
            }
        ),
        colors = outlinedTextFieldColors,
        textStyle = TextStyle(
            color = if (isValid == null || isValid == true) Color.Black else Color.Gray,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.flamesans))
        ),
    )
}


@Composable
fun CustomBottomButton(
    isLoading: Boolean = false,
    text: String,
    onClick: () -> Unit,
    height: Dp = 50.dp,
    letterSpacing: Float = 0.1f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFF8000FF)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.fox)),
                    letterSpacing = TextUnit(letterSpacing, TextUnitType.Em)
                )
            }
        }
    }
}


@Composable
fun CustomButton(
    isLoading: Boolean = false,
    text: String,
    onClick: () -> Unit,
    height: Dp = 50.dp,
    letterSpacing: Float = 0.1f,
    modifier: Modifier = (Modifier
        .fillMaxWidth()
        .height(height)
        .background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.primary,
                    Color(0xFF8000FF)
                )
            ),
            shape = RoundedCornerShape(12.dp)
        ))
) {
    Box(
        modifier = modifier
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize(), // Используйте fillMaxSize, чтобы кнопка занимала весь размер Box
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent // Убедитесь, что цвет контейнера прозрачный
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.fox)),
                    letterSpacing = TextUnit(letterSpacing, TextUnitType.Em)
                )
            }
        }
    }
}


@Composable
fun CustomMenuIcon(
    icon: String?
): Painter {
    return when (icon) {
        "menu" -> painterResource(id = R.drawable.menu)
        "cart" -> painterResource(id = R.drawable.sell)
        "profile" -> painterResource(id = R.drawable.profile)
        "home" -> painterResource(id = R.drawable.home)
        "logout" -> painterResource(id = R.drawable.logout)
        "settings" -> painterResource(id = R.drawable.settings)
        "lock" -> painterResource(id = R.drawable.round_lock_24)
        "vFalse" -> painterResource(id = R.drawable.baseline_visibility_off_24)
        "vTrue" -> painterResource(id = R.drawable.baseline_visibility_24)
        "email" -> painterResource(id = R.drawable.baseline_email_24)
        "phone" -> painterResource(id = R.drawable.phone)
        "map" -> painterResource(id = R.drawable.map)
        else -> painterResource(id = R.drawable.menu)
    }
}
