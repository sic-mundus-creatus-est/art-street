package edu.rmas.artstreet.screens.components

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShareLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import edu.rmas.artstreet.R
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.models.Artwork
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@Composable
fun Header(header_text: String, color: Color = ColorPalette.Yellow)
{
    Text(modifier = Modifier.width(250.dp),
    text = header_text,
        color = color,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 17.dp),
        text = secondary_text
    )
}


@Composable
fun TheDivider(
    color: Color = ColorPalette.Yellow, // Default color
    thickness: Dp = 2.dp // Default thickness
) {
    Divider(
        color = color,
        thickness = thickness,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    style = textStyle.copy(color = ColorPalette.LightGray)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = if (isError.value) Color.Red else ColorPalette.Yellow,
                unfocusedIndicatorColor = if (isError.value) Color.Red else ColorPalette.LightGray,
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
                style = textStyle.copy(color = ColorPalette.LightGray)
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                showPassword = !showPassword
            }) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = ColorPalette.LightGray
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = if (isError.value) Color.Red else ColorPalette.Yellow,
            unfocusedIndicatorColor = if (isError.value) Color.Red else ColorPalette.LightGray,
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
fun PrimaryArtworkPhotoUploadField(
    selectedImageUri: MutableState<Uri?>
){
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri.value = uri
        }
    )

    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(vertical = 2.dp)
        .shadow(
            6.dp,
            shape = RoundedCornerShape(20.dp)
        )
        .border(
            1.dp,
            Color.Transparent,
            shape = RoundedCornerShape(20.dp)
        )
        .background(
            ColorPalette.White,
            shape = RoundedCornerShape(20.dp)
        ),
        contentAlignment = Alignment.Center
    ){
        if (selectedImageUri.value == Uri.EMPTY) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = ""
                )
                Text(text = "Add primary artwork photo (cover)")
            }
        }else{
            selectedImageUri.value?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultilineInputField(
    inputValue: MutableState<String>,
    inputText: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = inputValue.value,
            onValueChange = { newValue ->
                inputValue.value = newValue
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), // Slightly transparent background
                    shape = RectangleShape
                ),
            placeholder = {
                Text(
                    text = inputText,
                    style = TextStyle(
                        color = ColorPalette.LightGray,
                        fontWeight = FontWeight.Normal
                    )
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = ColorPalette.White,
                unfocusedTextColor = ColorPalette.White,
                focusedBorderColor = ColorPalette.Yellow,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            keyboardOptions = KeyboardOptions.Default,
            maxLines = 10,
            shape = RectangleShape,
        )
    }
}


@Composable
fun ArtworkGalleryUploadField(
    selectedImages: MutableState<List<Uri>>,
    modifier: Modifier = Modifier,
    maxImages: Int = 7
) {
    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val currentImageCount = selectedImages.value.size
        val remainingCapacity = maxImages - currentImageCount

        if (remainingCapacity > 0) {
            val newImages = uris.take(remainingCapacity)
            if (newImages.isNotEmpty()) {
                selectedImages.value = selectedImages.value + newImages
            }
        }
    }

    val scrollState = rememberLazyListState()
    val isScrolled = scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    if (selectedImageUri != null) {
        FullScreenImageDialog(
            imageUri = selectedImageUri!!,
            onDismiss = { selectedImageUri = null }
        )
    }

    Box(modifier = modifier) {
        if (isScrolled) {
            Divider(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                thickness = 2.dp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        LazyRow(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(selectedImages.value.size) { index ->
                val uri = selectedImages.value[index]
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(100.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedImageUri = uri }
                    )

                    Icon( // X button to remove image
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove Image",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .clickable { selectedImages.value = selectedImages.value
                                .toMutableList()
                                .apply { remove(uri) }
                            }
                    )
                }
            }

            if (selectedImages.value.size < maxImages) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { pickImagesLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add Image",
                            tint = ColorPalette.Yellow,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(imageUri: Uri, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDismiss() }
            )
        }
    }
}


