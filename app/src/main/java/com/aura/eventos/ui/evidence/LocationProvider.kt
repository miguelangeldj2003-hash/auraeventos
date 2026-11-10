package com.aura.eventos.ui.evidence

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** El GPS funciona sin internet, por eso la ubicación se captura aunque no haya red. */
@Singleton
class LocationProvider @Inject constructor(@ApplicationContext ctx: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(ctx)

    @SuppressLint("MissingPermission")
    suspend fun current(): Location? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
        cont.invokeOnCancellation { cts.cancel() }
    }
}
