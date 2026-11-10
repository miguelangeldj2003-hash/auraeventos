package com.aura.eventos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.eventos.sync.SyncUiState
import com.aura.eventos.ui.theme.*

/**
 * Barra delgada tipo Spotify: aparece pegada abajo cuando hay algo que sincronizar.
 * Al tocarla abre el detalle de lo que se subió.
 */
@Composable
fun SyncBar(state: SyncUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Recordamos el último estado visible para animar la salida sin parpadeos
    val last = remember { mutableStateOf(state) }
    if (state !is SyncUiState.Hidden) last.value = state
    val s = last.value

    AnimatedVisibility(
        visible = state !is SyncUiState.Hidden,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        val (bg, icon, text) = when (s) {
            is SyncUiState.Offline -> Triple(AuraAmberBright, Icons.Filled.CloudOff,
                "Sin conexión · ${s.pending} guardado${if (s.pending == 1) "" else "s"} en el teléfono")
            is SyncUiState.Syncing -> Triple(AuraNavy, Icons.Filled.CloudUpload,
                "Subiendo ${minOf(s.done + 1, s.total)} de ${s.total}…")
            is SyncUiState.Done -> Triple(AuraGreen, Icons.Filled.CloudDone,
                "Todo sincronizado · ${s.count} elemento${if (s.count == 1) "" else "s"} subido${if (s.count == 1) "" else "s"}")
            is SyncUiState.Error -> Triple(AuraRed, Icons.Filled.ErrorOutline,
                "${s.failed} no se pudo subir · toca para reintentar")
            SyncUiState.Hidden -> Triple(AuraNavy, Icons.Filled.Cloud, "")
        }
        Column(modifier = Modifier.fillMaxWidth().background(bg).clickable(onClick = onClick)) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowUp, "Ver detalle", tint = Color.White)
            }
            // La "línea" de progreso estilo Spotify
            when (s) {
                is SyncUiState.Syncing -> LinearProgressIndicator(
                    progress = { if (s.total == 0) 0f else s.done / s.total.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = AuraGreen, trackColor = Color.White.copy(alpha = 0.25f)
                )
                is SyncUiState.Done -> LinearProgressIndicator(
                    progress = { 1f }, modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Color.White, trackColor = Color.White.copy(alpha = 0.25f)
                )
                else -> Spacer(Modifier.height(3.dp))
            }
        }
    }
}
