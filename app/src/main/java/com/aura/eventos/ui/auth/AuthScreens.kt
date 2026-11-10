package com.aura.eventos.ui.auth

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.eventos.data.repository.CuentasDemo
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*

/** Encabezado azul con la frase de la web (compacto para el celular). */
@Composable
private fun AuthHero(
    etiqueta: String, titulo: String, stats: List<Pair<String, String>>,
    onBack: (() -> Unit)? = null, onAjustes: (() -> Unit)? = null
) {
    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(AuraNavy, Color(0xFF2C3E57), Color(0xFF7C8796))))) {
        Column(Modifier.padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 56.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))) {
                        Icon(Icons.Filled.ArrowBack, "Regresar", tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                }
                AuraLogo(onDark = true, modifier = Modifier.weight(1f))
                if (onAjustes != null) IconButton(onClick = onAjustes) { Icon(Icons.Filled.Settings, "Servidor", tint = Color.White.copy(alpha = 0.7f)) }
            }
            Spacer(Modifier.height(28.dp))
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = AuraGold)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.width(36.dp).height(3.dp).background(AuraGreen))
            Spacer(Modifier.height(14.dp))
            Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                stats.forEach { (n, t) ->
                    Column {
                        Text(n, style = MaterialTheme.typography.titleLarge, color = AuraGreen)
                        Text(t, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TabsAcceso(registro: Boolean, onLogin: () -> Unit, onRegistro: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = AuraBg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(4.dp)) {
            listOf("Iniciar Sesión" to !registro, "Registrarse" to registro).forEachIndexed { i, (t, activo) ->
                Surface(
                    onClick = if (i == 0) onLogin else onRegistro, shape = RoundedCornerShape(50), modifier = Modifier.weight(1f),
                    color = if (activo) Color.White else Color.Transparent, shadowElevation = if (activo) 1.dp else 0.dp
                ) {
                    Text(t, Modifier.padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge, color = if (activo) AuraNavy else AuraMuted)
                }
            }
        }
    }
}

// ============================================================
//  Iniciar sesión
// ============================================================
@Composable
fun LoginScreen(onRegistro: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var ajustes by remember { mutableStateOf(false) }
    var olvido by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(AuraBg).verticalScroll(rememberScrollState())) {
        AuthHero(
            etiqueta = "PLATAFORMA DE GESTIÓN DE EVENTOS", titulo = "Renta de eventos, organizada con precisión.",
            stats = listOf("+500" to "Artículos", "+120" to "Eventos al mes", "99%" to "Satisfacción"),
            onAjustes = { ajustes = true }
        )
        AuraCard(Modifier.padding(horizontal = 16.dp).offset(y = (-36).dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                TabsAcceso(registro = false, onLogin = {}, onRegistro = onRegistro)
                LabeledField("Correo electrónico", ui.correo, vm::correo, placeholder = "tucorreo@gmail.com",
                    keyboardType = KeyboardType.Email, leading = Icons.Filled.MailOutline)
                Column {
                    LabeledField("Contraseña", ui.password, vm::password, placeholder = "Tu contraseña", password = true, leading = Icons.Filled.LockOpen)
                    Text("¿Olvidaste tu contraseña?", color = AuraGreen, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp).clickable { olvido = true })
                }
                ui.error?.let { AuraBanner(Tipo.ERROR, it) }
                PrimaryButton("Iniciar Sesión", vm::iniciarSesion, loading = ui.loading)
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Text("¿No tienes cuenta? ", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                    Text("Regístrate gratis", style = MaterialTheme.typography.bodySmall, color = AuraGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onRegistro))
                }
            }
        }
        // Cuentas de prueba (para ver la app sin internet ni servidor)
        CuentasDePrueba(onCliente = { vm.usarCuenta(false) }, onAdmin = { vm.usarCuenta(true) },
            modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-20).dp))
        Spacer(Modifier.height(8.dp))
    }

    if (ajustes) DialogoServidor(vm.servidor, onGuardar = { vm.cambiarServidor(it); ajustes = false }, onCerrar = { ajustes = false })
    if (olvido) AlertDialog(
        onDismissRequest = { olvido = false }, containerColor = Color.White,
        title = { Text("Restablecer contraseña") },
        text = { Text("Por seguridad, pide a la administradora de Aura Eventos que restablezca tu contraseña.") },
        confirmButton = { TextButton(onClick = { olvido = false }) { Text("Entendido", color = AuraGreen) } }
    )
}

