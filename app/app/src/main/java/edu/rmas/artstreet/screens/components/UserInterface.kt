package edu.rmas.artstreet.screens.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import coil.compose.AsyncImage
import edu.rmas.artstreet.R

@Composable
fun Header(header_text: String)
{
    Text(modifier = Modifier.width(250.dp),
    text = header_text,
        color = ColorPalette.Yellow,
    fontSize = 34.sp,
    fontWeight = FontWeight.Bold)
}



@Composable
fun Secondary(secondary_text: String)
{
    Text(style = TextStyle(
        color = ColorPalette.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.End
    ),
        modifier = Modifier.fillMaxWidth().padding(start = 17.dp),
        text = secondary_text
    )
}

@Composable
fun InputFieldLabel(label: String)
{
    Text(style = TextStyle(
        color = ColorPalette.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    ),
        text = label
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    onValueChange: (String) -> Unit = {},
    hint: String,
    value: MutableState<String>,
    isEmail: Boolean = false,
    isNumber: Boolean = false,
    isError: MutableState<Boolean>,
    errorText: MutableState<String>,
    textStyle: TextStyle = TextStyle(fontSize = 18.sp, color = ColorPalette.White)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        TextField(
            value = value.value,
            onValueChange = { newValue ->
                value.value = newValue
                if (isError.value) {
                    isError.value = false
                }
                onValueChange(newValue)
            },
            singleLine = true,
            placeholder = {
                Text(
                    text = hint,
                    style = textStyle.copy(color = Color(0xFFBEC2C2))
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = if (isError.value) Color.Red else Color(0xFFBEC2C2),
                unfocusedIndicatorColor = if (isError.value) Color.Red else Color(0xFFBEC2C2),
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = if (isEmail && !isNumber) {
                KeyboardOptions(keyboardType = KeyboardType.Email)
            } else if (!isEmail && isNumber) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            },
            textStyle = textStyle
        )

        if (isError.value && errorText.value.isNotEmpty()) {
            Text(
                text = errorText.value,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
            )
        } else {
            Text(text = " ")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputField(
    inputValue: MutableState<String>,
    hint: String,
    isError: MutableState<Boolean>,
    errorText: MutableState<String>,
    textStyle: TextStyle = TextStyle(fontSize = 18.sp, color = ColorPalette.White)
) {
    var showPassword by remember { mutableStateOf(false) }

    TextField(
        value = inputValue.value,
        onValueChange = { newValue ->
            inputValue.value = newValue
            if (isError.value) {
                isError.value = false
            }
        },
        singleLine = true,
        placeholder = {
            Text(
                text = hint,
                style = textStyle.copy(color = Color(0xFFBEC2C2))
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                showPassword = !showPassword
            }) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = Color(0xFFBEC2C2)
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = if (isError.value) Color.Red else Color(0xFFBEC2C2),
            unfocusedIndicatorColor = if (isError.value) Color.Red else Color(0xFFBEC2C2),
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        textStyle = textStyle
    )

    if (isError.value && errorText.value.isNotEmpty()) {
        Text(
            text = errorText.value,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                textAlign = TextAlign.Center,
                color = Color.Red
            )
        )
    } else {
        Text(text = " ")
    }
}


@Composable
fun SignUpInButton(
    onClick: () -> Unit = {},
    text: String,
    icon: ImageVector? = null,
    isEnabled: MutableState<Boolean> = mutableStateOf(true),
    isLoading: MutableState<Boolean> = mutableStateOf(false),
    buttonColor: Color = ColorPalette.Yellow,
    textColor: Color = ColorPalette.Secondary,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 2.dp)
            .height(50.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled.value) buttonColor else Color.LightGray,
            contentColor = textColor,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        enabled = isEnabled.value
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null && !isLoading.value) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = text,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CallToActionText(
    promptText: String,
    ctaLinkText: String,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = promptText,
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorPalette.White,
                fontStyle = FontStyle.Italic,
            )
        )
        Text(
            text = ctaLinkText,
            modifier = Modifier
                .clickable {
                    onClick()
                }
                .padding(start = 4.dp),
            style = TextStyle(
                fontSize = 14.sp,
                color = ColorPalette.Yellow,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@Composable
fun UploadIcon(
    selectedImageUri: MutableState<Uri?>,
    isError: MutableState<Boolean>

) {
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri.value = uri
        }
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri.value == Uri.EMPTY || selectedImageUri.value == null) {
            Image(
                painter = painterResource(id = R.drawable.pfp),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(140.dp)
                    .border(
                        if (isError.value) BorderStroke(2.dp, Color.Red) else BorderStroke(
                            0.dp,
                            Color.Transparent
                        )
                    )
                    .clip(RoundedCornerShape(70.dp)) // 50% border radius
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
        } else {
            selectedImageUri.value?.let { uri ->
                Image(
                    painter = painterResource(id = R.drawable.pfp),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(140.dp)
                        .border(
                            if (isError.value) BorderStroke(2.dp, Color.Red) else BorderStroke(
                                0.dp,
                                Color.Transparent
                            )
                        )
                        .clip(RoundedCornerShape(70.dp)) // 50% border radius
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                )

                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(70.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun customErrorContainer(
    errorText: String
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = errorText,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        )
    }
}











@Composable
fun DashedLineBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.BackgroundMain)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp)
        ) {
            val lineWidth = 24.dp.toPx()
            val dashHeight = 214.dp.toPx()
            val dashGap = 47.dp.toPx()
            val lineStartX = 0f

            var yOffset = 0f
            while (yOffset < size.height) {
                drawRect(
                    color = ColorPalette.Yellow,
                    topLeft = Offset(lineStartX, yOffset),
                    size = Size(lineWidth, dashHeight)
                )
                yOffset += dashHeight + dashGap
            }
        }

        // Content overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 64.dp)
        ) {
            content()
        }
    }
}


@Composable
fun CopyrightText(
    year: Int,
    owner: String = "18859",
    textColor: Color = Color.Gray,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "© $year - $owner",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        )
    }
}




