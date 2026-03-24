package com.example.acharya

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices

object LocationHelper {

    // We suppress the permission warning here because we will explicitly
    // ask the user for permission in our Jetpack Compose UI before calling this.
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onLocationRetrieved: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Success! Pass the real coordinates back
                    onLocationRetrieved(location.latitude, location.longitude)
                } else {
                    // Fallback if GPS is disabled or unavailable
                    onLocationRetrieved(0.0, 0.0)
                }
            }
            .addOnFailureListener {
                // Fallback on error
                onLocationRetrieved(0.0, 0.0)
            }
    }
}