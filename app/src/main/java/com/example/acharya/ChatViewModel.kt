package com.example.acharya
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    private var currentThreadId: String = ""

    fun sendMessage(question: String, lat: Double, long: Double, imageFile: File?) {

        messages.add(ChatMessage(text = question, isFromUser = true))

        viewModelScope.launch {
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

                // Make the API call (this now returns a plain String)
                val responseText = RetrofitClient.api.sendMessage(
                    questionBody, latBody, longBody, threadIdBody, imagePart
                )

                // Add the raw string response directly to the UI
                messages.add(ChatMessage(text = responseText, isFromUser = false))

            } catch (e: Exception) {
                // Handle error
                messages.add(ChatMessage(text = "Error: ${e.message}", isFromUser = false))
            }
        }
    }
}