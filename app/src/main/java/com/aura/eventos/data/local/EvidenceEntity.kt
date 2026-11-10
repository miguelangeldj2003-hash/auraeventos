package com.aura.eventos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class EvidenceType(val label: String) {
    ENTREGA("Entrega"), RECOLECCION("Recolección"), DANO("Daño"), ANTES("Antes del evento")
}

enum class SyncStatus { PENDING, UPLOADING, SYNCED, FAILED }

@Entity(tableName = "evidences")
data class EvidenceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventFolio: String,
    val type: EvidenceType,
    val photoPath: String,
    val comment: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val capturedAt: Long = System.currentTimeMillis(),
    val status: SyncStatus = SyncStatus.PENDING,
    val syncedAt: Long? = null,
    val attempts: Int = 0,
    val lastError: String? = null
)
