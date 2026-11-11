package com.aura.eventos.data.repository

import android.content.Context
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.remote.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

class ApiError(message: String) : Exception(message)

/** Cuentas de prueba para presentar la app sin servidor (se muestran abajo en el login). */
object CuentasDemo {
    const val CLIENTE_CORREO = "maria@gmail.com"
    const val CLIENTE_PASSWORD = "Maria123*"
    const val ADMIN_CORREO = "ana@aura.com"
    const val ADMIN_PASSWORD = "Admin123!"
}

@Singleton
class AuraRepository @Inject constructor(
    private val api: AuraApi,
    private val session: SessionManager,
    @ApplicationContext private val ctx: Context
) {
    // Convierte cualquier respuesta/fallo en un Result con un mensaje entendible para el usuario
    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Result<T> = try {
        val r = bloque()
        val cuerpo = r.body()
        if (r.isSuccessful && cuerpo != null) Result.success(cuerpo) else Result.failure(ApiError(mensajeError(r)))
    } catch (e: IOException) {
        Result.failure(ApiError("No hay conexión con el servidor. Revisa tu internet o la dirección del servidor."))
    } catch (e: Exception) {
        Result.failure(ApiError(e.message ?: "Error inesperado"))
    }

    private fun mensajeError(r: Response<*>): String = when (r.code()) {
        401 -> "Tu sesión venció, inicia sesión de nuevo"
        403 -> "No tienes permiso para hacer esto"
        else -> try {
            JSONObject(r.errorBody()?.string() ?: "").getString("error")
        } catch (e: Exception) { "Error del servidor (${r.code()})" }
    }

    // ---------- sesión ----------
    suspend fun login(correo: String, password: String): Result<Unit> {
        val c = correo.trim().lowercase()
        // Las cuentas de prueba funcionan sin servidor (modo demostración)
        if (c == CuentasDemo.CLIENTE_CORREO && password == CuentasDemo.CLIENTE_PASSWORD) { session.entrarDemo(false); return Result.success(Unit) }
        if (c == CuentasDemo.ADMIN_CORREO && password == CuentasDemo.ADMIN_PASSWORD) { session.entrarDemo(true); return Result.success(Unit) }
        return llamar { api.login(LoginRequest(c, password)) }.map { session.guardar(it) }
    }

    /** @return true si la cuenta se creó solo en modo demostración (no había servidor). */
    suspend fun registro(nombre: String, correo: String, telefono: String, password: String): Result<Boolean> {
        val r = llamar { api.registro(RegistroRequest(nombre.trim(), correo.trim(), password, telefono.ifBlank { null })) }
        r.onSuccess { session.guardar(it, registro = true); return Result.success(false) }
        // Sin servidor: se crea una cuenta de demostración para poder recorrer la app
        if (r.exceptionOrNull()?.message?.startsWith("No hay conexión") == true) {
            session.entrarDemo(false, nombre.trim(), correo.trim(), 8L, registro = true)
            return Result.success(true)
        }
        return Result.failure(r.exceptionOrNull() ?: ApiError("No se pudo crear la cuenta"))
    }

    fun cerrarSesion() = session.cerrar()

    // ---------- catálogo ----------
    private val articulosCache = mutableMapOf<Long, ArticuloDto>()
    fun articuloEnCache(id: Long): ArticuloDto? = articulosCache[id]

    suspend fun categorias() = llamar { api.categorias() }

    suspend fun articulos(desde: String? = null, hasta: String? = null) =
        llamar { api.articulos(desde, hasta) }.onSuccess { l -> l.forEach { articulosCache[it.id] = it } }

    suspend fun articulo(id: Long): ArticuloDto? = articulosCache[id] ?: articulos().getOrNull()?.firstOrNull { it.id == id }

    /** Días del mes en que el carrito no se puede entregar (se pintan en gris en el calendario). */
    suspend fun fechasOcupadas(items: List<CartItem>, mes: YearMonth): Result<Set<String>> {
        if (items.isEmpty()) return Result.success(emptySet())
        val param = items.joinToString(",") { "${it.articulo.id}:${it.cantidad}" }
        return llamar { api.disponibilidad(param, mes.toString()) }.map { it.toSet() }
    }

    // ---------- inventario (admin) ----------
    suspend fun adminArticulos() = llamar { api.adminArticulos() }.onSuccess { l -> l.forEach { articulosCache[it.id] = it } }
    suspend fun crearArticulo(r: ArticuloRequest) = llamar { api.crearArticulo(r) }
    suspend fun actualizarArticulo(id: Long, r: ArticuloRequest) = llamar { api.actualizarArticulo(id, r) }
    suspend fun subirImagenArticulo(id: Long, fotoFile: File): Result<ArticuloDto> = withContext(Dispatchers.IO) {
        try {
            val reqFile = fotoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("foto", fotoFile.name, reqFile)
            llamar { api.subirImagenArticulo(id, part) }
        } catch (e: Exception) {
            Result.failure(ApiError(e.message ?: "No se pudo subir la foto del artículo"))
        }
    }
    suspend fun cambiarEstado(id: Long, activo: Boolean) = llamar { if (activo) api.alta(id) else api.baja(id) }

    // ---------- reservaciones ----------
    suspend fun crearReservacion(r: ReservacionRequest) = llamar { api.crearReservacion(r) }
    suspend fun reservaciones(estado: String? = null) = llamar { api.reservaciones(estado) }
    suspend fun reservacion(id: Long) = llamar { api.reservacion(id) }
    suspend fun aprobar(id: Long, flete: Double?) = llamar { api.aprobar(id, AprobacionRequest(flete)) }
    suspend fun rechazar(id: Long, motivo: String?) = llamar { api.rechazar(id, RechazoRequest(motivo)) }
    suspend fun dashboard() = llamar { api.dashboard() }

    /** Descarga la Nota de Remisión (PDF) a la caché de la app para poder abrirla. */
    suspend fun descargarNota(id: Long, folio: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val r = api.nota(id)
            val cuerpo = r.body()
            if (!r.isSuccessful || cuerpo == null) return@withContext Result.failure(ApiError(mensajeError(r)))
            val carpeta = File(ctx.cacheDir, "notas").apply { mkdirs() }
            val archivo = File(carpeta, "nota-$folio.pdf")
            cuerpo.byteStream().use { entrada -> archivo.outputStream().use { salida -> entrada.copyTo(salida) } }
            Result.success(archivo)
        } catch (e: IOException) {
            Result.failure(ApiError("No se pudo descargar la nota. Revisa tu conexión."))
        }
    }
}
