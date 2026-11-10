package com.aura.eventos.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.demo.FakeAuraApi
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.ui.auth.DialogoServidor
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    val session: SessionManager,
    private val repo: AuraRepository,
    private val demo: FakeAuraApi
) : ViewModel() {
    fun cerrarSesion() = repo.cerrarSesion()
    fun reiniciarDemo() = demo.reiniciar()
}

@Composable
fun PerfilScreen(pendientesSinSubir: Int, onAcercaDe: () -> Unit = {}, vm: PerfilViewModel = hiltViewModel()) {
    val s by vm.session.session.collectAsStateWithLifecycle()
    val favoritos by vm.session.favoritos.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }
    var servidor by remember { mutableStateOf(false) }
    var urlActual by remember { mutableStateOf(vm.session.baseUrl) }
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = AuraBg, snackbarHost = { ToastHost(host) }, topBar = { AuraTopBar("Perfil", "Tu cuenta en Aura Eventos") }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AuraCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(s?.nombre ?: "", 60.dp, AuraNavy, Color.White)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(s?.nombre ?: "", style = MaterialTheme.typography.titleMedium)
                        Text(s?.correo ?: "", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                        Spacer(Modifier.height(6.dp))
                        SoftPill(if (s?.esAdmin == true) "ADMINISTRADORA" else "CLIENTE", AuraGreenDark, AuraGreenSoft, icon = if (s?.esAdmin == true) Icons.Filled.Star else Icons.Filled.Person)
                    }
                }
            }
            if (s?.esAdmin != true) AuraCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, null, tint = AuraRed); Spacer(Modifier.width(12.dp))
                    Text("Artículos favoritos", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Text("${favoritos.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
            if (s?.demo == true) {
                AuraBanner(Tipo.AVISO, "Modo demostración", "Los cambios viven solo en la memoria de la app, sin servidor ni base de datos.")
                OutlineButton("Reiniciar datos de la demostración", { vm.reiniciarDemo() }, icon = Icons.Filled.RestartAlt)
            } else {
                OutlineButton("Servidor: $urlActual", { servidor = true }, icon = Icons.Filled.Dns)
            }
            OutlineButton("Acerca de la aplicación", onAcercaDe, icon = Icons.Filled.Info)
            PrimaryButton("Cerrar sesión", { if (pendientesSinSubir > 0) confirmar = true else vm.cerrarSesion() }, icon = Icons.Filled.Logout, color = AuraRed)
        }
    }

    if (servidor) DialogoServidor(urlActual, onGuardar = { vm.session.baseUrl = it; urlActual = vm.session.baseUrl; servidor = false }, onCerrar = { servidor = false })

    // Aviso para no perder evidencias que aún no se suben
    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false }, containerColor = Color.White,
        title = { Text("Tienes evidencias sin subir") },
        text = { Text("Hay $pendientesSinSubir elemento(s) guardados en el teléfono. Si cierras sesión no se subirán hasta que vuelvas a entrar. No se pierden.") },
        confirmButton = { TextButton(onClick = { confirmar = false; vm.cerrarSesion() }) { Text("Cerrar sesión", color = AuraRed) } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar", color = AuraMuted) } }
    )
}
