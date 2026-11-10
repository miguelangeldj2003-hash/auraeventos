package com.aura.eventos.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.remote.DashboardDto
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class PanelUi(val datos: DashboardDto? = null, val loading: Boolean = true, val error: String? = null)

@HiltViewModel
class PanelViewModel @Inject constructor(private val repo: AuraRepository) : ViewModel() {
    private val _ui = MutableStateFlow(PanelUi())
    val ui: StateFlow<PanelUi> = _ui.asStateFlow()
    init { cargar() }

    fun cargar() {
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = repo.dashboard()
            _ui.update { it.copy(loading = false, datos = r.getOrNull() ?: it.datos, error = r.exceptionOrNull()?.message) }
        }
    }
}

@Composable
fun PanelScreen(onDetalle: (Long) -> Unit, onAgenda: () -> Unit, vm: PanelViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    AlVolver(vm::cargar)
    val hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "MX"))).replaceFirstChar { it.uppercase() }

    Scaffold(containerColor = AuraBg, topBar = { AuraTopBar("Panel de control", hoy, actions = { BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar) }) }) { pad ->
        val d = ui.datos
        when {
            d == null && ui.loading -> Box(Modifier.padding(pad)) { LoadingBox() }
            d == null -> Box(Modifier.padding(pad).padding(16.dp)) { AuraBanner(Tipo.ERROR, "No se pudo cargar el panel", ui.error, accion = "Reintentar", onAccion = vm::cargar) }
            else -> Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ui.error?.let { AuraBanner(Tipo.ERROR, it) }
                if (d.pendientes > 0) AuraBanner(Tipo.AVISO, "${d.pendientes} solicitudes requieren revisión",
                    "Asigna el flete y toma una decisión para cada una.", accion = "Ir a Agenda", onAccion = onAgenda)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(Icons.Filled.TrendingUp, AuraGreenDark, AuraGreenSoft, d.ingresosMes.mxnSinCentavos(), "Ingresos del mes", Modifier.weight(1f),
                        badge = (if (d.variacionMes >= 0) "↑ +" else "↓ ") + "${d.variacionMes}% vs anterior",
                        badgeFg = if (d.variacionMes >= 0) AuraGreenDark else AuraRed, badgeBg = if (d.variacionMes >= 0) AuraGreenSoft else AuraRedSoft)
                    MetricCard(Icons.Filled.Schedule, AuraAmber, AuraAmberSoft, "${d.pendientes}", "Solicitudes pendientes", Modifier.weight(1f),
                        badge = "Requieren acción", badgeFg = AuraAmber, badgeBg = AuraAmberSoft)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(Icons.Filled.EventAvailable, AuraGreenDark, AuraGreenSoft, "${d.aprobados}", "Eventos confirmados", Modifier.weight(1f))
                    MetricCard(Icons.Filled.Inventory2, AuraNavy, AuraBg, "${d.ocupacionMes}%", "Ocupación del mes", Modifier.weight(1f))
                }

                AuraCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ingresos mensuales", style = MaterialTheme.typography.titleMedium)
                        Text("Últimos 6 meses · MXN", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                        val max = (d.ingresosMensuales.maxOfOrNull { it.total } ?: 0.0).coerceAtLeast(1.0)
                        Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            d.ingresosMensuales.forEachIndexed { i, m ->
                                val ultimo = i == d.ingresosMensuales.lastIndex
                                Column(Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.fillMaxWidth().height((112 * (m.total / max)).toInt().coerceAtLeast(6).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(if (ultimo) AuraGreen else AuraGreen.copy(alpha = 0.28f)))
                                    Text(m.mes, style = MaterialTheme.typography.labelSmall, color = if (ultimo) AuraGreenDark else AuraFaint, modifier = Modifier.padding(top = 6.dp))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column { Text("ESTE MES", style = MaterialTheme.typography.labelSmall, color = AuraFaint); Text(d.ingresosMes.mxnSinCentavos(), style = MaterialTheme.typography.titleSmall, color = AuraGreenDark) }
                            Column { Text("TOTAL APROBADO", style = MaterialTheme.typography.labelSmall, color = AuraFaint); Text(d.ingresosTotales.mxnSinCentavos(), style = MaterialTheme.typography.titleSmall) }
                        }
                    }
                }

                AuraCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Top artículos rentados", style = MaterialTheme.typography.titleMedium)
                        Text("Por frecuencia", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                        if (d.topArticulos.isEmpty()) Text("Aún no hay rentas aprobadas.", style = MaterialTheme.typography.bodySmall)
                        val colores = listOf(AuraGreen, AuraNavy, Color(0xFF475569), AuraGold)
                        d.topArticulos.forEachIndexed { i, t ->
                            Column {
                                Row { Text(t.nombre, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${t.porcentaje}%", style = MaterialTheme.typography.labelLarge, color = colores[i % 4], fontWeight = FontWeight.Bold) }
                                LinearProgressIndicator(progress = { t.porcentaje / 100f }, Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = colores[i % 4], trackColor = AuraBg)
                            }
                        }
                        d.proxima?.let { p ->
                            HorizontalDivider(color = AuraBorder)
                            Text("PRÓXIMA ENTREGA", style = MaterialTheme.typography.labelSmall, color = AuraFaint)
                            Row(Modifier.clickable { onDetalle(p.id) }, verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AuraGreen)); Spacer(Modifier.width(8.dp))
                                Text("${if (p.fechaEvento == LocalDate.now().toString()) "Hoy" else p.fechaEvento.fechaCorta()} · Pedido ${p.folio.folioVisible()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                ListaTarjeta("Solicitudes recientes", "Ver todas →", onAgenda, d.recientes, onDetalle, vacio = "Sin solicitudes todavía.") { r ->
                    FilaReciente(r.cliente, r.fechaEvento.fechaBonita(), r.total.mxnSinCentavos()) { StatusPill(r.estado) }
                }
                ListaTarjeta("Entregas de hoy", "Ver agenda →", onAgenda, d.entregasHoy, onDetalle, vacio = "No hay entregas programadas para hoy.") { r ->
                    FilaReciente(r.detalles.firstOrNull()?.nombre ?: r.cliente, r.direccion, null) { SoftPill("HOY", AuraGreenDark, AuraGreenSoft) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ListaTarjeta(
    titulo: String, accion: String, onAccion: () -> Unit, items: List<ReservacionDto>, onDetalle: (Long) -> Unit, vacio: String,
    fila: @Composable (ReservacionDto) -> Unit
) {
    AuraCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(accion, color = AuraGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onAccion))
            }
            if (items.isEmpty()) Text(vacio, style = MaterialTheme.typography.bodySmall, color = AuraMuted)
            items.forEachIndexed { i, r ->
                if (i > 0) HorizontalDivider(color = AuraBorder)
                Box(Modifier.clickable { onDetalle(r.id) }) { fila(r) }
            }
        }
    }
}

@Composable
private fun FilaReciente(titulo: String, subtitulo: String, derecha: String?, pill: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = AuraMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (derecha != null) Text(derecha, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 10.dp))
        pill()
    }
}