@Composable
fun PostButton(
    onClick: () -> Unit = {},
    text: String,
    isEnabled: MutableState<Boolean> = mutableStateOf(true),
    isLoading: MutableState<Boolean> = mutableStateOf(false),
    buttonColor: Color = ColorPalette.Yellow,
    textColor: Color = ColorPalette.Secondary,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(vertical = 8.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        ),
        shape = RectangleShape,
        enabled = isEnabled.value,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            // Rectangle containing the text
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = if (isEnabled.value) buttonColor else Color.LightGray,
                        shape = RectangleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = textColor
                        )
                    )
                }
            }

            // Right-pointing equilateral triangle
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f) // Ensure the canvas is square
            ) {
                val trianglePath = Path().apply {
                    val sideLength = size.height// Since it's a square, height equals width
                    moveTo(0f, 0f) // Start at the top-left corner
                    lineTo(sideLength, size.height * 0.5f) // Line to the middle of the right side
                    lineTo(0f, sideLength) // Line to the bottom-left corner
                    close() // Close the path
                }

                drawPath(
                    path = trianglePath,
                    color = if (isEnabled.value) buttonColor else Color.LightGray
                )
            }
        }
    }
}











@Composable
fun UserImage(
    imageUrl: String,
    name: String,
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp), contentAlignment = Alignment.Center){
        Row {
            AsyncImage(
                model = imageUrl,
                contentDescription = "userimage",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .border(
                        3.dp,
                        Color.Gray,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .shadow(
                        6.dp,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .clip(shape = RoundedCornerShape(0.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(40.dp))
            Column {
                Text(
                    modifier = Modifier
                        .padding(10.dp),
                    text = name.replace(","," "),
                    color = ColorPalette.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun myPositionIndicator(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}

@Composable
fun ArtworkMarker(
    artwork: Artwork,
    icon: BitmapDescriptor?,
    artworksMarkers : MutableList<Artwork>,
    navController: NavController,
    notFiltered: Boolean
){
    Marker(
        state = if(notFiltered){
            rememberMarkerState(
                position = LatLng(
                    artwork.location.latitude,
                    artwork.location.longitude
                )
            )
        }
        else{
            MarkerState(
                position = LatLng(
                    artwork.location.latitude,
                    artwork.location.longitude
                ))
        }
        ,
        title = artwork.title,
        icon = icon,
        snippet = artwork.description,
        onClick = {
            val artworkJson = Gson().toJson(artwork)
            val encodedArtworkJson =
                URLEncoder.encode(
                    artworkJson,
                    StandardCharsets.UTF_8.toString()
                )

            navController.navigate(Routes.artworkScreen + "/$encodedArtworkJson")
            true
        }
    )
}



@Composable
fun SearchBar(
    inputValue: MutableState<String>,
    artworkData: MutableList<Artwork>,
    navController: NavController,
    cameraPositionState: CameraPositionState
){
    val focusRequester = remember{
        FocusRequester()
    }
    val onFocus = remember {
        mutableStateOf(false)
    }

    val artworks = remember {
        mutableListOf<Artwork>()
    }

    artworks.clear()
    artworks.addAll(searchLogic(artworkData, inputValue.value).toMutableList())

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.width(270.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .height(50.dp)
                .focusRequester(focusRequester = focusRequester)
                .onFocusChanged { focusState ->
                    onFocus.value = focusState.isFocused
                }
                .background(
                    Color.White,
                    shape = RoundedCornerShape(7.dp)
                )
                .border(
                    1.dp,
                    ColorPalette.BackgroundMainLighter,
                    shape = RoundedCornerShape(7.dp)
                ),
            value = inputValue.value,
            onValueChange = { newValue ->
                inputValue.value = newValue
                onFocus.value = true
            },
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search...",
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "",
                    tint = ColorPalette.Yellow
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = ColorPalette.BackgroundMainLighter,
                unfocusedContainerColor = ColorPalette.White,
                focusedTextColor = ColorPalette.White,
                unfocusedTextColor = ColorPalette.DarkGrey
            ),
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions.Default
        )
        if(onFocus.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(ColorPalette.BackgroundMainLighter)
                    .border(
                        1.dp,
                        ColorPalette.BackgroundMainDarker,
                        shape = RoundedCornerShape(7.dp)
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    for (artwork in artworks) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp)
                                    .clickable {
                                        val artworkJson = Gson().toJson(artwork)
                                        val encodedArtworkJson = URLEncoder.encode(
                                            artworkJson,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate(Routes.artworkScreen + "/$encodedArtworkJson")
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            ColorPalette.BackgroundMainDarker,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                        .background(
                                            color = ColorPalette.White,
                                            shape = RoundedCornerShape(1.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = artwork.primaryImage,
                                        contentDescription = "",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(40.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = artwork.title.replace("+", " "), color = ColorPalette.DarkGrey)
                                }

                                IconButton(
                                    onClick = {
                                        onFocus.value = false
                                        keyboardController?.hide()
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(artwork.location.latitude, artwork.location.longitude), 17f)
                                    },
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MyLocation,
                                        contentDescription = "",
                                        tint = ColorPalette.Yellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun searchLogic(
    artworks: MutableList<Artwork>,
    inputValue: String
): List<Artwork>{
    val regex = inputValue.split(" ").joinToString(".*"){
        Regex.escape(it)
    }.toRegex(RegexOption.IGNORE_CASE)

    return artworks.filter { artwork ->
        regex.containsMatchIn(artwork.title)
    }
}



@Composable
fun SimpleInputField(
    inputValue: MutableState<String>,
    inputText: String,
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(
                1.dp,
                Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                Color.White,
                shape = RoundedCornerShape(10.dp)
            )
    ){
        OutlinedTextField(
            value = inputValue.value,
            onValueChange = { newValue ->
                inputValue.value = newValue
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = inputText,
                    style = TextStyle(
                        color = ColorPalette.Secondary,
                        fontWeight = FontWeight.Medium
                    )
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormInputField(
    inputValue: MutableState<String>,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = inputValue.value,
            onValueChange = { newValue ->
                inputValue.value = newValue
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), // Slightly transparent background
                    shape = RectangleShape
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        color = ColorPalette.DarkGrey,
                        fontWeight = FontWeight.Normal
                    )
                )
            },
            leadingIcon = leadingIcon,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = ColorPalette.Yellow,
                unfocusedBorderColor = ColorPalette.BackgroundMainDarker,
                //textColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                //backgroundColor = Color.Transparent // Transparent container
            ),
            shape = RectangleShape, // Remove rounded corners
        )
    }
}





@Composable
fun ProfilePicture(
    imageUrl: String
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp), contentAlignment = Alignment.Center){
        Row {
            AsyncImage (
                model = imageUrl,
                contentDescription = "profile_picture",
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
                    .border(
                        3.dp,
                        Color.Gray,
                    )
                    .shadow(
                        6.dp,
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit
){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                Color.Transparent,
                RoundedCornerShape(5.dp)
            )
            .padding(0.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBackIosNew,
            contentDescription = ""
        )
    }
}

@Composable
fun PhotosSection(
    artworks: List<Artwork>,
    navController: NavController
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(text = "Added locations")
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if(artworks.isNotEmpty()) {
                for (artwork in artworks) {
                    item {
                        AsyncImage(
                            model = artwork.primaryImage,
                            contentScale = ContentScale.Crop,
                            contentDescription = "",
                            modifier =
                            Modifier
                                .width(150.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Color.White,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    val artworkJson = Gson().toJson(artwork)
                                    val encodedArtworkJson = URLEncoder.encode(
                                        artworkJson,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate(Routes.artworkScreen + "/$encodedArtworkJson")
                                }
                        )
                    }
                }
            }else{
                item {
                    Image(
                        imageVector = Icons.Filled.DoNotDisturb,
                        contentScale = ContentScale.Crop,
                        contentDescription = "",
                        modifier =
                        Modifier
                            .width(150.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Color.Gray,
                                RoundedCornerShape(20.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun PrimaryArtworkPhoto(
    imageUrl: String,
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RectangleShape,
                clip = false,
                spotColor = Color.Black,
                ambientColor = Color.Gray,
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RectangleShape),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun LocationTag(
    location: LatLng,
    context: Context
) {
    var addressText by remember { mutableStateOf("Fetching location...") }

    LaunchedEffect(location) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addressText = if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val streetNumber = address.subThoroughfare ?: ""
            val streetName = address.thoroughfare ?: ""
            val locality = address.locality ?: ""
            val country = address.countryName ?: ""
            "$streetName $streetNumber, $locality, $country"
        } else {
            "Unknown location"
        }
    }

    Row(
        modifier = Modifier.padding(start = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "",
            tint = ColorPalette.Yellow
        )

        Text(
            style = TextStyle(
                color = ColorPalette.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            ),
            text = addressText
        )
    }
}

@Composable
fun ArtworkDescription(description: String) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val annotatedText = buildAnnotatedString {
        if (isDescriptionExpanded) {
            // Full description when expanded
            withStyle(style = SpanStyle(color = ColorPalette.LightGray, fontSize = 14.sp)) {
                append(description.replace('+', ' '))
            }
        } else {
            // Truncated description with "Read more"
            withStyle(style = SpanStyle(color = ColorPalette.LightGray, fontSize = 14.sp)) {
                append(description.take(100).replace('+', ' ') + "...")
            }
            withStyle(style = SpanStyle(color = ColorPalette.Blue, fontSize = 14.sp)) {
                pushStringAnnotation(tag = "TOGGLE", annotation = "TOGGLE")
                append(" Read more")
                pop()
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ClickableText(
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations(tag = "TOGGLE", start = offset, end = offset)
                    .firstOrNull()?.let {
                        isDescriptionExpanded = !isDescriptionExpanded
                    } ?: run {
                    if (isDescriptionExpanded) {
                        isDescriptionExpanded = false
                    }
                }
            },
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}









@Composable
fun ArtworkPhotoGrid(images: List<String> )
{
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = ColorPalette.BackgroundMainDarker))
    {
        Spacer(modifier = Modifier.height(5.dp))

        for (index in images.indices step 3)
        { // step  3 to primarily show three images per row
            Row(modifier = Modifier.fillMaxWidth()) {
                val remainingImages = images.size - index

                // First image
                AsyncImage (
                    model = images[index],
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f) // distributes available width equally
                        .aspectRatio(1f) // keeps the images square
                        .clickable { selectedIndex = index }
                )
                if(remainingImages != 1) {
                    Spacer(modifier = Modifier.width(5.dp))
                }

                // Second image
                if (index + 1 < images.size) {
                    AsyncImage(
                        model = images[index + 1],
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { selectedIndex = index + 1 }
                    )
                    if(remainingImages != 1 && remainingImages != 2) {
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }

                // Third image
                if (index + 2 < images.size) {
                    AsyncImage(
                        model = images[index + 2],
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { selectedIndex = index + 2 }
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    // Image viewer
    if (selectedIndex != null) {
        Dialog(
            onDismissRequest = { selectedIndex = null }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize()
            ) {
                AsyncImage(
                    model = images[selectedIndex!!],
                    contentDescription = "Full Size Image",
                    contentScale = ContentScale.Fit,
                )

                // Left navigation button
                if (selectedIndex!! > 0) {
                    IconButton(
                        onClick = { selectedIndex = selectedIndex!! - 1 },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Image",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Right navigation button
                if (selectedIndex!! < images.size - 1) {
                    IconButton(
                        onClick = { selectedIndex = selectedIndex!! + 1 },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Image",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}





@Composable
fun ArtFeedPost (
    artwork: Artwork,
    artworkScreen: () -> Unit,
    artworkOnMap: () -> Unit
) {
    // State to control the expanded/collapsed state of the description
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(
                ColorPalette.BackgroundMainLighter,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { artworkScreen() }
            .padding(12.dp)
    ) {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage (
                model = artwork.galleryImages[0],
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        // Title and IconButton row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),  // Space between image and text row
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = artworkOnMap,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShareLocation,
                    contentDescription = "",
                    tint = ColorPalette.Yellow
                )
            }

            Text(
                text = if (artwork.title.length > 40) artwork.title.substring(0, 40).replace('+', ' ') + "..." else artwork.title.replace('+', ' '),
                style = TextStyle(
                    fontSize = 20.sp,
                    color = ColorPalette.Yellow,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TheDivider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(2.dp))

        // Description text with expand/collapse functionality
        ArtworkDescription(description = artwork.description)
    }
}
















@Composable
fun DashedLineBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.BackgroundMainLighter)
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
    textColor: Color = ColorPalette.White,
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




