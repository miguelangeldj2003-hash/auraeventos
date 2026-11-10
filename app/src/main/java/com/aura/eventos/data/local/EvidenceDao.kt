package com.aura.eventos.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Insert suspend fun insert(e: EvidenceEntity)
    @Update suspend fun update(e: EvidenceEntity)

    @Query("SELECT * FROM evidences ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidences WHERE status IN ('PENDING','FAILED') ORDER BY capturedAt")
    suspend fun getQueue(): List<EvidenceEntity>

    // Si la app se cerró a media subida, esos elementos vuelven a la cola
    @Query("UPDATE evidences SET status='PENDING' WHERE status='UPLOADING'")
    suspend fun resetStuck()

    @Query("UPDATE evidences SET status='PENDING', attempts=0, lastError=NULL WHERE status='FAILED'")
    suspend fun requeueFailed()
}
