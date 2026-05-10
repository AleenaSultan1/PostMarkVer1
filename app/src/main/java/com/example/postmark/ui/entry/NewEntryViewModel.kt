package com.example.postmark.ui.entry

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postmark.data.Entry
import com.example.postmark.data.EntryRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NewEntryUiState(
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val location: String = "",
    val geo: GeoPoint? = null,
    val body: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false
)

class NewEntryViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NewEntryUiState())
    val state = _state.asStateFlow()

    fun onLocationChange(v: String) = _state.update { it.copy(location = v) }
    fun onBodyChange(v: String) = _state.update { it.copy(body = v) }

    /**
     * Pulls the device's current location using FusedLocationProvider.
     * Caller must request ACCESS_FINE_LOCATION permission first.
     *
     * In production, also do reverse geocoding (Geocoder) to fill the
     * location field with "City, Country" instead of leaving it blank.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: Context) {
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val loc = client.lastLocation.await() ?: return@launch
                _state.update {
                    it.copy(geo = GeoPoint(loc.latitude, loc.longitude))
                }
            } catch (_: SecurityException) {
                // Permission not granted — silently skip
            }
        }
    }

    fun save() {
        val s = _state.value
        if (s.body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            repo.add(
                Entry(
                    date = s.date,
                    location = s.location.trim(),
                    geo = s.geo,
                    body = s.body.trim()
                )
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
