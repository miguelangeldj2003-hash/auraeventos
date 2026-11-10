package com.aura.eventos.ui.catalogo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.remote.ArticuloDto
import com.aura.eventos.data.remote.CategoriaDto
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.data.repository.CartItem
import com.aura.eventos.data.repository.CartManager
import com.aura.eventos.ui.components.Tipo
import com.aura.eventos.ui.components.ToastEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

fun ArticuloDto.estadoStock(): Triple<String, Boolean, Boolean> = when {   // texto, esAlerta, esError
    stockTotal <= 0 -> Triple("No disponible", false, true)
    stockTotal <= 5 -> Triple("Stock bajo", true, false)
    else -> Triple("Disponible", false, false)
}

data class CatalogoUi(
    val seccion: String = "MOBILIARIO", val categoriaId: Long? = null, val soloDisponible: Boolean = false,
    val busqueda: String = "", val categorias: List<CategoriaDto> = emptyList(), val articulos: List<ArticuloDto> = emptyList(),
    val loading: Boolean = true, val error: String? = null
) {
    val categoriasDeSeccion get() = categorias.filter { it.seccion == seccion }
    val deSeccion get() = articulos.filter { it.seccion == seccion }
    val visibles get() = deSeccion.filter {
        (categoriaId == null || it.categoriaId == categoriaId) && (!soloDisponible || it.stockTotal > 0) &&
            (busqueda.isBlank() || it.nombre.contains(busqueda, true) || it.categoria.contains(busqueda, true))
    }
    /** El artículo con más rentas aprobadas lleva la etiqueta "Más rentado". */
    val masRentadoId: Long? get() = articulos.filter { it.rentas > 0 }.maxByOrNull { it.rentas }?.id
}

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val repo: AuraRepository,
    private val cart: CartManager
) : ViewModel() {
    private val _ui = MutableStateFlow(CatalogoUi())
    val ui: StateFlow<CatalogoUi> = _ui.asStateFlow()
    val carrito: StateFlow<List<CartItem>> = cart.items
    val eventos = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 4)

    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val cats = repo.categorias()
            val arts = repo.articulos()
            _ui.update {
                it.copy(loading = false, categorias = cats.getOrDefault(emptyList()), articulos = arts.getOrDefault(emptyList()),
                    error = (arts.exceptionOrNull() ?: cats.exceptionOrNull())?.message)
            }
        }
    }

    fun seccion(s: String) = _ui.update { it.copy(seccion = s, categoriaId = null) }
    fun categoria(id: Long?) = _ui.update { it.copy(categoriaId = id) }
    fun soloDisponible() = _ui.update { it.copy(soloDisponible = !it.soloDisponible) }
    fun buscar(t: String) = _ui.update { it.copy(busqueda = t) }

    fun agregar(a: ArticuloDto, cantidad: Int = 1) {
        val error = cart.agregar(a, cantidad)
        eventos.tryEmit(
            if (error != null) ToastEvent(Tipo.AVISO, "No se pudo agregar", error)
            else ToastEvent(Tipo.EXITO, "¡Éxito! Artículo agregado correctamente.", "${a.nombre} fue añadido a tu carrito.")
        )
    }
}

// ---------- detalle de un artículo ----------
@HiltViewModel
class ArticuloDetalleViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: AuraRepository,
    private val cart: CartManager,
    private val session: SessionManager
) : ViewModel() {
    private val id: Long = savedState.get<Long>("id") ?: 0L
    private val _articulo = MutableStateFlow(repo.articuloEnCache(id))
    val articulo: StateFlow<ArticuloDto?> = _articulo.asStateFlow()
    val cantidad = MutableStateFlow(1)
    val carrito: StateFlow<List<CartItem>> = cart.items
    val esFavorito: StateFlow<Boolean> = session.favoritos.map { id in it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), id in session.favoritos.value)
    val eventos = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 4)

    init {
        if (_articulo.value == null) viewModelScope.launch { _articulo.value = repo.articulo(id) }
    }

    fun cambiarCantidad(c: Int) { cantidad.value = c.coerceIn(1, maxOf(1, _articulo.value?.stockTotal ?: 1)) }

    fun agregar() {
        val a = _articulo.value ?: return
        val error = cart.agregar(a, cantidad.value)
        eventos.tryEmit(
            if (error != null) ToastEvent(Tipo.AVISO, "No se pudo agregar", error)
            else ToastEvent(Tipo.EXITO, "¡Éxito! Artículo agregado correctamente.", "${a.nombre} fue añadido a tu carrito.")
        )
    }

    fun alternarFavorito() {
        val a = _articulo.value ?: return
        val ahora = session.alternarFavorito(a.id)
        eventos.tryEmit(ToastEvent(Tipo.EXITO, if (ahora) "Guardado en favoritos" else "Quitado de favoritos", a.nombre))
    }
}

// ---------- carrito ----------
@HiltViewModel
class CarritoViewModel @Inject constructor(private val cart: CartManager) : ViewModel() {
    val items: StateFlow<List<CartItem>> = cart.items
    fun cambiarCantidad(id: Long, c: Int) = cart.cambiarCantidad(id, c)
    fun quitar(id: Long) = cart.quitar(id)
}
