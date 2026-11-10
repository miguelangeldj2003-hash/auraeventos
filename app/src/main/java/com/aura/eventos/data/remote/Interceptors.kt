package com.aura.eventos.data.remote

import com.aura.eventos.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Response

/** Cambia el servidor en cada petición según lo guardado en Perfil/Login. */
class BaseUrlInterceptor(private val session: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val base = session.baseUrl.toHttpUrlOrNull() ?: return chain.proceed(req)
        val url = req.url.newBuilder().scheme(base.scheme).host(base.host).port(base.port).build()
        return chain.proceed(req.newBuilder().url(url).build())
    }
}

/** Manda el token y, si el servidor dice 401, cierra la sesión para pedir login otra vez. */
class AuthInterceptor(private val session: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.session.value?.token
        var req = chain.request()
        if (token != null && !req.url.encodedPath.contains("/api/auth/")) {
            req = req.newBuilder().header("Authorization", "Bearer $token").build()
        }
        val res = chain.proceed(req)
        if (res.code == 401 && token != null) session.cerrar()
        return res
    }
}
