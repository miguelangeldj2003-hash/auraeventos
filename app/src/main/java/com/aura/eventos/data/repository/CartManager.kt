package com.aura.eventos.data.repository

import com.aura.eventos.data.remote.ArticuloDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class CartItem(val articulo: ArticuloDto, val cantidad: Int) {
    val subtotal: Double get() = articulo.precioBase * cantidad
}

object Montos {
    // IVA del 16% solo sobre la renta; el flete se asigna al aprobar y no lleva IVA
    fun iva(subtotal: Double) = Math.round(subtotal * 0.16 * 100.0) / 100.0
}

/** Carrito compartido entre catálogo, calendario y solicitudes. */
@Singleton
class CartManager @Inject constructor() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    /** @return mensaje de error si no se pudo agregar, o null si todo bien. */
    fun agregar(a: ArticuloDto, cantidad: Int = 1): String? {
        val actual = _items.value.firstOrNull { it.articulo.id == a.id }?.cantidad ?: 0
        if (actual + cantidad > a.stockTotal) {
            return "No hay suficientes piezas de ${a.nombre} (máximo ${a.stockTotal})"
        }
        _items.update { lista ->
            if (actual == 0) lista + CartItem(a, cantidad)
            else lista.map { if (it.articulo.id == a.id) it.copy(cantidad = actual + cantidad) else it }
        }
        return null
    }

    fun cambiarCantidad(id: Long, cantidad: Int) = _items.update { lista ->
        if (cantidad <= 0) lista.filter { it.articulo.id != id }
        else lista.map { if (it.articulo.id == id) it.copy(cantidad = minOf(cantidad, it.articulo.stockTotal)) else it }
    }

    fun quitar(id: Long) = _items.update { l -> l.filter { it.articulo.id != id } }
    fun vaciar() { _items.value = emptyList() }
}
