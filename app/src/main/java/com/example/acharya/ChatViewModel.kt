package com.example.acharya

import android.net.Uri
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

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val imageUri: Uri? = null
)

class ChatViewModel : ViewModel() {

    // --- CHAT STATE ---
    val messages = mutableStateListOf<ChatMessage>()
    private var currentThreadId: String = UUID.randomUUID().toString()

    var isLoading by mutableStateOf(false)
        private set

    // --- SCANNER STATE ---
    var scannerResult by mutableStateOf<String?>(null)
        private set

    var isScanning by mutableStateOf(false)
        private set

    // --- CHAT FUNCTIONS ---
    fun startNewChat() {
        currentThreadId = UUID.randomUUID().toString()
        messages.clear()
    }

    fun sendMessage(
        question: String,
        lat: Double,
        long: Double,
        imageFile: File?,
        imageUri: Uri? = null,
        profile: UserProfile? = null
    ) {
        messages.add(ChatMessage(text = question, isFromUser = true, imageUri = imageUri))

        viewModelScope.launch {
            isLoading = true
            try {
                var finalQuestionText = question

                // Stealth Injection of Profile Context
                if (messages.size == 1 && profile != null) {
                    val hasProfileData = profile.name.isNotBlank() || profile.age.isNotBlank() || profile.allergies.isNotBlank() || profile.conditions.isNotBlank()

                    if (hasProfileData) {
                        val hiddenContext = "\n\n[System Note - Patient Profile: Name: ${profile.name}, Age ${profile.age}, Gender ${profile.gender}, Allergies: ${profile.allergies}, Chronic Conditions: ${profile.conditions}. Please address the patient by name if appropriate and consider this context in your medical advice.]"
                        finalQuestionText += hiddenContext
                    }
                }

                val questionBody = finalQuestionText.toRequestBody("text/plain".toMediaTypeOrNull())
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

    // --- SCANNER FUNCTIONS ---
    fun resetScanner() {
        scannerResult = null
    }

    fun analyzePrescription(imageFile: File?) {
        if (imageFile == null) return

        viewModelScope.launch {
            isScanning = true
            scannerResult = null

            try {
                val promptText = "Please analyze this medical document/prescription. Extract and clearly list the following information using bullet points:\n1. Active Ingredients / Medicine Name\n2. Dosage Instructions\n3. Potential Side Effects"

                val questionBody = promptText.toRequestBody("text/plain".toMediaTypeOrNull())
                val latBody = 0.0.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longBody = 0.0.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val oneOffThreadId = UUID.randomUUID().toString()
                val threadIdBody = oneOffThreadId.toRequestBody("text/plain".toMediaTypeOrNull())

                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val responseText = RetrofitClient.api.sendMessage(
                    questionBody, latBody, longBody, threadIdBody, imagePart
                )

                scannerResult = responseText

            } catch (e: Exception) {
                scannerResult = "Error analyzing document: ${e.message}"
            } finally {
                isScanning = false
            }
        }
    }
}