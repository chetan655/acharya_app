package com.example.acharya

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    // Scaffold automatically provides a framework for TopBars and content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Assistant") },
                actions = {
                    // NEW: The "+" Button for a new chat
                    IconButton(
                        onClick = { viewModel.startNewChat() },
                        // Disable the button if the bot is currently thinking
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat"
                        )
                    }

                    // EXISTING: Your Dark Mode Toggle
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
        // We apply the paddingValues from Scaffold so the content doesn't hide behind the TopBar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Chat Message List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message, isDarkTheme)
                }

                // Loading indicator
                if (viewModel.isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Agent is thinking...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask a question...") },
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(24.dp) // Making the text box pill-shaped like standard apps
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !viewModel.isLoading) {
                            viewModel.sendMessage(inputText, 28.6139, 77.2090, null)
                            inputText = ""
                        }
                    },
                    enabled = !viewModel.isLoading && inputText.isNotBlank(),
                    modifier = Modifier.background(
                        color = if (!viewModel.isLoading && inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (!viewModel.isLoading && inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isDarkTheme: Boolean) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    // Classic Chat App Colors (iMessage / ChatGPT style)
    val userBubbleColor = if (isDarkTheme) Color(0xFF0A84FF) else Color(0xFF007AFF) // Sleek Blue
    val botBubbleColor = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)  // Dark/Light Gray

    val userTextColor = Color.White
    val botTextColor = if (isDarkTheme) Color.White else Color.Black

    val bubbleColor = if (message.isFromUser) userBubbleColor else botBubbleColor
    val textColor = if (message.isFromUser) userTextColor else botTextColor

    // Adjusting corner shapes to give that "tail" effect on the chat bubbles
    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = textColor,
            modifier = Modifier
                .background(bubbleColor, bubbleShape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        )
    }
}