package com.team.jalisco.activities.contents


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.team.jalisco.R
import com.team.jalisco.domain.CustomButton
import com.team.jalisco.domain.CustomMenuIcon
import com.team.jalisco.domain.CustomTextField
import com.team.jalisco.domain.model.CustomDrawerState
import com.team.jalisco.domain.model.opposite
import com.team.jalisco.domain.util.loadDataFromSupabase
import com.team.jalisco.domain.util.supabaseCreate
import com.team.jalisco.domain.util.uploadDataToSupabase
import com.team.jalisco.domain.util.uploadStringToSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.InputStream




@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    drawerState: CustomDrawerState,
    onDrawerClick: (CustomDrawerState) -> Unit,
    supabase: SupabaseClient = supabaseCreate(),
    onClick: () -> Unit
) {
    var imageSize by remember { mutableStateOf(100.dp) }
    var nickname by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var mobilePhone by remember { mutableStateOf("") }

    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var supabaseImageUrl by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val focus = LocalFocusManager.current

    val cropLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val resultUri = result.data?.let { com.yalantis.ucrop.UCrop.getOutput(it) }
                if (resultUri != null) {
                    croppedImageUri = resultUri
                    Log.d("ImageUpdate", "Cropped Image URI: $croppedImageUri")
                } else {
                    Log.e("CropError", "UCrop resultUri is null")
                    Toast.makeText(context, "Failed to retrieve cropped image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startImageCrop(it, context, cropLauncher) }
        }

    LaunchedEffect(Unit) {
        loadDataFromSupabase(supabase) { loadedNickname, loadedName, loadedBio, loadedAddress, loadedPhone, loadedImageUrl ->
            nickname = loadedNickname
            name = loadedName
            bio = loadedBio
            supabaseImageUrl = loadedImageUrl
            address = loadedAddress
            mobilePhone = loadedPhone
        }
    }

    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CustomDrawerState.Opened) {
                onDrawerClick(CustomDrawerState.Closed)
            },
        topBar = {
            IconButton(onClick = {
                focus.clearFocus()
                onDrawerClick(drawerState.opposite())
            }) {
                Icon(
                    painter = CustomMenuIcon("menu"),
                    contentDescription = "Menu Icon"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF8000FF)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(12.dp)
                        .height(imageSize + 78.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val painter: Painter = if (croppedImageUri != null) {
                        rememberAsyncImagePainter(croppedImageUri)
                    } else if (supabaseImageUrl != null) {
                        rememberAsyncImagePainter(supabaseImageUrl)
                    } else {
                        painterResource(id = R.drawable.profile)
                    }

                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(imageSize + 100.dp)
                            .width(imageSize + 100.dp)
                            .weight(1f)
                            .fillMaxSize()
                            .border(
                                BorderStroke(4.dp, MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                launcher.launch("image/*")
                            }
                            .clip(RoundedCornerShape(24.dp))
                    )


                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {


                        TextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Nickname") },
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.onPrimary),
                                    RoundedCornerShape(12.dp)
                                )
                                .height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = Color.DarkGray,
                                focusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedLabelColor = Color.Black,
                                focusedLabelColor = Color.DarkGray
                            ),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.flamesans))
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focus.clearFocus()
                                }
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            maxLines = 1,
                            label = { Text("Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.onPrimary),
                                    RoundedCornerShape(12.dp)
                                )
                                .height(55.dp),

                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = Color.DarkGray,
                                focusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedLabelColor = Color.Black,
                                focusedLabelColor = Color.DarkGray,
                            ),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.flamesans))
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focus.clearFocus()
                                }
                            ),
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("BIO") },
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(3.dp, MaterialTheme.colorScheme.onPrimary),
                                    RoundedCornerShape(12.dp)
                                )
                                .height(55.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = Color.DarkGray,
                                focusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                unfocusedLabelColor = Color.Black,
                                focusedLabelColor = Color.DarkGray
                            ),
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.flamesans))
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focus.clearFocus()
                                }
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                text = "Upload to Database",
                onClick = {
                    if (bio.isNotBlank() && name.isNotBlank() && nickname.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                Log.d("Supabase", "API Key: ${supabase.supabaseKey}")
                                uploadDataToSupabase(
                                    context,
                                    croppedImageUri,
                                    bio,
                                    name,
                                    nickname,
                                    supabase,
                                    onSuccess = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Data uploaded successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    onError = { errorMessage ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Err", errorMessage)
                                                Toast.makeText(
                                                    context,
                                                    errorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    }
                                )
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Upload failed: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                var isTFEnabled by remember { mutableStateOf(false) }
                var isValid by remember { mutableStateOf(true) }

                var isValidPhone by remember { mutableStateOf(false) }
                val phoneRegex = "^(\\+?[1-9][0-9]{1,14})\$".toRegex()
                var isOTPSent by remember { mutableStateOf(false) }

                isValidPhone = mobilePhone.matches(phoneRegex) && mobilePhone.length > 10
                val outlinedTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
                    focusedBorderColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    unfocusedBorderColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    disabledBorderColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    disabledLabelColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    focusedLabelColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    unfocusedLabelColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                    cursorColor = if (mobilePhone.isEmpty()) Color.Black else if (isValidPhone) Color.Green else Color.Red,
                )
                val colorsAddress: TextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
                    focusedBorderColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    unfocusedBorderColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    disabledBorderColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    disabledLabelColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    focusedLabelColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    unfocusedLabelColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                    cursorColor = if (address.isEmpty()) Color.Black else if (isValid) Color.Green else Color.Red,
                )

                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF8000FF)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .wrapContentHeight()
                        .padding(12.dp, 12.dp, 12.dp, 24.dp)
                        .fillMaxWidth(),
                ) {
                    CustomTextField(
                        textValue = mobilePhone,
                        onValueChange = { mobilePhone = it },
                        labelText = "Phone",
                        onIconClick = {
                            if (isValidPhone) {
                                isOTPSent = !isOTPSent
                                CoroutineScope(Dispatchers.IO).launch {
                                    uploadStringToSupabase(
                                        string = mobilePhone,
                                        tableString = "phone",
                                        client = supabase,
                                        onSuccess = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Data uploaded successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        onError = { errorMessage ->
                                            CoroutineScope(Dispatchers.IO).launch {
                                                withContext(Dispatchers.Main) {
                                                    Log.e("Err", errorMessage)
                                                    Toast.makeText(
                                                        context,
                                                        errorMessage,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                            }
                        },
                        painterForIcon =
                        if (isOTPSent) rememberAsyncImagePainter(R.drawable.settings)
                        else rememberAsyncImagePainter(R.drawable.confirmation),
                        enabledOrNot = !isOTPSent,
                        outlinedTextFieldColors = outlinedTextFieldColors,
                        icon = "phone",
                        isValid = isValidPhone
                    )
                    if (isOTPSent) {
//                            Spacer(modifier = Modifier.height(16.dp))
//                            CoroutineScope(Dispatchers.IO).launch {
//                                supabase.auth.updateUser {
//                                    phone = mobilePhone
//                                }
//                                supabase.auth.currentAccessTokenOrNull()?.let {
//                                    try {
//                                        supabase.auth.verifyPhoneOtp(
//                                            OtpType.Phone.SMS,
//                                            mobilePhone,
//                                            token = it
//                                        )
//                                    } catch (e: Exception){
//                                        Log.e("err", e.toString())
//                                    }
//                                }
//                            }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTextField(
                        textValue = address,
                        onValueChange = { address = it },
                        labelText = "Country, City, Street",
                        onIconClick = {
                            isTFEnabled = !isTFEnabled
                            CoroutineScope(Dispatchers.IO).launch {
                                uploadStringToSupabase(
                                    string = address,
                                    tableString = "address",
                                    client = supabase,
                                    onSuccess = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Data uploaded successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    onError = { errorMessage ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main) {
                                                Log.e("Err", errorMessage)
                                                Toast.makeText(
                                                    context,
                                                    errorMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        painterForIcon =
                        if (isTFEnabled) rememberAsyncImagePainter(R.drawable.settings)
                        else rememberAsyncImagePainter(R.drawable.confirmation),
                        enabledOrNot = !isTFEnabled,
                        outlinedTextFieldColors = colorsAddress,
                        icon = "map",
                        isValid = null
                    )

                }
            }
        }
    }
}

fun startImageCrop(
    sourceUri: Uri,
    context: Context,
    cropLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val destinationUri =
        Uri.fromFile(
            File(
                context.cacheDir,
                "cropped_image_${System.currentTimeMillis()}.jpg"
            )
        )
    val cropIntent = com.yalantis.ucrop.UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(1f, 1f)
        .withMaxResultSize(500, 500)
        .getIntent(context)

    cropLauncher.launch(cropIntent)
}


