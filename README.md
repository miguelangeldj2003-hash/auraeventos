# AURA EVENTOS – App Android (Kotlin + Jetpack Compose + MVVM)

Versión móvil del front web: mismo diseño (Noto Sans, azul marino, verde esmeralda, dorado), mismas pantallas y reglas.

## Acceso (cuentas de prueba, funcionan SIN internet ni servidor)
| Rol | Correo | Contraseña |
|---|---|---|
| Cliente | maria@gmail.com | Maria123* |
| Administradora | ana@aura.com | Admin123! |

Están escritas abajo en la pantalla de login; al tocarlas rellenan el formulario. Con ellas la app usa datos de ejemplo
en memoria (franja "MODO DEMOSTRACIÓN"). Con otras cuentas habla con la API real (engrane ⚙ para poner la URL).
"Registrarse" sin servidor crea una cuenta de demostración.

## Pantallas
**Cliente:** Login · Registro · Inicio (Mi Dashboard) · Catálogo (2 secciones, filtros, "Más rentado", "En carrito") · Detalle de artículo
(especificaciones, favoritos) · Carrito · Reservación (calendario de entrega y recogida, días ocupados en gris, dirección,
notas, GPS, IVA 16%) · Solicitud enviada · Mis solicitudes · Detalle con descarga de la Nota de Remisión (PDF) · Perfil.
**Administradora:** Panel · Agenda de solicitudes · Revisión (flete obligatorio, IVA, verificación de stock, aprobar/rechazar,
nota PDF lista) · Inventario (estadísticas, editar, deshabilitar) · Alta/edición de artículo · Entregas · Evidencias · Perfil.
Todas las pantallas internas tienen botón para regresar.

## Funciones nuevas (respecto a la web)
- **Evidencias con cámara y GPS** al entregar y recoger.
- **Modo sin internet:** todo se guarda en el teléfono y se sube solo al volver la conexión. Barra inferior tipo Spotify
  (sin conexión / subiendo X de Y / sincronizado / error); al tocarla muestra el detalle de lo subido.
- **Ubicación GPS del lugar del evento** al reservar.

## Estructura
`data/` (API, repositorio, demo, sesión) · `sync/` (cola offline, WorkManager) · `ui/` (tema, componentes, pantallas por módulo).

## Requisitos
Android Studio Koala o superior, JDK 17+, SDK 34. Abrir la carpeta del proyecto en Android Studio y sincronizar con Gradle.
