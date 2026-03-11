package com.example.acharya

import androidx.compose.runtime.getValue // Add this
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf // Add this
import androidx.compose.runtime.setValue // Add this
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    private var currentThreadId: String = UUID.randomUUID().toString()

    // Call this to reset the session and clear the screen
    fun startNewChat() {
        currentThreadId = UUID.randomUUID().toString()
        messages.clear()
    }

    // NEW: State to track if the bot is thinking
    var isLoading by mutableStateOf(false)
        private set

    fun sendMessage(question: String, lat: Double, long: Double, imageFile: File?) {
        // Don't send empty messages
        if (question.isBlank() && imageFile == null) return

        // Add user message to UI immediately
        messages.add(ChatMessage(text = question, isFromUser = true))

        viewModelScope.launch {
            // Tell the UI to start spinning!
            isLoading = true

            try {
                val questionBody = question.toRequestBody("text/plain".toMediaTypeOrNull())
                val latBody = lat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longBody = long.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val threadIdBody = currentThreadId.toRequestBody("text/plain".toMediaTypeOrNull())

                var imagePart: MultipartBody.Part? = null
                if (imageFile != null) {
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                }

                val responseText = RetrofitClient.api.sendMessage(
                    questionBody, latBody, longBody, threadIdBody, imagePart
                )

                messages.add(ChatMessage(text = responseText, isFromUser = false))

            } catch (e: Exception) {
                messages.add(ChatMessage(text = "Error: ${e.message}", isFromUser = false))
            } finally {
                // Tell the UI to stop spinning, regardless of success or failure
                isLoading = false
            }
        }
    }
}