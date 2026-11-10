package com.aura.eventos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EvidenceEntity::class], version = 1, exportSchema = false)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
}
