package com.aura.eventos.ui.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.remote.ArticuloDto
import com.aura.eventos.data.repository.CartItem
import com.aura.eventos.data.repository.Montos
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*

// ============================================================
//  Catálogo
// ============================================================
@Composable
fun CatalogoScreen(onArticulo: (Long) -> Unit, onCarrito: () -> Unit, vm: CatalogoViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val carrito by vm.carrito.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    RecogerToasts(vm.eventos, host)
    val dulces = ui.seccion == "MESA_DULCES"
    val acento = if (dulces) AuraGold else AuraGreen

    Scaffold(containerColor = AuraBg, snackbarHost = { ToastHost(host) }, topBar = {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                AuraLogo(Modifier.weight(1f))
                BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar)
                BarIcon(Icons.Filled.ShoppingCart, "Carrito", onCarrito, badge = carrito.sumOf { it.cantidad })
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchField(ui.busqueda, vm::buscar, "Buscar artículos…")
                SegmentedSwitch(
                    listOf("Mobiliario y Decoración" to Icons.Filled.Chair, "Mesas de Dulces y Candy Bar" to Icons.Filled.Cake),
                    if (dulces) 1 else 0
                ) { vm.seccion(if (it == 1) "MESA_DULCES" else "MOBILIARIO") }
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("FILTRAR:", Modifier.padding(end = 4.dp, top = 10.dp), style = MaterialTheme.typography.labelSmall, color = AuraMuted) }
                item { FilterPill(ui.categoriaId == null, "Todos", { vm.categoria(null) }, acento) }
                items(ui.categoriasDeSeccion, key = { it.id }) { c -> FilterPill(ui.categoriaId == c.id, c.nombre, { vm.categoria(c.id) }, acento) }
                item { FilterPill(ui.soloDisponible, "Disponible", vm::soloDisponible, acento, icon = if (ui.soloDisponible) Icons.Filled.Check else null) }
            }
            Row(Modifier.padding(16.dp, 14.dp, 16.dp, 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(22.dp).background(acento))
                Spacer(Modifier.width(10.dp))
                Text(if (dulces) "Mesas de Dulces y Candy Bar" else "Mobiliario y Decoración", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                SoftPill("${ui.visibles.size} artículos", AuraAmber, AuraGoldSoft)
            }
            when {
                ui.loading -> LoadingBox()
                ui.error != null -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuraBanner(Tipo.ERROR, "No se pudo cargar el catálogo", ui.error)
                    PrimaryButton("Reintentar", vm::cargar, icon = Icons.Filled.Refresh)
                }
                ui.visibles.isEmpty() -> EmptyState("Sin resultados", "No encontramos artículos con esos filtros.", icon = Icons.Filled.SearchOff)
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ui.visibles, key = { it.id }) { a ->
                        ArticuloCard(a, acento, enCarrito = carrito.any { it.articulo.id == a.id }, masRentado = a.id == ui.masRentadoId,
                            onClick = { onArticulo(a.id) }, onAgregar = { vm.agregar(a) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticuloCard(a: ArticuloDto, acento: Color, enCarrito: Boolean, masRentado: Boolean, onClick: () -> Unit, onAgregar: () -> Unit) {
    val (textoStock, alerta, error) = a.estadoStock()
    val borde = when { enCarrito -> AuraGreen; error -> AuraRed; else -> AuraBorder }
    AuraCard(onClick = onClick, borderColor = borde, color = if (error) AuraRedSoft.copy(alpha = 0.4f) else Color.White) {
        Box {
            ArticuloImagen(a.sku, a.imagenUrl, a.nombre, a.seccion == "MESA_DULCES", Modifier.fillMaxWidth().height(118.dp))
            Row(Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SoftPill(textoStock, when { error -> AuraRed; alerta -> AuraAmber; else -> AuraGreenDark }, Color.White.copy(alpha = 0.92f), dot = true)
                when {
                    enCarrito -> SoftPill("En carrito", Color.White, AuraGreen, icon = Icons.Filled.Check)
                    masRentado -> SoftPill("Más rentado", Color.White, AuraNavy, icon = Icons.Filled.Star)
                }
            }
        }
        AcentoLinea(if (error) AuraRed else acento)
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(a.categoria.uppercase(), style = MaterialTheme.typography.labelSmall, color = acento, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(a.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
            Text(a.descripcion ?: "", style = MaterialTheme.typography.bodySmall, color = AuraMuted, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
            Spacer(Modifier.height(2.dp))
            SmallChip(if (a.stockTotal == 1) "1 unidad" else "${a.stockTotal} unidades", Icons.Filled.Inventory2,
                fg = if (alerta) AuraAmber else AuraMuted, bg = if (alerta) AuraAmberSoft else AuraBg)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(a.precioBase.mxnSinCentavos(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(" /evento", style = MaterialTheme.typography.bodySmall, color = AuraMuted, modifier = Modifier.padding(bottom = 2.dp))
            }
            Surface(
                onClick = onAgregar, enabled = !error, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = if (enCarrito) Color.White else if (error) AuraBorder else acento,
                border = if (enCarrito) androidx.compose.foundation.BorderStroke(1.dp, AuraGreen) else null
            ) {
                Row(Modifier.padding(vertical = 9.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (enCarrito) Icons.Filled.Check else Icons.Filled.Add, null, Modifier.size(16.dp), tint = if (enCarrito) AuraGreen else Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text(if (error) "No disponible" else if (enCarrito) "Agregado" else "Agregar", style = MaterialTheme.typography.labelMedium,
                        color = if (enCarrito) AuraGreen else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================
//  Detalle de artículo
// ============================================================
@Composable
fun ArticuloDetalleScreen(onBack: () -> Unit, onCarrito: () -> Unit, vm: ArticuloDetalleViewModel = hiltViewModel()) {
    val a by vm.articulo.collectAsStateWithLifecycle()
    val cantidad by vm.cantidad.collectAsStateWithLifecycle()
    val fav by vm.esFavorito.collectAsStateWithLifecycle()
    val carrito by vm.carrito.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    RecogerToasts(vm.eventos, host)

    Scaffold(containerColor = Color.White, snackbarHost = { ToastHost(host) }) { pad ->
        val art = a
        if (art == null) { Box(Modifier.padding(pad)) { LoadingBox() }; return@Scaffold }
        val dulces = art.seccion == "MESA_DULCES"
        val acento = if (dulces) AuraGold else AuraGreen
        val (textoStock, alerta, error) = art.estadoStock()
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())) {
            Box {
                ArticuloImagen(art.sku, art.imagenUrl, art.nombre, dulces, Modifier.fillMaxWidth().height(300.dp))
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircleBtn(Icons.Filled.ArrowBack, "Regresar", onBack)
                    Spacer(Modifier.weight(1f))
                    CircleBtn(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorito", vm::alternarFavorito, tint = if (fav) AuraRed else AuraNavy)
                    Spacer(Modifier.width(8.dp))
                    BadgedBox(badge = { if (carrito.isNotEmpty()) Badge(containerColor = AuraGreen) { Text("${carrito.sumOf { it.cantidad }}") } }) {
                        CircleBtn(Icons.Filled.ShoppingCart, "Carrito", onCarrito)
                    }
                }
            }
            Column(
                Modifier.offset(y = (-22).dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color.White).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionLabel(art.categoria, color = acento)
                Text(art.nombre, style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SoftPill("${art.rentas} rentas", AuraAmber, AuraGoldSoft, icon = Icons.Filled.Star)
                    art.sku?.let { SmallChip(it) }
                }
                Surface(shape = RoundedCornerShape(10.dp), color = if (error) AuraRedSoft else if (alerta) AuraAmberSoft else AuraGreenSoft) {
                    Text("● " + if (error) "Sin unidades disponibles" else "${art.stockTotal} unidades en inventario" + if (alerta) " · $textoStock" else "",
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium,
                        color = if (error) AuraRed else if (alerta) AuraAmber else AuraGreenDark)
                }
                art.descripcion?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = AuraMuted) }
                val specs = listOf("Dimensiones" to art.dimensiones, "Material" to art.material, "Incluye" to art.incluye, "Peso máx." to art.pesoMax).filter { !it.second.isNullOrBlank() }
                if (specs.isNotEmpty()) {
                    specs.chunked(2).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            fila.forEach { (t, v) ->
                                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(AuraBg).padding(12.dp)) {
                                    Text(t.uppercase(), style = MaterialTheme.typography.labelSmall, color = AuraFaint)
                                    Text(v ?: "", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (fila.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                HorizontalDivider(color = AuraBorder)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(art.precioBase.mxnSinCentavos(), style = MaterialTheme.typography.headlineMedium)
                    Text("  MXN / evento", style = MaterialTheme.typography.bodySmall, color = AuraMuted, modifier = Modifier.padding(bottom = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 12.dp))
                    QtyStepper(cantidad, art.stockTotal, vm::cambiarCantidad)
                    Text("  = ${(art.precioBase * cantidad).mxnSinCentavos()} MXN", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                }
                PrimaryButton("Agregar al carrito", vm::agregar, enabled = !error, icon = Icons.Filled.AddShoppingCart, color = acento)
                OutlineButton(if (fav) "Quitar de favoritos" else "Guardar en favoritos", vm::alternarFavorito,
                    icon = if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, color = if (fav) AuraRed else AuraNavy)
            }
        }
    }
}

@Composable
private fun CircleBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit, tint: Color = AuraNavy) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.95f))) {
        Icon(icon, desc, tint = tint)
    }
}

// ============================================================
//  Carrito
// ============================================================
@Composable
fun CarritoScreen(onBack: () -> Unit, onContinuar: () -> Unit, onCatalogo: () -> Unit, vm: CarritoViewModel = hiltViewModel()) {
    val items by vm.items.collectAsStateWithLifecycle()
    Scaffold(containerColor = AuraBg, topBar = { AuraTopBar("Mi carrito", "Catálogo › Carrito", onBack = onBack) }) { pad ->
        if (items.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("Tu carrito está vacío", "Agrega artículos del catálogo para armar tu solicitud.", icon = Icons.Filled.ShoppingCart,
                    accion = "Explorar catálogo", onAccion = onCatalogo)
            }
        } else {
            Column(Modifier.padding(pad).fillMaxSize()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items, key = { it.articulo.id }) { c -> LineaCarrito(c, vm) }
                }
                ResumenCarrito(items, onContinuar)
            }
        }
    }
}

@Composable
private fun LineaCarrito(c: CartItem, vm: CarritoViewModel) {
    val a = c.articulo
    AuraCard {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ArticuloImagen(a.sku, a.imagenUrl, a.nombre, a.seccion == "MESA_DULCES", Modifier.size(78.dp).clip(RoundedCornerShape(12.dp)), mostrarNombre = false)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(a.categoria.uppercase(), style = MaterialTheme.typography.labelSmall, color = if (a.seccion == "MESA_DULCES") AuraGold else AuraGreen, maxLines = 1)
                Text(a.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${a.precioBase.mxnSinCentavos()} /evento", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QtyStepper(c.cantidad, a.stockTotal) { vm.cambiarCantidad(a.id, it) }
                    Spacer(Modifier.weight(1f))
                    Text(c.subtotal.mxnSinCentavos(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                }
            }
            IconButton(onClick = { vm.quitar(a.id) }) { Icon(Icons.Filled.DeleteOutline, "Quitar", tint = AuraRed) }
        }
    }
}

@Composable
private fun ResumenCarrito(items: List<CartItem>, onContinuar: () -> Unit) {
    val sub = items.sumOf { it.subtotal }
    val iva = Montos.iva(sub)
    Surface(color = AuraNavy, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Resumen de la solicitud", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(6.dp))
            InfoRow("Subtotal", "${sub.mxn()} MXN", Color.White, bold = true, onDark = true)
            InfoRow("IVA (16%)", "${iva.mxn()} MXN", Color.White, bold = true, onDark = true)
            InfoRow("Flete", "Por asignar", AuraGold, bold = true, onDark = true)
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total estimado", Modifier.weight(1f), color = Color(0xFFB8C4D6), style = MaterialTheme.typography.bodyMedium)
                Text((sub + iva).mxn(), style = MaterialTheme.typography.headlineSmall, color = AuraGreen)
            }
            Text("El flete lo confirma la administradora al aprobar.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA0B8))
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Continuar a reservación", onContinuar, icon = Icons.Filled.ArrowForward)
        }
    }
}
