package com.example.acharya

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialProfile by ProfileManager.getProfile(context).collectAsState(initial = UserProfile())

    // UPDATED: Added name state
    var name by remember(initialProfile) { mutableStateOf(initialProfile.name) }
    var age by remember(initialProfile) { mutableStateOf(initialProfile.age) }
    var gender by remember(initialProfile) { mutableStateOf(initialProfile.gender) }
    var allergies by remember(initialProfile) { mutableStateOf(initialProfile.allergies) }
    var conditions by remember(initialProfile) { mutableStateOf(initialProfile.conditions) }

    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "This information helps the AI provide safer, personalized medical advice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // NEW: Name Input Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Known Allergies (e.g., Penicillin)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = { Text("Chronic Conditions (e.g., Asthma)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            if (showSuccessMessage) {
                Text(
                    text = "Profile saved successfully!",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        // UPDATED: Pass the name into the UserProfile
                        ProfileManager.saveProfile(
                            context,
                            UserProfile(name, age, gender, allergies, conditions)
                        )
                        showSuccessMessage = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}