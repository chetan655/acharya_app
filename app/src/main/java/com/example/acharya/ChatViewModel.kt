package com.example.acharya

import android.net.Uri // NEW: Needed for image URIs
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

// UPDATED: Now it can store an imageUri!
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()
    private var currentThreadId: String = UUID.randomUUID().toString()

    var isLoading by mutableStateOf(false)
        private set

    fun startNewChat() {
        currentThreadId = UUID.randomUUID().toString()
        messages.clear()
    }

    // UPDATED: Added imageUri as a parameter
    fun sendMessage(question: String, lat: Double, long: Double, imageFile: File?, imageUri: Uri? = null) {

        // Add user message (and image) to the UI immediately
        messages.add(ChatMessage(text = question, isFromUser = true, imageUri = imageUri))

        viewModelScope.launch {
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
                isLoading = false
            }
        }
    }
}