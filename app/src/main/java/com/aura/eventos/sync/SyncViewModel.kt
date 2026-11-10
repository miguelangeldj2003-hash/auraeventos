package com.aura.eventos.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.EvidenceEntity
import com.aura.eventos.data.local.SyncStatus
import com.aura.eventos.data.repository.EvidenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SyncUiState {
    data object Hidden : SyncUiState
    data class Offline(val pending: Int) : SyncUiState
    data class Syncing(val done: Int, val total: Int) : SyncUiState
    data class Done(val count: Int) : SyncUiState
    data class Error(val failed: Int) : SyncUiState
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val repo: EvidenceRepository,
    connectivity: ConnectivityObserver,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    val items: StateFlow<List<EvidenceEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lastSyncedAt: StateFlow<Long?> = items
        .map { l -> l.mapNotNull { it.syncedAt }.maxOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var batchStart: Long? = null      // inicio del "lote" actual de subida
    private var doneTimer: Job? = null
    private val tick = MutableStateFlow(0)    // fuerza recalcular al ocultar "Listo"

    val uiState: StateFlow<SyncUiState> =
        combine(connectivity.isOnline, items, tick) { online, list, _ ->
            val pending = list.count { it.status == SyncStatus.PENDING || it.status == SyncStatus.UPLOADING }
            val failed = list.count { it.status == SyncStatus.FAILED }
            if (pending > 0 && batchStart == null) { batchStart = System.currentTimeMillis(); doneTimer?.cancel() }
            val start = batchStart
            val syncedInBatch = if (start == null) 0
            else list.count { it.status == SyncStatus.SYNCED && (it.syncedAt ?: 0L) >= start }
            when {
                pending > 0 && !online -> SyncUiState.Offline(pending)
                pending > 0 -> SyncUiState.Syncing(syncedInBatch, syncedInBatch + pending)
                failed > 0 -> SyncUiState.Error(failed)
                start != null -> {
                    if (doneTimer?.isActive != true) {
                        doneTimer = viewModelScope.launch { delay(6_000); batchStart = null; tick.value++ }
                    }
                    SyncUiState.Done(syncedInBatch)
                }
                else -> SyncUiState.Hidden
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState.Hidden)

    init {
        viewModelScope.launch { repo.resetStuck(); SyncScheduler.enqueue(ctx) }
    }

    fun retryAll() = viewModelScope.launch { repo.requeueFailed(); SyncScheduler.enqueue(ctx) }
}
