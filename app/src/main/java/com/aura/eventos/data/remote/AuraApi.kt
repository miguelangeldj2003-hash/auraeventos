package com.aura.eventos.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// ---------- modelos (mismos nombres que los DTOs de la API Java) ----------
data class UsuarioDto(val id: Long, val nombre: String, val correo: String, val telefono: String?, val rol: String)
data class AuthResponse(val token: String, val usuario: UsuarioDto)
data class RegistroRequest(val nombre: String, val correo: String, val password: String, val telefono: String?)
data class LoginRequest(val correo: String, val password: String)

data class CategoriaDto(val id: Long, val nombre: String, val seccion: String)

data class ArticuloDto(
    val id: Long, val categoriaId: Long, val categoria: String, val seccion: String,
    val nombre: String, val descripcion: String?, val precioBase: Double, val stockTotal: Int,
    val disponibles: Int?, val imagenUrl: String?, val activo: Boolean,
    val sku: String?, val dimensiones: String?, val material: String?, val incluye: String?,
    val pesoMax: String?, val enMantenimiento: Boolean, val rentas: Int
)

data class ArticuloRequest(
    val categoriaId: Long, val nombre: String, val descripcion: String?, val precioBase: Double,
    val stockTotal: Int, val imagenUrl: String?, val sku: String?, val dimensiones: String?,
    val material: String?, val incluye: String?, val pesoMax: String?, val enMantenimiento: Boolean
)

data class ItemRequest(val articuloId: Long, val cantidad: Int)

data class ReservacionRequest(
    val fechaEvento: String, val fechaFin: String?, val direccion: String, val entorno: String,
    val notas: String?, val latitud: Double?, val longitud: Double?, val items: List<ItemRequest>
)

data class DetalleDto(
    val articuloId: Long, val sku: String?, val nombre: String, val cantidad: Int,
    val precioUnitario: Double, val subtotal: Double
)

data class ReservacionDto(
    val id: Long, val folio: String, val clienteId: Long, val cliente: String, val correo: String?,
    val telefono: String?, val fechaEvento: String, val fechaFin: String, val dias: Int,
    val direccion: String, val entorno: String, val notas: String?,
    val latitud: Double?, val longitud: Double?, val estado: String, val motivoRechazo: String?,
    val creadoEn: String?, val costoFlete: Double, val subtotal: Double, val iva: Double,
    val total: Double, val detalles: List<DetalleDto>
)

data class AprobacionRequest(val costoFlete: Double?)
data class RechazoRequest(val motivo: String?)

data class MesIngreso(val mes: String, val total: Double)
data class TopArticulo(val nombre: String, val cantidad: Int, val porcentaje: Int)
data class DashboardDto(
    val pendientes: Int, val aprobados: Int, val ingresosMes: Double, val ingresosTotales: Double,
    val variacionMes: Int, val ocupacionMes: Int, val ingresosMensuales: List<MesIngreso>,
    val topArticulos: List<TopArticulo>, val recientes: List<ReservacionDto>,
    val entregasHoy: List<ReservacionDto>, val proxima: ReservacionDto?
)

interface AuraApi {
    @POST("api/auth/login") suspend fun login(@Body r: LoginRequest): Response<AuthResponse>
    @POST("api/auth/registro") suspend fun registro(@Body r: RegistroRequest): Response<AuthResponse>

    @GET("api/categorias") suspend fun categorias(): Response<List<CategoriaDto>>
    @GET("api/articulos") suspend fun articulos(@Query("desde") desde: String?, @Query("hasta") hasta: String?): Response<List<ArticuloDto>>
    @GET("api/disponibilidad") suspend fun disponibilidad(@Query("items") items: String, @Query("mes") mes: String): Response<List<String>>

    @GET("api/admin/articulos") suspend fun adminArticulos(): Response<List<ArticuloDto>>
    @POST("api/admin/articulos") suspend fun crearArticulo(@Body r: ArticuloRequest): Response<ArticuloDto>
    @PUT("api/admin/articulos/{id}") suspend fun actualizarArticulo(@Path("id") id: Long, @Body r: ArticuloRequest): Response<ArticuloDto>
    @PATCH("api/admin/articulos/{id}/baja") suspend fun baja(@Path("id") id: Long): Response<ArticuloDto>
    @PATCH("api/admin/articulos/{id}/alta") suspend fun alta(@Path("id") id: Long): Response<ArticuloDto>

    @POST("api/reservaciones") suspend fun crearReservacion(@Body r: ReservacionRequest): Response<ReservacionDto>
    @GET("api/reservaciones") suspend fun reservaciones(@Query("estado") estado: String?): Response<List<ReservacionDto>>
    @GET("api/reservaciones/{id}") suspend fun reservacion(@Path("id") id: Long): Response<ReservacionDto>
    @POST("api/reservaciones/{id}/aprobar") suspend fun aprobar(@Path("id") id: Long, @Body r: AprobacionRequest): Response<ReservacionDto>
    @POST("api/reservaciones/{id}/rechazar") suspend fun rechazar(@Path("id") id: Long, @Body r: RechazoRequest): Response<ReservacionDto>
    @Streaming @GET("api/reservaciones/{id}/nota-remision") suspend fun nota(@Path("id") id: Long): Response<ResponseBody>

    @GET("api/admin/dashboard") suspend fun dashboard(): Response<DashboardDto>
}
