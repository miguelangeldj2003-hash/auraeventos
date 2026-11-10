package com.aura.eventos.ui.reserva

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.repository.Montos
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// ============================================================
//  Reservación (paso 3): fechas, dirección y resumen
// ============================================================
@Composable
fun ReservaScreen(onBack: () -> Unit, onCreada: (Long) -> Unit, vm: ReservaViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.creada.collect { onCreada(it.id) } }

    val permisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) vm.ubicarLugar() else vm.permisoDenegado()
    }

    Scaffold(containerColor = AuraBg, topBar = { AuraTopBar("Reservación", "Catálogo › Carrito › Reservación · Paso 3 de 3", onBack = onBack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                SectionLabel("Paso 3 · Selección de fecha", color = AuraGreen)
                Text("¿Cuándo es tu evento?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 4.dp))
                Text("Selecciona la fecha de entrega y recogida. Las fechas en gris ya están ocupadas.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
            }
            ui.conflicto?.let { AuraBanner(Tipo.ERROR, "Fecha no disponible (Overbooking)", it) }

            AuraCard {
                Column(Modifier.padding(16.dp)) {
                    CalendarioRango(ui.mes, ui.ocupadas, ui.inicio, ui.fin, vm::tocarDia, vm::cambiarMes)
                }
            }
            ui.inicio?.let { ini ->
                val fin = ui.finEfectivo ?: ini
                val texto = if (ui.fin == null) "Entrega: ${ini.toString().fechaBonita()} · toca otro día para la recogida (o el mismo si es de un solo día)"
                            else "Entrega: ${ini.toString().fechaBonita()} · Recogida: ${fin.toString().fechaBonita()} (${ui.dias} ${if (ui.dias == 1) "día" else "días"})"
                Surface(shape = RoundedCornerShape(12.dp), color = AuraGreenSoft) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.EventAvailable, null, tint = AuraGreenDark, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(texto, style = MaterialTheme.typography.labelMedium, color = AuraGreenDark)
                    }
                }
            }

            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = AuraRed, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                        Text("Dirección del evento", style = MaterialTheme.typography.titleMedium)
                    }
                    LabeledField("Dirección de entrega", ui.direccion, vm::direccion, placeholder = "Calle, número, colonia, ciudad")
                    Column {
                        Text("Entorno", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterPill(ui.entorno == "INTERIOR", "Interior", { vm.entorno("INTERIOR") }, icon = Icons.Filled.Home)
                            FilterPill(ui.entorno == "EXTERIOR", "Exterior", { vm.entorno("EXTERIOR") }, icon = Icons.Filled.Park)
                        }
                    }
                    LabeledField("Notas para la entrega (opcional)", ui.notas, vm::notas, placeholder = "2do piso, portón negro, tiene rampa…", singleLine = false, minLines = 2)
                    // Ubicación GPS del lugar del evento
                    Surface(shape = RoundedCornerShape(12.dp), color = AuraBg) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ubicación en el mapa (opcional)", style = MaterialTheme.typography.labelLarge)
                            when {
                                ui.ubicando -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = AuraGreen); Spacer(Modifier.width(8.dp)); Text("Obteniendo GPS…", style = MaterialTheme.typography.bodySmall)
                                }
                                ui.latitud != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = AuraGreen, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                                    Text("%.5f, %.5f".format(ui.latitud, ui.longitud), style = MaterialTheme.typography.bodySmall, color = AuraGreenDark, fontWeight = FontWeight.SemiBold)
                                }
                                else -> Text("Si estás en el lugar del evento, guarda tu ubicación para que lleguen directo.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                            }
                            ui.errorUbicacion?.let { Text(it, color = AuraRed, style = MaterialTheme.typography.bodySmall) }
                            OutlineButton("Usar mi ubicación actual", {
                                val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (ok) vm.ubicarLugar() else permisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }, icon = Icons.Filled.MyLocation)
                        }
                    }
                }
            }

            // Resumen en tarjeta azul, como en la web
            val sub = items.sumOf { it.subtotal }; val iva = Montos.iva(sub)
            Surface(shape = RoundedCornerShape(20.dp), color = AuraNavy) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.fillMaxWidth().height(3.dp).background(AuraGreen))
                    Text("Resumen de Solicitud", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("Confirma los artículos antes de enviar", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA0B8))
                    items.forEach { c ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AuraNavySoft).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            ArticuloImagen(c.articulo.sku, c.articulo.imagenUrl, c.articulo.nombre, c.articulo.seccion == "MESA_DULCES", Modifier.size(46.dp).clip(RoundedCornerShape(8.dp)), mostrarNombre = false)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.articulo.nombre, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 2)
                                Text("x${c.cantidad} · ${c.articulo.categoria}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA0B8), maxLines = 1)
                            }
                            Text(c.subtotal.mxnSinCentavos(), color = AuraGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Subtotal", "${sub.mxn()} MXN", Color.White, bold = true, onDark = true)
                    InfoRow("IVA (16%)", "${iva.mxn()} MXN", Color.White, bold = true, onDark = true)
                    InfoRow("Flete", "Por asignar", AuraGreen, bold = true, onDark = true)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total estimado", Modifier.weight(1f), color = Color(0xFFB8C4D6))
                        Text((sub + iva).mxnSinCentavos(), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    }
                    ui.inicio?.let { ini ->
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AuraNavySoft).padding(12.dp)) {
                            Text("FECHA SELECCIONADA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8FA0B8))
                            Text(rangoFechas(ini.toString(), (ui.finEfectivo ?: ini).toString()), style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Text("${ui.dias} ${if (ui.dias == 1) "día" else "días"} de renta", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FA0B8))
                        }
                    }
                    ui.error?.let { AuraBanner(Tipo.ERROR, it) }
                    PrimaryButton("Enviar solicitud", vm::enviar, loading = ui.enviando, enabled = items.isNotEmpty(), icon = Icons.Filled.Check)
                    Text("Al enviar, un agente de Aura revisará tu solicitud y te notificará en menos de 24 horas.", style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8FA0B8), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// ============================================================
