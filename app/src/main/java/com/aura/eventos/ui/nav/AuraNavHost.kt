package com.aura.eventos.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aura.eventos.data.local.Session
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.local.SyncStatus
import com.aura.eventos.sync.SyncViewModel
import com.aura.eventos.ui.acerca.AcercaDeScreen
import com.aura.eventos.ui.admin.ArticuloFormScreen
import com.aura.eventos.ui.admin.InventarioScreen
import com.aura.eventos.ui.admin.PanelScreen
import com.aura.eventos.ui.auth.LoginScreen
import com.aura.eventos.ui.auth.RegistroScreen
import com.aura.eventos.ui.catalogo.ArticuloDetalleScreen
import com.aura.eventos.ui.catalogo.CarritoScreen
import com.aura.eventos.ui.catalogo.CatalogoScreen
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.deliveries.DeliveriesScreen
import com.aura.eventos.ui.evidence.EvidenceCaptureScreen
import com.aura.eventos.ui.inicio.InicioScreen
import com.aura.eventos.ui.perfil.PerfilScreen
import com.aura.eventos.ui.reserva.ExitoScreen
import com.aura.eventos.ui.reserva.ReservaScreen
import com.aura.eventos.ui.solicitudes.AgendaScreen
import com.aura.eventos.ui.solicitudes.SolicitudDetalleScreen
import com.aura.eventos.ui.solicitudes.SolicitudesScreen
import com.aura.eventos.ui.splash.SplashScreen
import com.aura.eventos.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val CATALOGO = "catalogo"
    const val SOLICITUDES = "solicitudes"
    const val PERFIL = "perfil"
    const val ACERCA_DE = "acerca_de"
    const val CARRITO = "carrito"
    const val RESERVA = "reserva"
    const val EXITO = "exito/{id}"
    const val ARTICULO = "articulo/{id}"
    const val SOLICITUD = "solicitud/{id}"
    const val PANEL = "panel"
    const val AGENDA = "agenda"
    const val INVENTARIO = "inventario"
    const val ARTICULO_FORM = "articulo-form/{id}"
    const val ENTREGAS = "entregas"
    const val CAPTURE = "capture/{folio}"
    fun exito(id: Long) = "exito/$id"
    fun articulo(id: Long) = "articulo/$id"
    fun solicitud(id: Long) = "solicitud/$id"
    fun articuloForm(id: Long) = "articulo-form/$id"
    fun capture(folio: String) = "capture/$folio"
}

private data class Tab(val ruta: String, val titulo: String, val icono: ImageVector)

private val tabsCliente = listOf(
    Tab(Routes.INICIO, "Inicio", Icons.Filled.Home),
    Tab(Routes.CATALOGO, "Catálogo", Icons.Filled.Storefront),
    Tab(Routes.SOLICITUDES, "Solicitudes", Icons.Filled.ReceiptLong),
    Tab(Routes.PERFIL, "Perfil", Icons.Filled.Person)
)
private val tabsAdmin = listOf(
    Tab(Routes.PANEL, "Panel", Icons.Filled.Dashboard),
    Tab(Routes.AGENDA, "Agenda", Icons.Filled.CalendarMonth),
    Tab(Routes.INVENTARIO, "Inventario", Icons.Filled.Inventory2),
    Tab(Routes.ENTREGAS, "Entregas", Icons.Filled.LocalShipping),
    Tab(Routes.PERFIL, "Perfil", Icons.Filled.Person)
)

@HiltViewModel
class RootViewModel @Inject constructor(private val sessionManager: SessionManager) : ViewModel() {
    val session = sessionManager.session
    val aviso = sessionManager.aviso
    fun consumirAviso() = sessionManager.consumirAviso()
}

private fun inicio(s: Session?) = when {
    s == null -> Routes.LOGIN
    s.esAdmin -> Routes.PANEL
    else -> Routes.INICIO
}

