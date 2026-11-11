package com.aura.eventos.ui.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.remote.ArticuloDto
import com.aura.eventos.data.remote.ArticuloRequest
import com.aura.eventos.data.remote.CategoriaDto
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.ui.components.Tipo
import com.aura.eventos.ui.components.ToastEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class EstadoArticulo(val texto: String) { ACTIVO("Activo"), STOCK_BAJO("Stock bajo"), MANTENIMIENTO("Mantenimiento"), DESHABILITADO("Deshabilitado") }

fun ArticuloDto.estadoInventario() = when {
    !activo -> EstadoArticulo.DESHABILITADO
    enMantenimiento -> EstadoArticulo.MANTENIMIENTO
    stockTotal <= 5 -> EstadoArticulo.STOCK_BAJO
    else -> EstadoArticulo.ACTIVO
}

data class InventarioUi(
    val articulos: List<ArticuloDto> = emptyList(), val categorias: List<CategoriaDto> = emptyList(),
    val busqueda: String = "", val categoriaId: Long? = null, val loading: Boolean = true, val error: String? = null
) {
    val visibles get() = articulos.filter {
        (categoriaId == null || it.categoriaId == categoriaId) &&
            (busqueda.isBlank() || it.nombre.contains(busqueda, true) || (it.sku ?: "").contains(busqueda, true) || it.categoria.contains(busqueda, true))
    }
    fun cuenta(e: EstadoArticulo) = articulos.count { it.estadoInventario() == e }
}

@HiltViewModel
class InventarioViewModel @Inject constructor(private val repo: AuraRepository) : ViewModel() {
    private val _ui = MutableStateFlow(InventarioUi())
    val ui: StateFlow<InventarioUi> = _ui.asStateFlow()
    val eventos = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 4)
    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val a = repo.adminArticulos(); val c = repo.categorias()
            _ui.update { it.copy(loading = false, articulos = a.getOrDefault(emptyList()), categorias = c.getOrDefault(emptyList()), error = (a.exceptionOrNull() ?: c.exceptionOrNull())?.message) }
        }
    }
    fun buscar(t: String) = _ui.update { it.copy(busqueda = t) }
    fun categoria(id: Long?) = _ui.update { it.copy(categoriaId = id) }

    fun alternarEstado(a: ArticuloDto) {
        viewModelScope.launch {
            val r = repo.cambiarEstado(a.id, !a.activo)
            if (r.isSuccess) {
                eventos.tryEmit(ToastEvent(Tipo.EXITO, if (a.activo) "Artículo deshabilitado" else "Artículo habilitado", a.nombre))
                cargar()
            } else eventos.tryEmit(ToastEvent(Tipo.ERROR, "No se pudo cambiar el estado", r.exceptionOrNull()?.message))
        }
    }
}

// ---------- alta y edición ----------
data class FormUi(
    val nombre: String = "", val sku: String = "", val categoriaId: Long? = null, val descripcion: String = "",
    val precio: String = "", val stock: String = "", val photoPath: String? = null, val currentImagenUrl: String? = null,
    val photoRemoved: Boolean = false, val dimensiones: String = "", val material: String = "", val incluye: String = "",
    val peso: String = "", val mantenimiento: Boolean = false, val activo: Boolean = true,
    val categorias: List<CategoriaDto> = emptyList(), val cargando: Boolean = true, val guardando: Boolean = false,
    val guardandoFoto: Boolean = false, val error: String? = null, val photoError: String? = null,
    val guardado: Boolean = false, val articuloIdGuardado: Long? = null
)

