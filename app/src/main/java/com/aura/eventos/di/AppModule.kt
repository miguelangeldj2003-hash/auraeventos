package com.aura.eventos.di

import android.content.Context
import androidx.room.Room
import com.aura.eventos.data.local.AuraDatabase
import com.aura.eventos.data.local.EvidenceDao
import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.demo.FakeAuraApi
import com.aura.eventos.data.demo.FakeEvidenceApi
import com.aura.eventos.data.remote.AuraApi
import com.aura.eventos.data.remote.AuraApiRouter
import com.aura.eventos.data.remote.EvidenceApiRouter
import com.aura.eventos.data.remote.AuthInterceptor
import com.aura.eventos.data.remote.BaseUrlInterceptor
import com.aura.eventos.data.remote.EvidenceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context): AuraDatabase =
        Room.databaseBuilder(c, AuraDatabase::class.java, "aura.db").build()

    @Provides fun dao(db: AuraDatabase): EvidenceDao = db.evidenceDao()

    // Tiempos largos: si el servidor estaba dormido tarda en responder la primera vez
    @Provides @Singleton
    fun okHttp(session: SessionManager): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(BaseUrlInterceptor(session))
        .addInterceptor(AuthInterceptor(session))
        .build()

    // La URL real la pone BaseUrlInterceptor
    @Provides @Singleton
    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/").client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()

    // Cada API decide sola si usa el servidor real o los datos de demostración
    @Provides @Singleton
    fun evidenceApi(r: Retrofit, demo: FakeEvidenceApi, session: SessionManager): EvidenceApi =
        EvidenceApiRouter(r.create(EvidenceApi::class.java), demo, session)

    @Provides @Singleton
    fun auraApi(r: Retrofit, demo: FakeAuraApi, session: SessionManager): AuraApi =
        AuraApiRouter(r.create(AuraApi::class.java), demo, session)
}
