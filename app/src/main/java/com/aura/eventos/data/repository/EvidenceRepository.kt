package com.aura.eventos.data.repository

import android.content.Context
import com.aura.eventos.data.local.*
import com.aura.eventos.data.remote.EvidenceApi
import com.aura.eventos.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceRepository @Inject constructor(
    private val dao: EvidenceDao,
    private val api: EvidenceApi,
    @ApplicationContext private val ctx: Context
) {
    companion object { const val MAX_ATTEMPTS = 5 }

    fun observeAll(): Flow<List<EvidenceEntity>> = dao.observeAll()

    /** Siempre guarda primero en el teléfono; la subida ocurre en segundo plano. */
    suspend fun saveLocal(e: EvidenceEntity) {
        dao.insert(e)
        SyncScheduler.enqueue(ctx)
    }

    suspend fun resetStuck() = dao.resetStuck()
    suspend fun requeueFailed() = dao.requeueFailed()

    /** @return true si toda la cola se subió (o no hay nada que subir). */
    suspend fun uploadQueue(): Boolean {
        var allOk = true
        for (item in dao.getQueue()) {
            if (item.attempts >= MAX_ATTEMPTS) { allOk = false; continue }
            dao.update(item.copy(status = SyncStatus.UPLOADING))
            try {
                val file = File(item.photoPath)
                val photo = MultipartBody.Part.createFormData(
                    "foto", file.name, file.asRequestBody("image/jpeg".toMediaType())
                )
                fun txt(s: String) = s.toRequestBody("text/plain".toMediaType())
                val res = api.upload(
                    photo, txt(item.id), txt(item.eventFolio), txt(item.type.name), txt(item.comment),
                    txt(item.latitude?.toString() ?: ""), txt(item.longitude?.toString() ?: ""),
                    txt(item.accuracyMeters?.toString() ?: ""), txt(item.capturedAt.toString())
                )
                if (res.isSuccessful) {
                    dao.update(item.copy(status = SyncStatus.SYNCED, syncedAt = System.currentTimeMillis(), lastError = null))
                } else {
                    allOk = false
                    dao.update(item.copy(status = SyncStatus.FAILED, attempts = item.attempts + 1, lastError = "Servidor respondió ${res.code()}"))
                }
            } catch (e: IOException) {
                // Sin red / red inestable: vuelve a la cola sin contar como intento fallido
                allOk = false
                dao.update(item.copy(status = SyncStatus.PENDING))
            } catch (e: Exception) {
                allOk = false
                dao.update(item.copy(status = SyncStatus.FAILED, attempts = item.attempts + 1, lastError = e.message))
            }
        }
        return allOk
    }
}
