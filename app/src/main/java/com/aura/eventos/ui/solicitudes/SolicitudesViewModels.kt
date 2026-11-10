package com.aura.eventos.ui.solicitudes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.Session
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.ui.components.Tipo
import com.aura.eventos.ui.components.ToastEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ---------- lista (cliente: "Mis solicitudes"; administradora: "Agenda") ----------
data class ListaUi(
    val lista: List<ReservacionDto> = emptyList(), val loading: Boolean = true, val error: String? = null,
    val filtro: String? = null, val busqueda: String = "", val recientesPrimero: Boolean = true
) {
    val visibles: List<ReservacionDto> get() = lista
        .filter { filtro == null || it.estado == filtro }
        .filter { busqueda.isBlank() || it.folio.contains(busqueda, true) || it.cliente.contains(busqueda, true) ||
            it.detalles.any { d -> d.nombre.contains(busqueda, true) } }
        .let { l -> if (recientesPrimero) l.sortedByDescending { it.creadoEn ?: "" } else l.sortedBy { it.fechaEvento } }
    fun cuenta(estado: String?) = lista.count { estado == null || it.estado == estado }
}

@HiltViewModel
class SolicitudesViewModel @Inject constructor(private val repo: AuraRepository, sessionManager: SessionManager) : ViewModel() {
    private val _ui = MutableStateFlow(ListaUi())
    val ui: StateFlow<ListaUi> = _ui.asStateFlow()
    val session: StateFlow<Session?> = sessionManager.session

    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = repo.reservaciones()
            _ui.update { it.copy(loading = false, lista = r.getOrDefault(emptyList()), error = r.exceptionOrNull()?.message) }
        }
    }
    fun filtro(f: String?) = _ui.update { it.copy(filtro = f) }
    fun buscar(t: String) = _ui.update { it.copy(busqueda = t) }
    fun orden() = _ui.update { it.copy(recientesPrimero = !it.recientesPrimero) }
}

// ---------- inicio del cliente (Mi Dashboard) ----------
@HiltViewModel
class InicioViewModel @Inject constructor(private val repo: AuraRepository, sessionManager: SessionManager) : ViewModel() {
    val session: StateFlow<Session?> = sessionManager.session
    private val _lista = MutableStateFlow<List<ReservacionDto>>(emptyList())
    val lista: StateFlow<List<ReservacionDto>> = _lista.asStateFlow()
    val cargando = MutableStateFlow(true)
    val error = MutableStateFlow<String?>(null)

    init { cargar() }

    fun cargar() {
        cargando.value = true; error.value = null
        viewModelScope.launch {
            val r = repo.reservaciones()
            _lista.value = r.getOrDefault(emptyList())
            error.value = r.exceptionOrNull()?.message
            cargando.value = false
        }
    }
}

// ---------- detalle / revisión de una solicitud ----------
data class DetalleUi(
    val r: ReservacionDto? = null, val loading: Boolean = true, val error: String? = null,
    val disponibles: Map<Long, Int> = emptyMap(),
    val flete: String = "", val motivo: String = "", val errorFlete: String? = null,
    val procesando: Boolean = false, val descargando: Boolean = false,
    val notaLista: Boolean = false, val confirmarRechazo: Boolean = false
)

@HiltViewModel
class SolicitudDetalleViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: AuraRepository,
    sessionManager: SessionManager
) : ViewModel() {
    private val id: Long = savedState.get<Long>("id") ?: 0L
    val esAdmin: Boolean = sessionManager.session.value?.esAdmin == true
    private val _ui = MutableStateFlow(DetalleUi())
    val ui: StateFlow<DetalleUi> = _ui.asStateFlow()
    val eventos = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 4)
    val pdf = MutableSharedFlow<File>(extraBufferCapacity = 1)

    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = repo.reservacion(id)
            val dto = r.getOrNull()
            // La administradora ve cuántas piezas quedan libres en las fechas de la solicitud
            var disp = emptyMap<Long, Int>()
            if (esAdmin && dto != null) {
                disp = repo.articulos(dto.fechaEvento, dto.fechaFin).getOrNull()?.associate { it.id to (it.disponibles ?: it.stockTotal) } ?: emptyMap()
            }
            _ui.update { it.copy(loading = false, r = dto, disponibles = disp, error = r.exceptionOrNull()?.message,
                flete = if (dto != null && dto.estado != "PENDIENTE") dto.costoFlete.toString() else it.flete) }
        }
    }

    fun flete(v: String) = _ui.update { it.copy(flete = v.filter { c -> c.isDigit() || c == '.' }, errorFlete = null) }
    fun motivo(v: String) = _ui.update { it.copy(motivo = v) }
    fun pedirRechazo(v: Boolean) = _ui.update { it.copy(confirmarRechazo = v) }
    fun cerrarNota() = _ui.update { it.copy(notaLista = false) }

    fun aprobar() {
        val r = _ui.value.r ?: return
        val flete = _ui.value.flete.toDoubleOrNull()
        if (flete == null) { _ui.update { it.copy(errorFlete = "Asigna el costo de flete antes de aprobar (puede ser 0)") }; return }
        _ui.update { it.copy(procesando = true, error = null) }
        viewModelScope.launch {
            val res = repo.aprobar(r.id, flete)
            res.onSuccess { nueva ->
                _ui.update { it.copy(procesando = false, r = nueva, notaLista = true) }
                eventos.tryEmit(ToastEvent(Tipo.EXITO, "¡Solicitud Aprobada y Stock Bloqueado!", "El pedido ${nueva.folio.let { f -> "#$f" }} fue aprobado. Inventario reservado para ${nueva.cliente}."))
            }.onFailure { e ->
                _ui.update { it.copy(procesando = false, error = e.message) }
                eventos.tryEmit(ToastEvent(Tipo.ERROR, "No se pudo aprobar", e.message))
            }
        }
    }

    fun rechazar() {
        val r = _ui.value.r ?: return
        _ui.update { it.copy(procesando = true, confirmarRechazo = false, error = null) }
        viewModelScope.launch {
            val res = repo.rechazar(r.id, _ui.value.motivo.ifBlank { null })
            res.onSuccess { nueva ->
                _ui.update { it.copy(procesando = false, r = nueva) }
                eventos.tryEmit(ToastEvent(Tipo.AVISO, "Solicitud rechazada", "El cliente verá el motivo en su historial."))
            }.onFailure { e -> _ui.update { it.copy(procesando = false, error = e.message) } }
        }
    }

    fun descargarNota() {
        val r = _ui.value.r ?: return
        _ui.update { it.copy(descargando = true) }
        viewModelScope.launch {
            val res = repo.descargarNota(r.id, r.folio)
            res.onSuccess { pdf.emit(it) }
            res.onFailure { eventos.tryEmit(ToastEvent(Tipo.ERROR, "No se pudo descargar la nota", it.message)) }
            _ui.update { it.copy(descargando = false) }
        }
    }
}
