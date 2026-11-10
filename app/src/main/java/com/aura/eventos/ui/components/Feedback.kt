package com.aura.eventos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.eventos.ui.theme.*

enum class Tipo(val color: Color, val suave: Color, val icono: ImageVector) {
    EXITO(AuraGreen, AuraGreenSoft, Icons.Filled.CheckCircle),
    AVISO(AuraAmber, AuraAmberSoft, Icons.Filled.WarningAmber),
    ERROR(AuraRed, AuraRedSoft, Icons.Filled.ErrorOutline)
}

/** Mensaje flotante (toast) como los de la web: barra de color a la izquierda, título y detalle. */
class ToastVisuals(val tipo: Tipo, val titulo: String, val detalle: String? = null) : SnackbarVisuals {
    override val message: String get() = titulo
    override val actionLabel: String? get() = null
    override val withDismissAction: Boolean get() = true
    override val duration: SnackbarDuration get() = SnackbarDuration.Short
}

suspend fun SnackbarHostState.toast(tipo: Tipo, titulo: String, detalle: String? = null) {
    showSnackbar(ToastVisuals(tipo, titulo, detalle))
}

@Composable
fun ToastHost(estado: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(estado, modifier) { data ->
        val v = data.visuals as? ToastVisuals
        val tipo = v?.tipo ?: Tipo.EXITO
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 8.dp
        ) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(tipo.color))
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(tipo.suave), contentAlignment = Alignment.Center) {
                        Icon(tipo.icono, null, tint = tipo.color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(v?.titulo ?: data.visuals.message, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AuraNavy)
                        v?.detalle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = AuraMuted) }
                    }
                    IconButton(onClick = { data.dismiss() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, "Cerrar", tint = AuraFaint, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/** Aviso dentro de la pantalla (banner): éxito, advertencia o error, con botón de acción opcional. */
@Composable
fun AuraBanner(
    tipo: Tipo, titulo: String, detalle: String? = null, modifier: Modifier = Modifier,
    accion: String? = null, onAccion: (() -> Unit)? = null
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier.fillMaxWidth().clip(forma).background(tipo.suave).border(1.dp, tipo.color.copy(alpha = 0.35f), forma).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(tipo.icono, null, tint = tipo.color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = tipo.color)
                if (detalle != null) Text(detalle, style = MaterialTheme.typography.bodySmall, color = tipo.color.copy(alpha = 0.9f))
            }
        }
        if (accion != null && onAccion != null) {
            Surface(onClick = onAccion, shape = RoundedCornerShape(50), color = tipo.color, modifier = Modifier.align(Alignment.End)) {
                Text(accion, Modifier.padding(horizontal = 16.dp, vertical = 7.dp), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ErrorBanner(mensaje: String, modifier: Modifier = Modifier) = AuraBanner(Tipo.ERROR, mensaje, modifier = modifier)

/** Evento de mensaje que un ViewModel manda a su pantalla. */
data class ToastEvent(val tipo: Tipo, val titulo: String, val detalle: String? = null)

@Composable
fun RecogerToasts(eventos: kotlinx.coroutines.flow.Flow<ToastEvent>, host: SnackbarHostState) {
    androidx.compose.runtime.LaunchedEffect(Unit) { eventos.collect { host.toast(it.tipo, it.titulo, it.detalle) } }
}

/** Ejecuta la acción cada vez que la pantalla vuelve a verse (menos la primera, que ya cargó sola). */
@Composable
fun AlVolver(accion: () -> Unit) {
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val actual = androidx.compose.runtime.rememberUpdatedState(accion)
    androidx.compose.runtime.DisposableEffect(owner) {
        var primera = true
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) { if (primera) primera = false else actual.value() }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
}
