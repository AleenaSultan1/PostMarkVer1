package com.example.postmark.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore reads/writes for journal entries.
 *
 * Data model is flat and per-user:
 *   users/{uid}/entries/{entryId}
 *
 * That nesting makes security rules trivial — see firestore.rules in the repo:
 *   match /users/{userId}/entries/{entry} {
 *     allow read, write: if request.auth.uid == userId;
 *   }
 */
class EntryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun entriesRef() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("entries")
    } ?: throw IllegalStateException("Not signed in")

    /** Real-time stream of all entries for the current user, newest first. */
    fun observeEntries(): Flow<List<Entry>> = callbackFlow {
        val registration = entriesRef()
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Entry::class.java)?.also { it.id = doc.id }
                }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun add(entry: Entry): String {
        val ref = entriesRef().add(entry).await()
        return ref.id
    }

    suspend fun delete(entryId: String) {
        entriesRef().document(entryId).delete().await()
    }

    suspend fun deleteAll() {
        // Firestore doesn't have a true "delete collection" — fetch and batch.
        val snap = entriesRef().get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
