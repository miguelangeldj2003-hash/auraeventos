package com.aura.eventos.ui.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.SyncStatus
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.data.repository.EvidenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DeliveryUi(val reservacion: ReservacionDto, val pendientes: Int, val subidas: Int)
data class DeliveriesState(val lista: List<DeliveryUi> = emptyList(), val loading: Boolean = true, val error: String? = null)

@HiltViewModel
class DeliveriesViewModel @Inject constructor(
    private val repo: AuraRepository,
    evidencias: EvidenceRepository
) : ViewModel() {
    // Las entregas vienen del servidor; si no hay internet se conserva la última lista que se descargó
    private val aprobadas = MutableStateFlow<List<ReservacionDto>>(emptyList())
    private val estado = MutableStateFlow(Pair(true, null as String?))

    val ui: StateFlow<DeliveriesState> = combine(aprobadas, evidencias.observeAll(), estado) { lista, ev, st ->
        DeliveriesState(
            lista = lista.map { r ->
                val mias = ev.filter { it.eventFolio == r.folio }
                DeliveryUi(r, mias.count { it.status != SyncStatus.SYNCED }, mias.count { it.status == SyncStatus.SYNCED })
            },
            loading = st.first, error = st.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeliveriesState())

    init { cargar() }

    fun cargar() {
        estado.value = true to null
        viewModelScope.launch {
            val r = repo.reservaciones("APROBADO")
            // Solo eventos de ayer en adelante (la recolección puede ser al día siguiente)
            r.onSuccess { lista ->
                aprobadas.value = lista.filter { x ->
                    runCatching { !LocalDate.parse(x.fechaFin).isBefore(LocalDate.now().minusDays(1)) }.getOrDefault(true)
                }.sortedBy { it.fechaEvento }
            }
            estado.value = false to r.exceptionOrNull()?.message
        }
    }
}
