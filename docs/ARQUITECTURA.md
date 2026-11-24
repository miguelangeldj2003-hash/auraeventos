# Arquitectura de Aura Eventos (Android)

## 1. Diagrama de Capas (MVVM + Clean Architecture)

```
+-----------------------------------------------------------------+
|                        CAPA DE PRESENTACIÓN                     |
|  [Jetpack Compose Screens]  <--->  [ViewModels (StateFlow/UI)]  |
|  (Auth, Catálogo, Reserva, Solicitudes, Admin, Evidence, Sync)  |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                        CAPA DE DOMINIO / DATOS                  |
|  [Repositories (AuraRepository, EvidenceRepository)]            |
|  - Gestión de Caché, Validación de Negocio, Coordinación        |
+-----------------------------------------------------------------+
                /                               \
               v                                 v
+-----------------------------+   +-------------------------------+
|    CAPA REMOTA (API REST)   |   |     CAPA LOCAL (ROOM DB)      |
|  - Retrofit + OkHttp        |   |  - AuraDatabase (Room)        |
|  - AuraApi / EvidenceApi    |   |  - EvidenceDao                |
|  - Interceptores Auth/URL   |   |  - SessionManager (Encrypted) |
+-----------------------------+   +-------------------------------+
```

## 2. Flujo de Caso de Uso: Enviar Solicitud de Reserva

1. **Pantalla (UI - Jetpack Compose)**: `ReservaConfirmaScreen` captura la fecha, dirección y notas del evento. Al presionar "Enviar Solicitud", invoca `ReservaViewModel.confirmarReserva()`.
2. **ViewModel (`ReservaViewModel`)**:
   - Actualiza el flujo de estado `_uiState` a estado cargando (`cargando = true`).
   - Invoca a `AuraRepository.crearReservacion(reservacionRequest)`.
3. **Repositorio (`AuraRepository`)**:
   - Empaqueta los items del carrito y la información de entrega en un `ReservacionRequest`.
   - Ejecuta la llamada remota usando la interfaz `AuraApi.crearReservacion(request)`.
4. **Capa Remota (`AuraApi` / Retrofit & OkHttp)**:
   - `AuthInterceptor` añade el encabezado `Authorization: Bearer <token>`.
   - `BaseUrlInterceptor` redirige las peticiones al endpoint configurado (`http://10.0.2.2:8080/api/`).
   - Envía la solicitud HTTP POST a `/api/reservaciones`.
5. **Respuesta y Servidor / Demo**:
   - El backend Spring Boot valida los datos, verifica disponibilidad en base de datos MySQL 8 y crea la reservación retornando el DTO.
   - En modo demostración (sin conexión), `AuraApiRouter` redirige a `FakeAuraApi` simulando la persistencia.
6. **Actualización de la Interfaz**:
   - `AuraRepository` retorna `Result.success(dto)` al ViewModel.
   - `ReservaViewModel` vacía el carrito a través de `CartManager.limpiar()` y actualiza el `StateFlow`.
   - La pantalla reacciona al cambio de estado, muestra la pantalla de confirmación con el folio asignado y permite descargar la Nota de Remisión en PDF.