//  Calendario de rango (seleccionado, rango, no disponible, hoy)
// ============================================================
@Composable
private fun CalendarioRango(
    mes: YearMonth, ocupadas: Set<LocalDate>, inicio: LocalDate?, fin: LocalDate?,
    onDia: (LocalDate) -> Unit, onMes: (Int) -> Unit
) {
    val hoy = LocalDate.now()
    val titulo = mes.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() } + " ${mes.year}"
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            val puedeAtras = mes.isAfter(YearMonth.now())
            IconButton(onClick = { onMes(-1) }, enabled = puedeAtras, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AuraBg)) {
                Icon(Icons.Filled.ChevronLeft, "Mes anterior", tint = if (puedeAtras) AuraNavy else AuraFaint)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onMes(1) }, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(AuraBg)) {
                Icon(Icons.Filled.ChevronRight, "Mes siguiente", tint = AuraNavy)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = AuraFaint)
            }
        }
        val vacios = mes.atDay(1).dayOfWeek.value % 7
        val celdas: List<LocalDate?> = List(vacios) { null } + (1..mes.lengthOfMonth()).map { mes.atDay(it) }
        celdas.chunked(7).forEach { semana ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (semana + List(7 - semana.size) { null }).forEach { d ->
                    Box(Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.Center) { if (d != null) Dia(d, hoy, ocupadas, inicio, fin, onDia) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Leyenda(AuraGreen, "Seleccionado"); Leyenda(AuraGreenSoft, "Rango"); Leyenda(Color(0xFFD5DBE3), "No disponible")
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color)); Spacer(Modifier.width(5.dp))
    Text(texto, style = MaterialTheme.typography.bodySmall, color = AuraMuted)
}

@Composable
private fun Dia(d: LocalDate, hoy: LocalDate, ocupadas: Set<LocalDate>, inicio: LocalDate?, fin: LocalDate?, onDia: (LocalDate) -> Unit) {
    val pasado = d.isBefore(hoy)
    val ocupado = d in ocupadas
    val extremo = d == inicio || d == fin || (fin == null && d == inicio)
    val enRango = inicio != null && fin != null && d.isAfter(inicio) && d.isBefore(fin)
    val forma = RoundedCornerShape(10.dp)
    val base = Modifier.fillMaxSize().padding(1.dp).clip(forma)
    val fondo = when {
        extremo -> base.background(AuraGreen)
        enRango -> base.background(AuraGreenSoft)
        ocupado -> base.drawBehind {
            drawRect(Color(0xFFF1F4F8))
            var x = -size.height
            while (x < size.width) { drawLine(Color(0xFFD5DBE3), Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 1.5f); x += 9.dp.toPx() }
        }
        else -> base
    }
    val conBorde = if (d == hoy && !extremo) fondo.border(1.5.dp, AuraGreen, forma) else fondo
    Box(if (pasado || ocupado) conBorde else conBorde.clickable { onDia(d) }, contentAlignment = Alignment.Center) {
        Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelLarge,
            color = when { extremo -> Color.White; pasado || ocupado -> AuraFaint; else -> AuraNavy },
            fontWeight = if (extremo) FontWeight.Bold else FontWeight.Medium)
    }
}

// ============================================================
//  Solicitud enviada
// ============================================================
@Composable
fun ExitoScreen(onInicio: () -> Unit, onCatalogo: () -> Unit, vm: ExitoViewModel = hiltViewModel()) {
    val r by vm.reservacion.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(AuraBg).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().background(AuraNavy).padding(top = 24.dp, bottom = 28.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = onInicio, modifier = Modifier.align(Alignment.TopEnd).size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))) {
                Icon(Icons.Filled.Close, "Cerrar", tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(AuraGreen.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(58.dp).clip(CircleShape).background(AuraGreen), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("¡Solicitud Enviada!", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text("Tu solicitud de reservación está en revisión. Un agente de Aura Eventos te contactará en menos de 24 horas.",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8C4D6), textAlign = TextAlign.Center)
            }
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(AuraGreen))
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SoftPill("PENDIENTE DE APROBACIÓN", AuraAmber, AuraAmberSoft, icon = Icons.Filled.Schedule)
            r?.let { res ->
                AuraCard {
                    Column(Modifier.padding(16.dp)) {
                        InfoRow("Número de solicitud", res.folio.folioVisible(), bold = true)
                        HorizontalDivider(color = AuraBorder)
                        InfoRow("Fecha del evento", rangoFechas(res.fechaEvento, res.fechaFin), bold = true)
                        HorizontalDivider(color = AuraBorder)
                        InfoRow("Artículos seleccionados", "${res.detalles.sumOf { it.cantidad }} artículos", bold = true)
                        HorizontalDivider(color = AuraBorder)
                        InfoRow("Total estimado", "${(res.subtotal + res.iva).mxn()} MXN", AuraGreenDark, bold = true)
                        HorizontalDivider(color = AuraBorder)
                        InfoRow("Flete", "Por asignar", AuraGold, bold = true)
                    }
                }
            } ?: CircularProgressIndicator(color = AuraGreen)
            PrimaryButton("Ir a inicio", onInicio)
            OutlineButton("Explorar más artículos", onCatalogo)
        }
    }
}
