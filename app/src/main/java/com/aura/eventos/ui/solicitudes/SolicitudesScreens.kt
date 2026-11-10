package com.aura.eventos.ui.solicitudes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*

private fun ReservacionDto.resumenArticulos(): String =
    detalles.joinToString(", ") { it.nombre.take(24) + if (it.cantidad > 1) " x${it.cantidad}" else "" }

// ============================================================
//  Tarjeta de solicitud (la usan "Mis solicitudes" y "Agenda")
// ============================================================
@Composable
fun SolicitudCard(r: ReservacionDto, mostrarCliente: Boolean, onClick: () -> Unit) {
    AuraCard(onClick = onClick, borderColor = if (mostrarCliente && r.estado == "PENDIENTE") AuraGold.copy(alpha = 0.6f) else AuraBorder) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.folio.folioVisible(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusPill(r.estado)
            }
            if (mostrarCliente) Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(r.cliente, 36.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(r.cliente, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(r.correo ?: "", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                }
            }
            Text(r.resumenArticulos(), style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, null, tint = AuraMuted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                Text(rangoFechas(r.fechaEvento, r.fechaFin), style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                if (mostrarCliente) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.Place, null, tint = AuraMuted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                    Text(r.direccion, style = MaterialTheme.typography.bodySmall, color = AuraMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider(color = AuraBorder)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = AuraFaint)
                    Text(r.total.mxnSinCentavos(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                when {
                    r.estado == "RECHAZADO" -> SmallChip("Sin flete")
                    r.estado == "PENDIENTE" -> SoftPill("Flete: por asignar", AuraAmber, AuraGoldSoft)
                    else -> SoftPill("Flete: ${r.costoFlete.mxnSinCentavos()}", AuraGreenDark, AuraGreenSoft)
                }
                Spacer(Modifier.width(10.dp))
                Text("Ver detalle ›", color = AuraGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FiltrosEstado(ui: ListaUi, onFiltro: (String?) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf<Pair<String?, String>>(null to "Todas", "PENDIENTE" to "Pendientes", "APROBADO" to "Aprobadas", "RECHAZADO" to "Rechazadas")) { (valor, texto) ->
            FilterPill(ui.filtro == valor, "$texto (${ui.cuenta(valor)})", { onFiltro(valor) })
        }
    }
}

// ============================================================
//  Mis solicitudes (cliente)
// ============================================================
@Composable
fun SolicitudesScreen(onDetalle: (Long) -> Unit, onCatalogo: () -> Unit, vm: SolicitudesViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(containerColor = AuraBg, topBar = {
        AuraTopBar("Mis solicitudes", "${ui.lista.size} solicitudes", actions = { BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar) })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)) { SearchField(ui.busqueda, vm::buscar, "Buscar solicitud…") }
            FiltrosEstado(ui, vm::filtro)
            Spacer(Modifier.height(8.dp))
            when {
                ui.loading -> LoadingBox()
                ui.error != null -> Box(Modifier.padding(16.dp)) { AuraBanner(Tipo.ERROR, "No se pudieron cargar tus solicitudes", ui.error, accion = "Reintentar", onAccion = vm::cargar) }
                ui.visibles.isEmpty() -> EmptyState("Sin solicitudes", if (ui.lista.isEmpty()) "Aún no has enviado solicitudes. Arma tu carrito en el catálogo." else "No hay solicitudes con ese filtro.",
                    icon = Icons.Filled.ReceiptLong, accion = if (ui.lista.isEmpty()) "Explorar catálogo" else null, onAccion = if (ui.lista.isEmpty()) onCatalogo else null)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.visibles, key = { it.id }) { r -> SolicitudCard(r, mostrarCliente = false) { onDetalle(r.id) } }
                }
            }
        }
    }
}

// ============================================================
//  Agenda de solicitudes (administradora)
// ============================================================
@Composable
fun AgendaScreen(onDetalle: (Long) -> Unit, vm: SolicitudesViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val pendientes = ui.cuenta("PENDIENTE")
    Scaffold(containerColor = AuraBg, topBar = {
        AuraTopBar("Agenda de solicitudes", "Gestiona y aprueba los pedidos entrantes", actions = { BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar) })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Column(Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (pendientes > 0) AuraBanner(Tipo.AVISO, "$pendientes sin revisar", "Asigna el flete y toma una decisión para cada solicitud pendiente.",
                    accion = "Ver pendientes", onAccion = { vm.filtro("PENDIENTE") })
                SearchField(ui.busqueda, vm::buscar, "Buscar cliente, artículo o ID…")
            }
            FiltrosEstado(ui, vm::filtro)
            Row(Modifier.padding(16.dp, 10.dp).clickable(onClick = vm::orden), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SwapVert, null, tint = AuraMuted, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text(if (ui.recientesPrimero) "Más recientes primero" else "Por fecha de evento", style = MaterialTheme.typography.labelMedium, color = AuraMuted)
            }
            when {
                ui.loading -> LoadingBox()
                ui.error != null -> Box(Modifier.padding(16.dp)) { AuraBanner(Tipo.ERROR, "No se pudo cargar la agenda", ui.error, accion = "Reintentar", onAccion = vm::cargar) }
                ui.visibles.isEmpty() -> EmptyState("Sin solicitudes", "No hay solicitudes en esta vista.", icon = Icons.Filled.EventBusy)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.visibles, key = { it.id }) { r -> SolicitudCard(r, mostrarCliente = true) { onDetalle(r.id) } }
                }
            }
        }
    }
}

