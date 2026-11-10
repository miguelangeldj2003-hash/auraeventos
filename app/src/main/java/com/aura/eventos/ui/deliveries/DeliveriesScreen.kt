package com.aura.eventos.ui.deliveries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*

@Composable
fun DeliveriesScreen(onOpen: (String) -> Unit, vm: DeliveriesViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    AlVolver(vm::cargar)

    Scaffold(containerColor = AuraBg, topBar = {
        AuraTopBar("Entregas", "Eventos aprobados · evidencias con foto y GPS", actions = { BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar) })
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            ui.error?.let { Box(Modifier.padding(16.dp, 12.dp, 16.dp, 0.dp)) { AuraBanner(Tipo.AVISO, "Sin conexión con el servidor", "Mostrando la última lista guardada. Tus evidencias se siguen guardando en el teléfono.") } }
            when {
                ui.loading && ui.lista.isEmpty() -> LoadingBox()
                ui.lista.isEmpty() -> EmptyState("Sin entregas próximas", "Cuando apruebes solicitudes aparecerán aquí.", icon = Icons.Filled.LocalShipping)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.lista, key = { it.reservacion.id }) { d ->
                        val r = d.reservacion
                        AuraCard(onClick = { onOpen(r.folio) }) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Avatar(r.cliente, 40.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(r.cliente, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text(r.folio.folioVisible(), style = MaterialTheme.typography.bodySmall, color = AuraGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                    StatusPill(r.estado)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarMonth, null, tint = AuraMuted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                                    Text("Entrega ${r.fechaEvento.fechaBonita()} · Recogida ${r.fechaFin.fechaBonita()}", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, null, tint = AuraMuted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                                    Text(r.direccion, style = MaterialTheme.typography.bodySmall, color = AuraMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                if (!r.notas.isNullOrBlank()) Text("“${r.notas}”", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (d.pendientes > 0) SoftPill("${d.pendientes} sin subir", AuraAmber, AuraAmberSoft, icon = Icons.Filled.Schedule)
                                    if (d.subidas > 0) SoftPill("${d.subidas} sincronizada${if (d.subidas == 1) "" else "s"}", AuraGreenDark, AuraGreenSoft, icon = Icons.Filled.CheckCircle)
                                    if (d.pendientes == 0 && d.subidas == 0) Text("Sin evidencias", style = MaterialTheme.typography.bodySmall, color = AuraFaint)
                                }
                                HorizontalDivider(color = AuraBorder)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (r.latitud != null && r.longitud != null)
                                        OutlineButton("Cómo llegar", { abrirMapa(ctx, r.latitud, r.longitud, r.folio) }, Modifier.weight(1f), icon = Icons.Filled.Directions)
                                    PrimaryButton("Tomar evidencia", { onOpen(r.folio) }, Modifier.weight(1f), icon = Icons.Filled.CameraAlt)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