@Composable
private fun CuentasDePrueba(onCliente: () -> Unit, onAdmin: () -> Unit, modifier: Modifier = Modifier) {
    AuraCard(modifier, color = AuraGoldSoft, borderColor = AuraGold.copy(alpha = 0.4f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Key, null, tint = AuraGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                SectionLabel("Cuentas de prueba")
            }
            Text("Toca una para rellenar el formulario. Funcionan sin internet.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
            FilaCuenta("Cliente", CuentasDemo.CLIENTE_CORREO, CuentasDemo.CLIENTE_PASSWORD, Icons.Filled.Person, onCliente)
            FilaCuenta("Administradora", CuentasDemo.ADMIN_CORREO, CuentasDemo.ADMIN_PASSWORD, Icons.Filled.AdminPanelSettings, onAdmin)
        }
    }
}

@Composable
private fun FilaCuenta(rol: String, correo: String, password: String, icono: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorder)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(AuraGreenSoft), contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = AuraGreenDark, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(rol, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(correo, style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                Text("Contraseña: $password", style = MaterialTheme.typography.bodySmall, color = AuraNavy, fontWeight = FontWeight.SemiBold)
            }
            Text("Usar", color = AuraGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
//  Crear cuenta
// ============================================================
@Composable
fun RegistroScreen(onBack: () -> Unit, onLogin: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(AuraBg).verticalScroll(rememberScrollState())) {
        AuthHero(
            etiqueta = "ÚNETE A AURA EVENTOS", titulo = "Crea tu cuenta y empieza a reservar.",
            stats = listOf("Gratis" to "Sin costo", "<24h" to "Confirmación", "100%" to "Seguro"), onBack = onBack
        )
        AuraCard(Modifier.padding(horizontal = 16.dp).offset(y = (-36).dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Crear cuenta nueva", style = MaterialTheme.typography.titleLarge)
                Text("Completa tus datos para comenzar. Es rápido y gratuito.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledField("Nombre(s)", ui.nombre, vm::nombre, Modifier.weight(1f), placeholder = "María")
                    LabeledField("Apellidos", ui.apellidos, vm::apellidos, Modifier.weight(1f), placeholder = "García López")
                }
                LabeledField("Correo electrónico", ui.correo, vm::correo, placeholder = "maria@gmail.com", keyboardType = KeyboardType.Email,
                    leading = Icons.Filled.MailOutline, trailingText = if (ui.correoValido) "✓" else null)
                LabeledField("Teléfono de contacto", ui.telefono, vm::telefono, placeholder = "55 9876 5432",
                    keyboardType = KeyboardType.Phone, leading = Icons.Filled.Phone, trailingText = "+52")
                LabeledField("Contraseña", ui.password, vm::password, placeholder = "Mínimo 8 caracteres", password = true)
                if (ui.password.isNotEmpty()) {
                    val (texto, color) = when (ui.fuerza) { 0, 1 -> "Contraseña débil" to AuraRed; 2 -> "Contraseña media" to AuraAmber; else -> "Contraseña fuerte ✓" to AuraGreenDark }
                    LinearProgressIndicator(progress = { ui.fuerza / 4f }, Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = AuraBg)
                    Text(texto, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
                }
                LabeledField("Confirmar contraseña", ui.confirmar, vm::confirmar, placeholder = "Repite tu contraseña", password = true,
                    error = if (ui.confirmar.isNotEmpty() && ui.confirmar != ui.password) "Las contraseñas no coinciden" else null)
                Row(Modifier.clickable { vm.acepta(!ui.acepta) }, verticalAlignment = Alignment.Top) {
                    Checkbox(ui.acepta, { vm.acepta(it) }, colors = CheckboxDefaults.colors(checkedColor = AuraGreen))
                    Text("Acepto los Términos de Servicio y la Política de Privacidad de Aura Eventos.", style = MaterialTheme.typography.bodySmall,
                        color = AuraMuted, modifier = Modifier.padding(top = 12.dp))
                }
                ui.error?.let { AuraBanner(Tipo.ERROR, it) }
                PrimaryButton("Crear mi cuenta gratis", vm::registrar, loading = ui.loading, icon = Icons.Filled.ArrowForward)
                Row(Modifier.align(Alignment.CenterHorizontally)) {
                    Text("¿Ya tienes cuenta? ", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                    Text("Inicia sesión", style = MaterialTheme.typography.bodySmall, color = AuraGreen, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onLogin))
                }
            }
        }
    }
}

/** Para cambiar la dirección del servidor sin recompilar (la IP de AWS cambia al reiniciar el laboratorio). */
@Composable
fun DialogoServidor(actual: String, onGuardar: (String) -> Unit, onCerrar: () -> Unit) {
    var texto by remember { mutableStateOf(actual) }
    AlertDialog(
        onDismissRequest = onCerrar, containerColor = Color.White,
        title = { Text("Dirección del servidor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Solo se usa con el servidor real. Ejemplo: http://54.12.34.56:8080", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                LabeledField("URL", texto, { texto = it }, placeholder = "http://IP:8080")
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(texto) }) { Text("Guardar", color = AuraGreen) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = AuraMuted) } }
    )
}
