package com.example.postmark.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postmark.data.Entry
import com.example.postmark.data.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for the entries list.
 *
 * Both ListScreen and MapScreen consume from here, so they always show the
 * same data, and Firestore's real-time listener pushes updates automatically.
 */
class EntriesViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    val entries = repo.observeEntries()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }
    fun add(entry: Entry) = viewModelScope.launch { repo.add(entry) }
}
