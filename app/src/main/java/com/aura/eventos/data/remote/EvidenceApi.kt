package com.aura.eventos.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** Contrato que debe exponer el backend Java: POST /api/evidencias (multipart). */
interface EvidenceApi {
    @Multipart
    @POST("api/evidencias")
    suspend fun upload(
        @Part photo: MultipartBody.Part,
        @Part("id") id: RequestBody,
        @Part("folio") folio: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("comentario") comentario: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part("precision") precision: RequestBody,
        @Part("capturadoEn") capturadoEn: RequestBody
    ): Response<Unit>
}