private fun NavHostController.irTab(ruta: String) = navigate(ruta) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AuraNavHost(syncVm: SyncViewModel = hiltViewModel(), rootVm: RootViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val session by rootVm.session.collectAsStateWithLifecycle()
    val aviso by rootVm.aviso.collectAsStateWithLifecycle()
    val sync by syncVm.uiState.collectAsStateWithLifecycle()
    val items by syncVm.items.collectAsStateWithLifecycle()
    val ultima by syncVm.lastSyncedAt.collectAsStateWithLifecycle()
    var verSync by remember { mutableStateOf(false) }
    val host = remember { SnackbarHostState() }

    // Al iniciar o cerrar sesión se manda a la pantalla correcta
    val inicial = remember { inicio(session) }
    var ultimoToken by remember { mutableStateOf(session?.token) }
    LaunchedEffect(session?.token) {
        if (session?.token != ultimoToken) {
            ultimoToken = session?.token
            nav.navigate(inicio(session)) { popUpTo(0) { inclusive = true } }
        }
    }
    // Aviso de bienvenida (se muestra una sola vez)
    LaunchedEffect(aviso) {
        aviso?.let {
            val (titulo, detalle) = it.split("|").let { p -> p[0] to p.getOrNull(1) }
            rootVm.consumirAviso()
            host.toast(Tipo.EXITO, titulo, detalle)
        }
    }

    val esAdmin = session?.esAdmin == true
    val tabs = if (esAdmin) tabsAdmin else tabsCliente
    val ruta = nav.currentBackStackEntryAsState().value?.destination?.route
    val enTab = session != null && tabs.any { it.ruta == ruta }
    val sinSubir = items.count { it.status != SyncStatus.SYNCED }

    Scaffold(containerColor = AuraBg, snackbarHost = { ToastHost(host, Modifier.statusBarsPadding()) }, bottomBar = {
        Column {
            if (session?.demo == true) Surface(color = AuraNavy) {
                Text("MODO DEMOSTRACIÓN · datos de ejemplo, sin servidor", color = AuraGold, fontWeight = FontWeight.Bold, fontSize = 10.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
            }
            // La barra de sincronización solo aplica al equipo que sube evidencias
            if (esAdmin) SyncBar(state = sync, onClick = { verSync = true })
            if (enTab) {
                HorizontalDivider(color = AuraBorder)
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = ruta == t.ruta, onClick = { nav.irTab(t.ruta) },
                            icon = { Icon(t.icono, t.titulo) }, label = { Text(t.titulo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AuraGreenDark, selectedTextColor = AuraGreenDark, indicatorColor = AuraGreenSoft,
                                unselectedIconColor = AuraMuted, unselectedTextColor = AuraMuted
                            )
                        )
                    }
                }
            }
        }
    }) { pad ->
        val idArg = listOf(navArgument("id") { type = NavType.LongType })
        NavHost(nav, startDestination = Routes.SPLASH, modifier = Modifier.padding(pad)) {
            // ---- splash ----
            composable(Routes.SPLASH) {
                SplashScreen(onNavigateNext = {
                    nav.navigate(inicial) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }

            // ---- acceso ----
            composable(Routes.LOGIN) { LoginScreen(onRegistro = { nav.navigate(Routes.REGISTRO) }) }
            composable(Routes.REGISTRO) { RegistroScreen(onBack = { nav.popBackStack() }, onLogin = { nav.popBackStack() }) }

            // ---- cliente ----
            composable(Routes.INICIO) {
                InicioScreen(onDetalle = { nav.navigate(Routes.solicitud(it)) }, onSolicitudes = { nav.irTab(Routes.SOLICITUDES) }, onCatalogo = { nav.irTab(Routes.CATALOGO) })
            }
            composable(Routes.CATALOGO) { CatalogoScreen(onArticulo = { nav.navigate(Routes.articulo(it)) }, onCarrito = { nav.navigate(Routes.CARRITO) }) }
            composable(Routes.ARTICULO, idArg) { ArticuloDetalleScreen(onBack = { nav.popBackStack() }, onCarrito = { nav.navigate(Routes.CARRITO) }) }
            composable(Routes.CARRITO) {
                CarritoScreen(onBack = { nav.popBackStack() }, onContinuar = { nav.navigate(Routes.RESERVA) }, onCatalogo = { nav.irTab(Routes.CATALOGO) })
            }
            composable(Routes.RESERVA) {
                ReservaScreen(onBack = { nav.popBackStack() }, onCreada = { id ->
                    nav.navigate(Routes.exito(id)) { popUpTo(Routes.CARRITO) { inclusive = true } }
                })
            }
            composable(Routes.EXITO, idArg) {
                ExitoScreen(onInicio = { nav.irTab(Routes.INICIO) }, onCatalogo = { nav.irTab(Routes.CATALOGO) })
            }
            composable(Routes.SOLICITUDES) { SolicitudesScreen(onDetalle = { nav.navigate(Routes.solicitud(it)) }, onCatalogo = { nav.irTab(Routes.CATALOGO) }) }
            composable(Routes.SOLICITUD, idArg) { SolicitudDetalleScreen(onBack = { nav.popBackStack() }) }

            // ---- administradora / equipo de entrega ----
            composable(Routes.PANEL) { PanelScreen(onDetalle = { nav.navigate(Routes.solicitud(it)) }, onAgenda = { nav.irTab(Routes.AGENDA) }) }
            composable(Routes.AGENDA) { AgendaScreen(onDetalle = { nav.navigate(Routes.solicitud(it)) }) }
            composable(Routes.INVENTARIO) { InventarioScreen(onEditar = { nav.navigate(Routes.articuloForm(it)) }) }
            composable(Routes.ARTICULO_FORM, idArg) { ArticuloFormScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ENTREGAS) { DeliveriesScreen(onOpen = { nav.navigate(Routes.capture(it)) }) }
            composable(Routes.CAPTURE) { EvidenceCaptureScreen(onBack = { nav.popBackStack() }) }

            composable(Routes.PERFIL) {
                PerfilScreen(pendientesSinSubir = sinSubir, onAcercaDe = { nav.navigate(Routes.ACERCA_DE) })
            }
            composable(Routes.ACERCA_DE) {
                AcercaDeScreen(onBack = { nav.popBackStack() })
            }
        }
    }
    if (verSync) SyncDetailSheet(items, ultima, onRetry = syncVm::retryAll, onDismiss = { verSync = false })
}
