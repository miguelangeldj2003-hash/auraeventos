package com.aura.eventos.data.remote

import com.aura.eventos.data.local.SessionManager
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

/** Decide en cada llamada: si la sesión es de demostración usa datos de ejemplo, si no el servidor real. */
class AuraApiRouter(private val real: AuraApi, private val demo: AuraApi, private val session: SessionManager) : AuraApi {
    private fun api(): AuraApi = if (session.session.value?.demo == true) demo else real

    override suspend fun login(r: LoginRequest): Response<AuthResponse> = api().login(r)
    override suspend fun registro(r: RegistroRequest): Response<AuthResponse> = api().registro(r)
    override suspend fun categorias(): Response<List<CategoriaDto>> = api().categorias()
    override suspend fun articulos(desde: String?, hasta: String?): Response<List<ArticuloDto>> = api().articulos(desde, hasta)
    override suspend fun disponibilidad(items: String, mes: String): Response<List<String>> = api().disponibilidad(items, mes)
    override suspend fun adminArticulos(): Response<List<ArticuloDto>> = api().adminArticulos()
    override suspend fun crearArticulo(r: ArticuloRequest): Response<ArticuloDto> = api().crearArticulo(r)
    override suspend fun actualizarArticulo(id: Long, r: ArticuloRequest): Response<ArticuloDto> = api().actualizarArticulo(id, r)
    override suspend fun subirImagenArticulo(id: Long, foto: MultipartBody.Part): Response<ArticuloDto> = api().subirImagenArticulo(id, foto)
    override suspend fun baja(id: Long): Response<ArticuloDto> = api().baja(id)
    override suspend fun alta(id: Long): Response<ArticuloDto> = api().alta(id)
    override suspend fun crearReservacion(r: ReservacionRequest): Response<ReservacionDto> = api().crearReservacion(r)
    override suspend fun reservaciones(estado: String?): Response<List<ReservacionDto>> = api().reservaciones(estado)
    override suspend fun reservacion(id: Long): Response<ReservacionDto> = api().reservacion(id)
    override suspend fun aprobar(id: Long, r: AprobacionRequest): Response<ReservacionDto> = api().aprobar(id, r)
    override suspend fun rechazar(id: Long, r: RechazoRequest): Response<ReservacionDto> = api().rechazar(id, r)
    override suspend fun nota(id: Long): Response<ResponseBody> = api().nota(id)
    override suspend fun dashboard(): Response<DashboardDto> = api().dashboard()
}

class EvidenceApiRouter(private val real: EvidenceApi, private val demo: EvidenceApi, private val session: SessionManager) : EvidenceApi {
    override suspend fun upload(
        photo: MultipartBody.Part, id: RequestBody, folio: RequestBody, tipo: RequestBody,
        comentario: RequestBody, latitud: RequestBody, longitud: RequestBody,
        precision: RequestBody, capturadoEn: RequestBody
    ): Response<Unit> {
        val api = if (session.session.value?.demo == true) demo else real
        return api.upload(photo, id, folio, tipo, comentario, latitud, longitud, precision, capturadoEn)
    }
}
