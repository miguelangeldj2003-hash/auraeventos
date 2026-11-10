package com.aura.eventos.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.remote.ArticuloDto
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*

// ============================================================
//  Inventario de artículos
// ============================================================
@Composable
fun InventarioScreen(onEditar: (Long) -> Unit, vm: InventarioViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    RecogerToasts(vm.eventos, host)
    AlVolver(vm::cargar)

    Scaffold(containerColor = AuraBg, snackbarHost = { ToastHost(host) },
        topBar = { AuraTopBar("Inventario de artículos", "Gestiona stock, tarifas y disponibilidad", actions = { BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onEditar(0L) }, containerColor = AuraGreen, contentColor = Color.White, shape = RoundedCornerShape(50)) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Agregar artículo", fontWeight = FontWeight.Bold)
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)) { SearchField(ui.busqueda, vm::buscar, "Buscar artículo, SKU o categoría…") }
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterPill(ui.categoriaId == null, "Todas las categorías", { vm.categoria(null) }) }
                items(ui.categorias, key = { it.id }) { c -> FilterPill(ui.categoriaId == c.id, c.nombre, { vm.categoria(c.id) }) }
            }
            LazyRow(contentPadding = PaddingValues(16.dp, 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Estadistica(ui.cuenta(EstadoArticulo.ACTIVO) + ui.cuenta(EstadoArticulo.STOCK_BAJO), "Artículos activos", AuraGreen) }
                item { Estadistica(ui.cuenta(EstadoArticulo.STOCK_BAJO), "Stock bajo", AuraAmberBright) }
                item { Estadistica(ui.cuenta(EstadoArticulo.MANTENIMIENTO), "En mantenimiento", AuraRed) }
                item { Estadistica(ui.cuenta(EstadoArticulo.DESHABILITADO), "Deshabilitados", AuraFaint) }
            }
            when {
                ui.loading -> LoadingBox()
                ui.error != null -> Box(Modifier.padding(16.dp)) { AuraBanner(Tipo.ERROR, "No se pudo cargar el inventario", ui.error, accion = "Reintentar", onAccion = vm::cargar) }
                ui.visibles.isEmpty() -> EmptyState("Sin artículos", "No hay artículos con esos filtros.", icon = Icons.Filled.Inventory2)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.visibles, key = { it.id }) { a -> FilaInventario(a, onEditar = { onEditar(a.id) }, onEstado = { vm.alternarEstado(a) }) }
                }
            }
        }
    }
}

@Composable
private fun Estadistica(n: Int, texto: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorder)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color)); Spacer(Modifier.width(8.dp))
            Text("$n", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.width(6.dp))
            Text(texto, style = MaterialTheme.typography.bodySmall, color = AuraMuted)
        }
    }
}

@Composable
private fun FilaInventario(a: ArticuloDto, onEditar: () -> Unit, onEstado: () -> Unit) {
    val estado = a.estadoInventario()
    val (fg, bg) = when (estado) {
        EstadoArticulo.ACTIVO -> AuraGreenDark to AuraGreenSoft
        EstadoArticulo.STOCK_BAJO -> AuraAmber to AuraAmberSoft
        EstadoArticulo.MANTENIMIENTO -> AuraRed to AuraRedSoft
        EstadoArticulo.DESHABILITADO -> AuraMuted to AuraBg
    }
    val libres = a.disponibles ?: a.stockTotal
    AuraCard(onClick = onEditar, borderColor = if (estado == EstadoArticulo.STOCK_BAJO) AuraGold.copy(alpha = 0.6f) else AuraBorder) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArticuloImagen(a.sku, a.imagenUrl, a.nombre, a.seccion == "MESA_DULCES", Modifier.size(62.dp).clip(RoundedCornerShape(12.dp)), mostrarNombre = false)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(a.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                    Text("SKU: ${a.sku ?: "—"}", style = MaterialTheme.typography.bodySmall, color = AuraFaint)
                    Text(a.categoria, style = MaterialTheme.typography.labelMedium, color = if (a.seccion == "MESA_DULCES") AuraGold else AuraGreen)
                }
                SoftPill(estado.texto, fg, bg, dot = true)
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AuraBg).padding(vertical = 10.dp)) {
                Dato("STOCK TOTAL", "${a.stockTotal}", Modifier.weight(1f))
                Dato("DISPONIBLES", "$libres", Modifier.weight(1f), AuraGreenDark)
                Dato("RENTADOS HOY", "${(a.stockTotal - libres).coerceAtLeast(0)}", Modifier.weight(1f))
                Dato("TARIFA / EVENTO", a.precioBase.mxnSinCentavos(), Modifier.weight(1.2f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccionChica("Editar", Icons.Filled.Edit, AuraGreenDark, AuraGreenSoft, Modifier.weight(1f), onEditar)
                AccionChica(if (a.activo) "Deshabilitar" else "Habilitar", if (a.activo) Icons.Filled.Block else Icons.Filled.CheckCircle,
                    AuraAmber, AuraAmberSoft, Modifier.weight(1f), onEstado)
            }
        }
    }
}

