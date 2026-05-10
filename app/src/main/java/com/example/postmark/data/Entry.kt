package com.example.postmark.data

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * A single travel journal entry.
 *
 * Stored at: users/{uid}/entries/{entryId}
 *
 * The no-arg constructor and `var` fields are required by Firestore's
 * automatic serialization — don't convert these to `val`.
 */
data class Entry(
    var id: String = "",
    var date: String = "",            // ISO date "2026-04-14" — sortable as a string
    var location: String = "",        // Human-readable, e.g. "Budapest, Hungary"
    var geo: GeoPoint? = null,        // For the map view
    var body: String = "",
    var photoUrls: List<String> = emptyList(),
    @ServerTimestamp var createdAt: Date? = null
)
