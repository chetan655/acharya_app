package com.example.acharya

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.filled.DocumentScanner // For the Scanner Button
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Assistant") },
                actions = {
                    // NEW: Document Scanner Button
                    IconButton(
                        onClick = { navController.navigate("scanner") },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "Scanner")
                    }

                    // Profile Button
                    IconButton(
                        onClick = { navController.navigate("profile") },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }

                    // New Chat Button
                    IconButton(
                        onClick = { viewModel.startNewChat() },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }

                    // Theme Toggle
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
                    ChatBubble(message, isDarkTheme)
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
                    placeholder = { Text("Ask a question...") },
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = !viewModel.isLoading && (inputText.isNotBlank() || selectedImageUri != null)

                IconButton(
                    onClick = {
                        if (canSend) {
                            val imageFile = selectedImageUri?.let { uriToFile(context, it) }
                            viewModel.sendMessage(inputText, 28.6139, 77.2090, imageFile, selectedImageUri, userProfile)
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

@Composable
fun ChatBubble(message: ChatMessage, isDarkTheme: Boolean) {
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