package com.example.acharya

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech // NEW: Text to Speech import
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp // NEW: Speaker Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val userProfile by ProfileManager.getProfile(context).collectAsState(initial = UserProfile())
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- LIVE LOCATION STATE ---
    var userLat by remember { mutableStateOf(0.0) }
    var userLong by remember { mutableStateOf(0.0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            LocationHelper.getCurrentLocation(context) { lat, long ->
                userLat = lat
                userLong = long
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // --- SPEECH RECOGNITION (MIC) ---
    var isListening by remember { mutableStateOf(false) }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) { isListening = false }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        inputText = if (inputText.isBlank()) recognizedText else "$inputText $recognizedText"
                    }
                    isListening = false
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            speechRecognizer.startListening(speechIntent)
        }
    }

    // --- NEW: TEXT TO SPEECH (SPEAKER) ---
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS and clean it up when leaving the screen
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }
        tts = textToSpeech

        onDispose {
            speechRecognizer.destroy() // Clean up mic
            textToSpeech.stop()        // Clean up speaker
            textToSpeech.shutdown()
        }
    }
    // ---------------------------------------------

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Assistant") },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("scanner") },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "Scanner")
                    }

                    IconButton(
                        onClick = { navController.navigate("profile") },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }

                    IconButton(
                        onClick = { viewModel.startNewChat() },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }

                    IconButton(onClick = onThemeToggle) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.messages) { message ->
                    // UPDATED: Pass the TTS speak function down to the bubble
                    ChatBubble(
                        message = message,
                        isDarkTheme = isDarkTheme,
                        onSpeak = { text ->
                            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    )
                }

                if (viewModel.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Agent is thinking...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (selectedImageUri != null) {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Attach Photo", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isListening) "Listening..." else "Ask a question...") },
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    speechRecognizer.stopListening()
                                    isListening = false
                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        isListening = true
                                        speechRecognizer.startListening(speechIntent)
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListening) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = !viewModel.isLoading && (inputText.isNotBlank() || selectedImageUri != null)

                IconButton(
                    onClick = {
                        if (canSend) {
                            val imageFile = selectedImageUri?.let { uriToFile(context, it) }
                            viewModel.sendMessage(
                                question = inputText,
                                lat = userLat,
                                long = userLong,
                                imageFile = imageFile,
                                imageUri = selectedImageUri,
                                profile = userProfile
                            )
                            inputText = ""
                            selectedImageUri = null
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier.background(
                        color = if (canSend) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// UPDATED: Now accepts an onSpeak function
@Composable
fun ChatBubble(message: ChatMessage, isDarkTheme: Boolean, onSpeak: (String) -> Unit) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val userBubbleColor = if (isDarkTheme) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val botBubbleColor = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val userTextColor = Color.White
    val botTextColor = if (isDarkTheme) Color.White else Color.Black
    val bubbleColor = if (message.isFromUser) userBubbleColor else botBubbleColor
    val textColor = if (message.isFromUser) userTextColor else botTextColor

    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .background(bubbleColor, bubbleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            if (message.imageUri != null) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "Sent Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = if (message.text.isNotBlank()) 8.dp else 0.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    color = textColor
                )
            }

            // NEW: Speaker Icon (Only shows for the AI's messages)
            if (!message.isFromUser && message.text.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onSpeak(message.text) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}