@Composable
private fun Dato(titulo: String, valor: String, modifier: Modifier, color: Color = AuraNavy) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(titulo, style = MaterialTheme.typography.labelSmall, color = AuraFaint, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun AccionChica(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, fg: Color, bg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = bg, modifier = modifier) {
        Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = fg, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
            Text(texto, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
//  Alta y edición de artículo
// ============================================================
@Composable
fun ArticuloFormScreen(onBack: () -> Unit, vm: ArticuloFormViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(ui.guardado) { if (ui.guardado) onBack() }

    Scaffold(containerColor = AuraBg, topBar = { AuraTopBar(if (vm.esNuevo) "Nuevo artículo" else "Editar artículo", "Inventario", onBack = onBack) }) { pad ->
        if (ui.cargando) { Box(Modifier.padding(pad)) { LoadingBox() }; return@Scaffold }
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Datos generales")
                    LabeledField("Nombre del artículo", ui.nombre, { v -> vm.cambiar { it.copy(nombre = v) } }, placeholder = "Mesa Candy Premium Dorada")
                    LabeledField("SKU", ui.sku, { v -> vm.cambiar { it.copy(sku = v) } }, placeholder = "MC-001-ORO")
                    Column {
                        Text("Categoría", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
                        listOf("MOBILIARIO" to "Mobiliario y decoración", "MESA_DULCES" to "Mesas de dulces").forEach { (sec, titulo) ->
                            Text(titulo, style = MaterialTheme.typography.bodySmall, color = AuraMuted, modifier = Modifier.padding(vertical = 4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(ui.categorias.filter { it.seccion == sec }, key = { it.id }) { c ->
                                    FilterPill(ui.categoriaId == c.id, c.nombre, { vm.cambiar { it.copy(categoriaId = c.id) } }, if (sec == "MESA_DULCES") AuraGold else AuraGreen)
                                }
                            }
                        }
                    }
                    LabeledField("Descripción", ui.descripcion, { v -> vm.cambiar { it.copy(descripcion = v) } }, singleLine = false, minLines = 3, placeholder = "Describe el artículo…")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LabeledField("Tarifa por evento (MXN)", ui.precio, { v -> vm.cambiar { it.copy(precio = v.filter { c -> c.isDigit() || c == '.' }) } }, Modifier.weight(1f), "1200", KeyboardType.Decimal)
                        LabeledField("Stock total", ui.stock, { v -> vm.cambiar { it.copy(stock = v.filter { c -> c.isDigit() }) } }, Modifier.weight(1f), "10", KeyboardType.Number)
                    }
                    LabeledField("URL de la foto (opcional)", ui.imagen, { v -> vm.cambiar { it.copy(imagen = v) } }, placeholder = "https://…")
                }
            }
            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Especificaciones")
                    LabeledField("Dimensiones", ui.dimensiones, { v -> vm.cambiar { it.copy(dimensiones = v) } }, placeholder = "1.8m × 0.6m × 0.9m")
                    LabeledField("Material", ui.material, { v -> vm.cambiar { it.copy(material = v) } }, placeholder = "MDF lacado blanco")
                    LabeledField("Incluye", ui.incluye, { v -> vm.cambiar { it.copy(incluye = v) } }, placeholder = "Montaje + desmontaje")
                    LabeledField("Peso máximo", ui.peso, { v -> vm.cambiar { it.copy(peso = v) } }, placeholder = "80 kg")
                }
            }
            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilaInterruptor("En mantenimiento", "No se podrá rentar hasta que lo desactives.", ui.mantenimiento) { v -> vm.cambiar { it.copy(mantenimiento = v) } }
                    if (!vm.esNuevo) FilaInterruptor("Visible en el catálogo", "Si lo apagas se deshabilita, sin borrar el historial de rentas.", ui.activo) { v -> vm.cambiar { it.copy(activo = v) } }
                }
            }
            ui.error?.let { AuraBanner(Tipo.ERROR, it) }
            PrimaryButton("Guardar artículo", vm::guardar, loading = ui.guardando, icon = Icons.Filled.Check)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FilaInterruptor(titulo: String, detalle: String, valor: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!valor) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = AuraMuted)
        }
        Switch(valor, onChange, colors = SwitchDefaults.colors(checkedTrackColor = AuraGreen, checkedThumbColor = Color.White))
    }
}
