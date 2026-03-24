package com.example.acharya

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

// UPDATED: Added name field
data class UserProfile(
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val allergies: String = "",
    val conditions: String = ""
)

object ProfileManager {
    private val NAME = stringPreferencesKey("name") // NEW
    private val AGE = stringPreferencesKey("age")
    private val GENDER = stringPreferencesKey("gender")
    private val ALLERGIES = stringPreferencesKey("allergies")
    private val CONDITIONS = stringPreferencesKey("conditions")

    suspend fun saveProfile(context: Context, profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[NAME] = profile.name // NEW
            preferences[AGE] = profile.age
            preferences[GENDER] = profile.gender
            preferences[ALLERGIES] = profile.allergies
            preferences[CONDITIONS] = profile.conditions
        }
    }

    fun getProfile(context: Context): Flow<UserProfile> {
        return context.dataStore.data.map { preferences ->
            UserProfile(
                name = preferences[NAME] ?: "", // NEW
                age = preferences[AGE] ?: "",
                gender = preferences[GENDER] ?: "",
                allergies = preferences[ALLERGIES] ?: "",
                conditions = preferences[CONDITIONS] ?: ""
            )
        }
    }
}