@HiltViewModel
class ArticuloFormViewModel @Inject constructor(savedState: SavedStateHandle, private val repo: AuraRepository) : ViewModel() {
    val id: Long = savedState.get<Long>("id") ?: 0L
    val esNuevo get() = id == 0L
    private var original: ArticuloDto? = null
    private val _ui = MutableStateFlow(FormUi())
    val ui: StateFlow<FormUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = repo.categorias().getOrDefault(emptyList())
            val a = if (esNuevo) null else repo.articulo(id) ?: repo.adminArticulos().getOrNull()?.firstOrNull { it.id == id }
            original = a
            _ui.update {
                if (a == null) it.copy(categorias = cats, categoriaId = cats.firstOrNull()?.id, cargando = false)
                else it.copy(categorias = cats, cargando = false, nombre = a.nombre, sku = a.sku ?: "", categoriaId = a.categoriaId,
                    descripcion = a.descripcion ?: "", precio = a.precioBase.toString().removeSuffix(".0"), stock = a.stockTotal.toString(),
                    currentImagenUrl = a.imagenUrl ?: "", dimensiones = a.dimensiones ?: "", material = a.material ?: "", incluye = a.incluye ?: "",
                    peso = a.pesoMax ?: "", mantenimiento = a.enMantenimiento, activo = a.activo)
            }
        }
    }

    fun cambiar(f: (FormUi) -> FormUi) = _ui.update { f(it).copy(error = null) }

    fun onFotoElegida(path: String) {
        _ui.update { it.copy(photoPath = path, photoRemoved = false, photoError = null) }
    }

    fun onQuitarFoto() {
        _ui.update { it.copy(photoPath = null, currentImagenUrl = null, photoRemoved = true, photoError = null) }
    }

    fun guardar() {
        val s = _ui.value
        val precio = s.precio.toDoubleOrNull()
        val stock = s.stock.toIntOrNull()
        val problema = when {
            s.nombre.isBlank() -> "Escribe el nombre del artículo"
            s.categoriaId == null -> "Elige una categoría"
            precio == null || precio < 0 -> "Escribe una tarifa válida"
            stock == null || stock < 0 -> "Escribe el stock total"
            else -> null
        }
        if (problema != null) { _ui.update { it.copy(error = problema) }; return }

        _ui.update { it.copy(guardando = true, error = null, photoError = null) }
        viewModelScope.launch {
            val targetId = s.articuloIdGuardado ?: if (esNuevo) 0L else id
            val imagenUrlReq = if (s.photoRemoved) null else s.currentImagenUrl
            val req = ArticuloRequest(
                s.categoriaId!!, s.nombre.trim(), s.descripcion.ifBlank { null }, precio!!, stock!!,
                imagenUrlReq, s.sku.ifBlank { null }, s.dimensiones.ifBlank { null },
                s.material.ifBlank { null }, s.incluye.ifBlank { null }, s.peso.ifBlank { null }, s.mantenimiento
            )
            val r = if (targetId == 0L) repo.crearArticulo(req) else repo.actualizarArticulo(targetId, req)
            val artGuardado = r.getOrNull()
            if (artGuardado == null) {
                _ui.update { it.copy(guardando = false, error = r.exceptionOrNull()?.message ?: "No se pudo guardar el artículo") }
                return@launch
            }
            val savedId = artGuardado.id
            if (!esNuevo && original != null && original!!.activo != s.activo) {
                repo.cambiarEstado(savedId, s.activo)
            }
            if (s.photoPath != null) {
                _ui.update { it.copy(articuloIdGuardado = savedId, guardando = false, guardandoFoto = true) }
                val fileFoto = File(s.photoPath)
                val rFoto = repo.subirImagenArticulo(savedId, fileFoto)
                if (rFoto.isSuccess) {
                    _ui.update { it.copy(guardandoFoto = false, guardado = true) }
                } else {
                    _ui.update {
                        it.copy(
                            guardandoFoto = false,
                            photoError = "Artículo guardado, pero ocurrió un error al subir la foto: ${rFoto.exceptionOrNull()?.message}. Puedes reintentar subir la foto."
                        )
                    }
                }
            } else {
                _ui.update { it.copy(guardando = false, guardado = true) }
            }
        }
    }

    fun reintentarFoto() {
        val s = _ui.value
        val savedId = s.articuloIdGuardado ?: id
        val path = s.photoPath ?: return
        if (savedId == 0L) return
        _ui.update { it.copy(guardandoFoto = true, photoError = null) }
        viewModelScope.launch {
            val rFoto = repo.subirImagenArticulo(savedId, File(path))
            if (rFoto.isSuccess) {
                _ui.update { it.copy(guardandoFoto = false, guardado = true) }
            } else {
                _ui.update {
                    it.copy(
                        guardandoFoto = false,
                        photoError = "No se pudo subir la foto: ${rFoto.exceptionOrNull()?.message}. Intenta de nuevo."
                    )
                }
            }
        }
    }
}
