package com.aura.eventos.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.aura.eventos.data.repository.EvidenceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun enqueue(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        // Se ejecuta solo cuando hay internet, aunque la app esté cerrada
        WorkManager.getInstance(ctx).enqueueUniqueWork("aura_sync", ExistingWorkPolicy.APPEND_OR_REPLACE, req)
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: EvidenceRepository
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        if (repo.uploadQueue()) Result.success() else Result.retry()
}
