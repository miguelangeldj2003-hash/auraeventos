package com.aura.eventos.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aura.eventos.data.local.EvidenceEntity
import com.aura.eventos.data.local.SyncStatus
import com.aura.eventos.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Detalle que se abre al tocar la barra: qué se subió, qué falta y qué falló. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDetailSheet(
    items: List<EvidenceEntity>,
    lastSyncedAt: Long?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val synced = items.filter { it.status == SyncStatus.SYNCED }
    val pending = items.filter { it.status == SyncStatus.PENDING || it.status == SyncStatus.UPLOADING }
    val failed = items.filter { it.status == SyncStatus.FAILED }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Sincronización", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    lastSyncedAt?.let {
                        "Última vez: " + DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                    } ?: "Aún no se ha subido nada",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Counter("Subidos", synced.size, AuraGreen, Modifier.weight(1f))
                    Counter("Pendientes", pending.size, AuraAmber, Modifier.weight(1f))
                    Counter("Con error", failed.size, AuraRed, Modifier.weight(1f))
                }
                if (failed.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Icon(Icons.Filled.Refresh, null); Spacer(Modifier.width(8.dp)); Text("Reintentar ahora")
                    }
                }
            }
            if (failed.isNotEmpty()) { item { Section("Con error") }; items(failed, key = { it.id }) { FilaEvidencia(it) } }
            if (pending.isNotEmpty()) { item { Section("Pendientes de subir") }; items(pending, key = { it.id }) { FilaEvidencia(it) } }
            if (synced.isNotEmpty()) { item { Section("Subidos correctamente") }; items(synced, key = { it.id }) { FilaEvidencia(it) } }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable private fun Section(t: String) =
    Text(t, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))

@Composable
private fun Counter(label: String, n: Int, color: Color, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$n", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FilaEvidencia(e: EvidenceEntity) {
    val hour = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "MX"))
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = File(e.photoPath), contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${e.eventFolio} · ${e.type.label}", fontWeight = FontWeight.SemiBold)
            Text(hour.format(Date(e.capturedAt)), style = MaterialTheme.typography.bodySmall)
            if (e.latitude != null && e.longitude != null)
                Text("📍 %.5f, %.5f (±%d m)".format(e.latitude, e.longitude, (e.accuracyMeters ?: 0f).toInt()),
                    style = MaterialTheme.typography.bodySmall)
            if (e.status == SyncStatus.FAILED && e.lastError != null)
                Text(e.lastError, style = MaterialTheme.typography.bodySmall, color = AuraRed)
            if (e.status == SyncStatus.SYNCED && e.syncedAt != null)
                Text("Subido ${hour.format(Date(e.syncedAt))}", style = MaterialTheme.typography.bodySmall, color = AuraGreen)
        }
        val (icon, tint) = when (e.status) {
            SyncStatus.SYNCED -> Icons.Filled.CheckCircle to AuraGreen
            SyncStatus.FAILED -> Icons.Filled.ErrorOutline to AuraRed
            SyncStatus.UPLOADING -> Icons.Filled.CloudUpload to AuraNavy
            SyncStatus.PENDING -> Icons.Filled.Schedule to AuraAmber
        }
        Icon(icon, null, tint = tint)
    }
}
