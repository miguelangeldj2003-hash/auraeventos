package com.aura.eventos.ui.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.solicitudes.InicioViewModel
import com.aura.eventos.ui.solicitudes.SolicitudCard
import com.aura.eventos.ui.theme.*

@Composable
fun InicioScreen(onDetalle: (Long) -> Unit, onSolicitudes: () -> Unit, onCatalogo: () -> Unit, vm: InicioViewModel = hiltViewModel()) {
    val lista by vm.lista.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val sesion by vm.session.collectAsStateWithLifecycle()
    AlVolver(vm::cargar)

    val pendientes = lista.filter { it.estado == "PENDIENTE" }
    val aprobadas = lista.filter { it.estado == "APROBADO" }
    val invertido = aprobadas.sumOf { it.total }
    val primerNombre = sesion?.nombre?.split(" ")?.firstOrNull().orEmpty()

    Scaffold(containerColor = AuraBg, topBar = {
        Surface(color = androidx.compose.ui.graphics.Color.White, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(start = 16.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AuraLogo(Modifier.weight(1f))
                BarIcon(Icons.Filled.Refresh, "Actualizar", vm::cargar)
                Avatar(sesion?.nombre ?: "", 36.dp, AuraNavy, androidx.compose.ui.graphics.Color.White)
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text("Mi Dashboard", style = MaterialTheme.typography.headlineSmall)
                Text("Hola, $primerNombre. Aquí está el resumen de tus eventos.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
            }
            error?.let { AuraBanner(Tipo.ERROR, "No se pudo actualizar", it, accion = "Reintentar", onAccion = vm::cargar) }
            pendientes.firstOrNull()?.let {
                AuraBanner(Tipo.AVISO, "Fecha pendiente de aprobación", "Solicitud ${it.folio.folioVisible()} aún no confirmada.", accion = "Ver solicitud", onAccion = { onDetalle(it.id) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(Icons.Filled.Schedule, AuraAmber, AuraAmberSoft, "${pendientes.size}", "Solicitudes pendientes", Modifier.weight(1f))
                MetricCard(Icons.Filled.CheckCircle, AuraGreenDark, AuraGreenSoft, "${aprobadas.size}", "Eventos aprobados", Modifier.weight(1f))
            }
            MetricCard(Icons.Filled.TrendingUp, AuraNavy, AuraBg, invertido.mxnSinCentavos(), "Total invertido (MXN)", Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mis solicitudes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("Ver todas →", color = AuraGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onSolicitudes))
            }
            when {
                cargando && lista.isEmpty() -> Box(Modifier.fillMaxWidth().height(120.dp)) { LoadingBox() }
                lista.isEmpty() -> EmptyState("Aún no tienes solicitudes", "Explora el catálogo y arma tu primera reservación.", icon = Icons.Filled.ReceiptLong)
                else -> lista.sortedByDescending { it.creadoEn ?: "" }.take(3).forEach { r -> SolicitudCard(r, mostrarCliente = false) { onDetalle(r.id) } }
            }
            PrimaryButton("Explorar catálogo", onCatalogo, icon = Icons.Filled.Storefront)
            Spacer(Modifier.height(8.dp))
        }
    }
}
