package com.example.hotelres.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hotelres.data.model.Booking
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "hotel_prefs")

class FavoriteHotelsDataStore(private val context: Context) {

    companion object {
        private val FAVORITE_HOTELS_KEY = stringSetPreferencesKey("favorite_hotels")
        private val BOOKINGS_KEY = stringPreferencesKey("bookings_json")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
    }

    private val gson = Gson()

    // Favorite Hotels
    val favoriteHotelIdsFlow: Flow<Set<Int>> = context.dataStore.data
        .map { prefs ->
            prefs[FAVORITE_HOTELS_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }

    suspend fun saveFavoriteHotelIds(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[FAVORITE_HOTELS_KEY] = ids.map { it.toString() }.toSet()
        }
    }

    // Bookings
    val bookingsFlow: Flow<List<Booking>> = context.dataStore.data.map { prefs ->
        prefs[BOOKINGS_KEY]?.let { json ->
            val type = object : TypeToken<List<Booking>>() {}.type
            gson.fromJson(json, type)
        } ?: emptyList()
    }

    // Profile Image URI flow
    val profileImageUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PROFILE_IMAGE_URI_KEY]
    }

    suspend fun saveProfileImageUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(PROFILE_IMAGE_URI_KEY)
            } else {
                prefs[PROFILE_IMAGE_URI_KEY] = uri
            }
        }
    }

    suspend fun saveBookings(bookings: List<Booking>) {
        context.dataStore.edit { prefs ->
            prefs[BOOKINGS_KEY] = gson.toJson(bookings)
        }
    }
}
