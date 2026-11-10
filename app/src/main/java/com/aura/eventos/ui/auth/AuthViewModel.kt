package com.aura.eventos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.repository.AuraRepository
import com.aura.eventos.data.repository.CuentasDemo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUi(
    val correo: String = "", val password: String = "",
    val nombre: String = "", val apellidos: String = "", val telefono: String = "", val confirmar: String = "",
    val acepta: Boolean = false, val loading: Boolean = false, val error: String? = null
) {
    /** 0 a 4 según largo, mayúscula, número y símbolo. */
    val fuerza: Int get() = listOf(password.length >= 8, password.any { it.isUpperCase() }, password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() }).count { it }
    val correoValido: Boolean get() = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(correo.trim())
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuraRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUi())
    val ui: StateFlow<AuthUi> = _ui.asStateFlow()

    val servidor: String get() = session.baseUrl
    fun cambiarServidor(url: String) { session.baseUrl = url }

    fun correo(v: String) = _ui.update { it.copy(correo = v, error = null) }
    fun password(v: String) = _ui.update { it.copy(password = v, error = null) }
    fun nombre(v: String) = _ui.update { it.copy(nombre = v, error = null) }
    fun apellidos(v: String) = _ui.update { it.copy(apellidos = v, error = null) }
    fun telefono(v: String) = _ui.update { it.copy(telefono = v.filter { c -> c.isDigit() || c == ' ' }, error = null) }
    fun confirmar(v: String) = _ui.update { it.copy(confirmar = v, error = null) }
    fun acepta(v: Boolean) = _ui.update { it.copy(acepta = v, error = null) }

    /** Rellena el formulario con una de las cuentas de prueba. */
    fun usarCuenta(admin: Boolean) = _ui.update {
        if (admin) it.copy(correo = CuentasDemo.ADMIN_CORREO, password = CuentasDemo.ADMIN_PASSWORD, error = null)
        else it.copy(correo = CuentasDemo.CLIENTE_CORREO, password = CuentasDemo.CLIENTE_PASSWORD, error = null)
    }

    fun iniciarSesion() {
        val s = _ui.value
        val problema = when {
            !s.correoValido -> "Escribe un correo válido"
            s.password.isBlank() -> "Escribe tu contraseña"
            else -> null
        }
        if (problema != null) { _ui.update { it.copy(error = problema) }; return }
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = repo.login(s.correo, s.password)
            // Si sale bien la sesión queda guardada y la navegación cambia sola
            _ui.update { it.copy(loading = false, error = r.exceptionOrNull()?.message) }
        }
    }

    fun registrar() {
        val s = _ui.value
        val problema = when {
            s.nombre.isBlank() -> "Escribe tu nombre"
            s.apellidos.isBlank() -> "Escribe tus apellidos"
            !s.correoValido -> "Escribe un correo válido"
            s.telefono.filter { it.isDigit() }.length < 10 -> "El teléfono debe tener 10 dígitos"
            s.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            s.fuerza < 3 -> "Usa una contraseña más fuerte: mayúscula, número o símbolo"
            s.password != s.confirmar -> "Las contraseñas no coinciden"
            !s.acepta -> "Debes aceptar los Términos de Servicio y la Política de Privacidad"
            else -> null
        }
        if (problema != null) { _ui.update { it.copy(error = problema) }; return }
        _ui.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val r = repo.registro("${s.nombre.trim()} ${s.apellidos.trim()}", s.correo, s.telefono.filter { it.isDigit() }, s.password)
            _ui.update { it.copy(loading = false, error = r.exceptionOrNull()?.message) }
        }
    }
}
