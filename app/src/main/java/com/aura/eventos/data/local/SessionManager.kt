package com.aura.eventos.data.local

import android.content.Context
import com.aura.eventos.BuildConfig
import com.aura.eventos.data.remote.AuthResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Session(
    val token: String, val id: Long, val nombre: String, val correo: String, val rol: String,
    val demo: Boolean = false   // true = datos de ejemplo, sin servidor
) {
    val esAdmin: Boolean get() = rol == "ADMIN"
}

/** Guarda la sesión y la dirección del servidor (la IP de AWS cambia seguido, por eso es editable). */
@Singleton
class SessionManager @Inject constructor(@ApplicationContext ctx: Context) {
    private val prefs = ctx.getSharedPreferences("aura_session", Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(leer())
    val session: StateFlow<Session?> = _session.asStateFlow()

    var baseUrl: String
        get() = prefs.getString("base_url", null) ?: BuildConfig.BASE_URL
        set(valor) { prefs.edit().putString("base_url", normalizar(valor)).apply() }

    // Mensaje de bienvenida que la pantalla principal muestra una vez (titulo|detalle)
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()
    fun consumirAviso() { _aviso.value = null }
    private fun bienvenida(nombre: String, registro: Boolean, extra: String? = null) {
        val primero = nombre.trim().split(" ").firstOrNull().orEmpty()
        _aviso.value = if (registro) "Cuenta creada con éxito|Bienvenido a Aura Eventos, $primero." + (extra?.let { " $it" } ?: "")
                       else "Se inició sesión correctamente|Bienvenido de vuelta, $primero"
    }

    fun guardar(auth: AuthResponse, registro: Boolean = false) {
        prefs.edit()
            .putString("token", auth.token).putLong("id", auth.usuario.id)
            .putString("nombre", auth.usuario.nombre).putString("correo", auth.usuario.correo)
            .putString("rol", auth.usuario.rol).putBoolean("demo", false).apply()
        _session.value = leer()
        bienvenida(auth.usuario.nombre, registro)
    }

    /** Entra sin servidor: la app usa datos de ejemplo guardados en memoria. */
    fun entrarDemo(admin: Boolean, nombre: String? = null, correo: String? = null, idUsuario: Long? = null, registro: Boolean = false) {
        prefs.edit()
            .putString("token", "demo").putLong("id", idUsuario ?: if (admin) 1L else 2L)
            .putString("nombre", nombre ?: if (admin) "Ana Martínez" else "María García López")
            .putString("correo", correo ?: if (admin) "ana@aura.com" else "maria@gmail.com")
            .putString("rol", if (admin) "ADMIN" else "CLIENTE").putBoolean("demo", true).apply()
        _session.value = leer()
        bienvenida(_session.value?.nombre ?: "", registro, if (registro) "(modo demostración, sin servidor)" else null)
    }

    // ---------- favoritos del cliente (se guardan en el teléfono) ----------
    private val _favoritos = MutableStateFlow(prefs.getStringSet("favoritos", emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet())
    val favoritos: StateFlow<Set<Long>> = _favoritos.asStateFlow()

    fun alternarFavorito(id: Long): Boolean {
        val nuevo = if (id in _favoritos.value) _favoritos.value - id else _favoritos.value + id
        _favoritos.value = nuevo
        prefs.edit().putStringSet("favoritos", nuevo.map { it.toString() }.toSet()).apply()
        return id in nuevo
    }

    fun cerrar() {
        prefs.edit().remove("token").remove("id").remove("nombre").remove("correo").remove("rol").remove("demo").apply()
        _session.value = null
    }

    private fun leer(): Session? {
        val token = prefs.getString("token", null) ?: return null
        return Session(token, prefs.getLong("id", 0), prefs.getString("nombre", "") ?: "",
            prefs.getString("correo", "") ?: "", prefs.getString("rol", "CLIENTE") ?: "CLIENTE",
            prefs.getBoolean("demo", false))
    }

    private fun normalizar(v: String): String {
        var u = v.trim()
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://$u"
        if (!u.endsWith("/")) u += "/"
        return u
    }
}