// ============================================================
//  Detalle: el cliente ve su solicitud; la administradora la revisa y decide
// ============================================================
@Composable
fun SolicitudDetalleScreen(onBack: () -> Unit, vm: SolicitudDetalleViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val host = remember { SnackbarHostState() }
    RecogerToasts(vm.eventos, host)
    LaunchedEffect(Unit) { vm.pdf.collect { abrirPdf(ctx, it) } }
    val r = ui.r

    Scaffold(containerColor = AuraBg, snackbarHost = { ToastHost(host) }, topBar = {
        AuraTopBar(if (vm.esAdmin) "Revisión de solicitud" else "Detalle de solicitud",
            r?.let { "${it.folio.folioVisible()} · Recibida ${it.creadoEn.recibida()}" } ?: "", onBack = onBack)
    }) { pad ->
        when {
            ui.loading -> Box(Modifier.padding(pad)) { LoadingBox() }
            r == null -> Box(Modifier.padding(pad).padding(16.dp)) { AuraBanner(Tipo.ERROR, "No se pudo abrir la solicitud", ui.error, accion = "Reintentar", onAccion = vm::cargar) }
            else -> Contenido(r, ui, vm, Modifier.padding(pad))
        }
    }

    if (ui.confirmarRechazo) AlertDialog(
        onDismissRequest = { vm.pedirRechazo(false) }, containerColor = Color.White,
        title = { Text("¿Rechazar solicitud?") },
        text = { Text("El cliente verá el motivo en su historial. Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = vm::rechazar) { Text("Sí, rechazar", color = AuraRed, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = { vm.pedirRechazo(false) }) { Text("Cancelar", color = AuraMuted) } }
    )
    if (ui.notaLista && r != null) NotaListaDialog(r, ui.descargando, onDescargar = vm::descargarNota, onCerrar = vm::cerrarNota)
}

@Composable
private fun Contenido(r: ReservacionDto, ui: DetalleUi, vm: SolicitudDetalleViewModel, modifier: Modifier) {
    val ctx = LocalContext.current
    val pendiente = r.estado == "PENDIENTE"
    val flete = if (vm.esAdmin && pendiente) (ui.flete.toDoubleOrNull() ?: 0.0) else r.costoFlete
    val total = r.subtotal + r.iva + flete

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(r.estado)
            Spacer(Modifier.weight(1f))
            Text(rangoFechas(r.fechaEvento, r.fechaFin), style = MaterialTheme.typography.labelMedium, color = AuraMuted)
        }
        if (r.estado == "RECHAZADO") AuraBanner(Tipo.ERROR, "Solicitud rechazada", r.motivoRechazo?.takeIf { it.isNotBlank() } ?: "La administradora no aprobó esta solicitud.")
        if (r.estado == "APROBADO") AuraBanner(Tipo.EXITO, "Solicitud aprobada", "El inventario está reservado para tus fechas y la nota de remisión ya está disponible.")
        ui.error?.let { AuraBanner(Tipo.ERROR, it) }

        // Datos del cliente (administradora) o del evento (cliente)
        SeccionTitulo(if (vm.esAdmin) "Datos del cliente" else "Datos del evento")
        AuraCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (vm.esAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        Avatar(r.cliente, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(r.cliente, style = MaterialTheme.typography.titleSmall); Text(r.correo ?: "", style = MaterialTheme.typography.bodySmall, color = AuraMuted) }
                    }
                    InfoRow("Teléfono", r.telefono ?: "—")
                }
                InfoRow("Fecha del evento", rangoFechas(r.fechaEvento, r.fechaFin))
                InfoRow("Días de renta", "${r.dias} ${if (r.dias == 1) "día" else "días"}")
                InfoRow("Dirección de entrega", r.direccion)
                InfoRow("Entorno", r.entorno.lowercase().replaceFirstChar { it.uppercase() })
                if (!r.notas.isNullOrBlank()) InfoRow("Notas del cliente", r.notas)
                if (r.latitud != null && r.longitud != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlineButton("Ver ubicación en el mapa", { abrirMapa(ctx, r.latitud, r.longitud, r.folio) }, icon = Icons.Filled.Place)
                }
            }
        }

        SeccionTitulo("Artículos solicitados")
        r.detalles.forEach { d ->
            AuraCard {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ArticuloImagen(d.sku, null, d.nombre, false, Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)), mostrarNombre = false)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(d.nombre, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 2)
                        Text("x${d.cantidad}${d.sku?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                    }
                    Text(d.subtotal.mxnSinCentavos(), style = MaterialTheme.typography.titleSmall, color = AuraGreenDark, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Verificación de stock (solo administradora y solo si está pendiente)
        if (vm.esAdmin && pendiente) {
            SeccionTitulo("Verificación de stock")
            AuraCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    r.detalles.forEach { d ->
                        val libres = ui.disponibles[d.articuloId]
                        val bien = libres == null || libres >= d.cantidad
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(d.nombre, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (libres == null) "—" else if (bien) "✓ $libres disponibles" else "⚠ solo $libres disponibles",
                                style = MaterialTheme.typography.labelMedium, color = if (bien) AuraGreenDark else AuraRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            SeccionTitulo("Asignar costo de flete")
            AuraCard(color = AuraGoldSoft, borderColor = AuraGold.copy(alpha = 0.5f)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledField("Costo de flete en MXN (obligatorio antes de aprobar)", ui.flete, vm::flete, placeholder = "450",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal, error = ui.errorFlete, leading = Icons.Filled.LocalShipping)
                    Text("Este costo se sumará al total del cliente y aparecerá en la nota de remisión.", style = MaterialTheme.typography.bodySmall, color = AuraAmber)
                }
            }
        }

        SeccionTitulo(if (vm.esAdmin && pendiente) "Resumen financiero" else "Resumen")
        Surface(shape = RoundedCornerShape(18.dp), color = AuraNavy) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                InfoRow("Subtotal artículos", r.subtotal.mxn(), Color.White, bold = true, onDark = true)
                InfoRow("IVA (16%)", r.iva.mxn(), Color.White, bold = true, onDark = true)
                InfoRow(if (vm.esAdmin) "Flete asignado" else "Flete", if (pendiente && (!vm.esAdmin || ui.flete.isBlank())) "Por asignar" else flete.mxn(), AuraGold, bold = true, onDark = true)
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (vm.esAdmin) "Total a cobrar" else "Total", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${(if (pendiente && !vm.esAdmin) r.subtotal + r.iva else total).mxn()} MXN", style = MaterialTheme.typography.titleLarge, color = AuraGreen)
                }
            }
        }

        if (vm.esAdmin && pendiente) {
            SeccionTitulo("Motivo de rechazo (solo si rechaza)")
            LabeledField("", ui.motivo, vm::motivo, placeholder = "Ej: fecha ocupada, inventario insuficiente en esa zona…", singleLine = false, minLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Aprobar", vm::aprobar, Modifier.weight(1f), loading = ui.procesando, icon = Icons.Filled.Check, color = AuraGreenDark)
                PrimaryButton("Rechazar", { vm.pedirRechazo(true) }, Modifier.weight(1f), enabled = !ui.procesando, icon = Icons.Filled.Close, color = AuraRed)
            }
            Text("Al aprobar se bloquea el stock y se genera la nota de remisión.", style = MaterialTheme.typography.bodySmall, color = AuraMuted,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else {
            // La nota solo se puede descargar cuando la solicitud ya está aprobada
            PrimaryButton("Descargar nota de remisión (PDF)", vm::descargarNota, enabled = r.estado == "APROBADO", loading = ui.descargando, icon = Icons.Filled.PictureAsPdf)
            if (pendiente) Text("Disponible cuando la administradora apruebe tu solicitud.", style = MaterialTheme.typography.bodySmall, color = AuraMuted,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SeccionTitulo(texto: String) = SectionLabel(texto, color = AuraGold)

@Composable
private fun NotaListaDialog(r: ReservacionDto, descargando: Boolean, onDescargar: () -> Unit, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column {
                Row(Modifier.fillMaxWidth().background(AuraNavy).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AuraGreen), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Description, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column { Text("Nota de Remisión Lista", color = Color.White, style = MaterialTheme.typography.titleSmall); Text("Generada automáticamente al aprobar", color = Color(0xFF8FA0B8), style = MaterialTheme.typography.bodySmall) }
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AuraBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PictureAsPdf, null, tint = AuraRed); Spacer(Modifier.width(10.dp))
                        Text("Remision_${r.folio}.pdf", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    InfoRow("Pedido", r.folio.folioVisible(), bold = true)
                    InfoRow("Cliente", r.cliente, bold = true)
                    InfoRow("Fecha del evento", rangoFechas(r.fechaEvento, r.fechaFin), bold = true)
                    InfoRow("Total con flete", "${r.total.mxn()} MXN", AuraGreenDark, bold = true)
                    InfoRow("Estado", "✓ APROBADO", AuraGreenDark, bold = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton("Descargar PDF", onDescargar, Modifier.weight(1f), loading = descargando, icon = Icons.Filled.Download)
                        OutlineButton("Cerrar", onCerrar, Modifier.weight(0.6f))
                    }
                }
            }
        }
    }
}
