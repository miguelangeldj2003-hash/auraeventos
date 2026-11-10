package com.aura.eventos.ui.reserva

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.remote.ItemRequest
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.data.remote.ReservacionRequest
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.data.repository.CartItem
import com.aura.eventos.data.repository.CartManager
import com.aura.eventos.ui.evidence.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class ReservaUi(
    val mes: YearMonth = YearMonth.now(), val ocupadas: Set<LocalDate> = emptySet(),
    val inicio: LocalDate? = null, val fin: LocalDate? = null,
    val direccion: String = "", val entorno: String = "INTERIOR", val notas: String = "",
    val latitud: Double? = null, val longitud: Double? = null,
    val ubicando: Boolean = false, val errorUbicacion: String? = null,
    val enviando: Boolean = false, val conflicto: String? = null, val error: String? = null
) {
    val finEfectivo: LocalDate? get() = fin ?: inicio
    val dias: Int get() = if (inicio == null) 0 else maxOf(1, java.time.temporal.ChronoUnit.DAYS.between(inicio, finEfectivo).toInt())
}

@HiltViewModel
class ReservaViewModel @Inject constructor(
    private val repo: AuraRepository,
    private val cart: CartManager,
    private val ubicacion: LocationProvider
) : ViewModel() {
    private val _ui = MutableStateFlow(ReservaUi())
    val ui: StateFlow<ReservaUi> = _ui.asStateFlow()
    val items: StateFlow<List<CartItem>> = cart.items

    // Al enviar bien, la pantalla navega a la confirmación con el id de la solicitud
    val creada = MutableSharedFlow<ReservacionDto>(extraBufferCapacity = 1)

    init { cargarOcupadas(YearMonth.now()) }

    private fun cargarOcupadas(mes: YearMonth) {
        viewModelScope.launch {
            val r = repo.fechasOcupadas(cart.items.value, mes)
            _ui.update { it.copy(ocupadas = r.getOrDefault(emptySet()).map { d -> LocalDate.parse(d) }.toSet()) }
        }
    }

    fun cambiarMes(delta: Int) {
        val nuevo = _ui.value.mes.plusMonths(delta.toLong())
        if (nuevo.isBefore(YearMonth.now())) return
        _ui.update { it.copy(mes = nuevo) }
        cargarOcupadas(nuevo)
    }

    /** Primer toque = entrega; segundo toque = recogida (el mismo día si es de un solo día). */
    fun tocarDia(dia: LocalDate) {
        val s = _ui.value
        if (dia.isBefore(LocalDate.now()) || dia in s.ocupadas) return
        when {
            s.inicio == null || s.fin != null -> _ui.update { it.copy(inicio = dia, fin = null, conflicto = null, error = null) }
            dia.isBefore(s.inicio) -> _ui.update { it.copy(inicio = dia, conflicto = null) }
            else -> {
                val ocupado = generateSequence(s.inicio) { d -> d.plusDays(1).takeIf { !it.isAfter(dia) } }.firstOrNull { it in s.ocupadas }
                if (ocupado != null) {
                    val txt = ocupado.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX")))
                    _ui.update { it.copy(conflicto = "El $txt ya está reservado para los artículos seleccionados. Por favor elige otra fecha.") }
                } else _ui.update { it.copy(fin = dia, conflicto = null) }
            }
        }
    }

    fun direccion(v: String) = _ui.update { it.copy(direccion = v, error = null) }
    fun entorno(v: String) = _ui.update { it.copy(entorno = v) }
    fun notas(v: String) = _ui.update { it.copy(notas = v) }

    fun ubicarLugar() {
        _ui.update { it.copy(ubicando = true, errorUbicacion = null) }
        viewModelScope.launch {
            val loc = ubicacion.current()
            _ui.update {
                if (loc == null) it.copy(ubicando = false, errorUbicacion = "No se pudo obtener el GPS. Intenta en un lugar abierto.")
                else it.copy(ubicando = false, latitud = loc.latitude, longitud = loc.longitude)
            }
        }
    }

    fun permisoDenegado() = _ui.update { it.copy(errorUbicacion = "Permiso de ubicación denegado. Actívalo en Ajustes.") }

    fun enviar() {
        val s = _ui.value
        val lista = cart.items.value
        val problema = when {
            lista.isEmpty() -> "Tu carrito está vacío"
            s.inicio == null -> "Elige la fecha de entrega en el calendario"
            s.direccion.isBlank() -> "Escribe la dirección del evento"
            else -> null
        }
        if (problema != null) { _ui.update { it.copy(error = problema) }; return }
        _ui.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            val r = repo.crearReservacion(
                ReservacionRequest(s.inicio.toString(), s.finEfectivo.toString(), s.direccion.trim(), s.entorno, s.notas.ifBlank { null },
                    s.latitud, s.longitud, lista.map { ItemRequest(it.articulo.id, it.cantidad) })
            )
            r.onSuccess { cart.vaciar(); creada.emit(it) }
            _ui.update { it.copy(enviando = false, error = r.exceptionOrNull()?.message) }
        }
    }
}

// ---------- confirmación ----------
@HiltViewModel
class ExitoViewModel @Inject constructor(savedState: SavedStateHandle, repo: AuraRepository) : ViewModel() {
    private val id: Long = savedState.get<Long>("id") ?: 0L
    val reservacion: StateFlow<ReservacionDto?> = flow { emit(repo.reservacion(id).getOrNull()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
