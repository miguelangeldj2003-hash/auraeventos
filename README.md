# AURA EVENTOS – App Android (Kotlin + Jetpack Compose + MVVM)

Plataforma móvil para la gestión y renta de mobiliario y mesas de dulces para eventos sociales. Construida con Kotlin, Jetpack Compose, arquitectura MVVM, Room, Retrofit y Hilt.

## Acceso (Cuentas de Prueba - Funcionan SIN Internet ni Servidor)

| Rol | Correo | Contraseña |
|---|---|---|
| **Cliente** | `maria@gmail.com` | `Maria123*` |
| **Administradora** | `ana@aura.com` | `AdminClave123*` |

*Las cuentas de prueba aparecen en tarjetas al pie de la pantalla de Login; al tocarlas se rellenan automáticamente los campos.*

---

## Funcionalidades Destacadas

* **Selección de Foto desde la Galería (Photo Picker):** Carga de imágenes nativa con `PickVisualMedia`, submuestreo eficiente, compresión JPG (1280px máx, calidad 82) y corrección de rotación EXIF. Subida multipart al backend.
* **Tolerancia Offline y Modo Sin Internet:** Guardado inmediato de evidencias en base de datos local **Room** (`aura.db`) y sincronización automática en segundo plano mediante **WorkManager** al recuperar conexión.
* **Barra de Estado de Sincronización:** Componente animado tipo Spotify que notifica la conexión (Sin conexión / Subiendo X de Y / Sincronizado / Error).
* **Integración con Cámara y GPS:** Captura de fotos con **CameraX** y geolocalización precisa (latitud, longitud y precisión) con **Play Services Location**.
* **Generación Nativa de PDF:** Creación e impresión de la Nota de Remisión en PDF nativo de Android.
* **Conexión Servidor Editable:** Ajuste dinámico de la URL del backend mediante el ícono de engrane ⚙ en Login o en Perfil.

---

## Estructura del Proyecto

```text
aura-android/
├── app/                        # Proyecto móvil Android
│   └── src/main/java/com/aura/eventos/
│       ├── data/               # Modelos DTO, Room Database, Repositorio y Fake API Demo
│       ├── di/                 # Módulos de Inyección de Dependencias (Hilt)
│       ├── sync/               # Monitor de Red y WorkManager de sincronización
│       └── ui/                 # Pantallas Compose, ViewModels y Tema (Módulos: auth, admin, catalogo, etc.)
├── api/                        # Proyecto Backend Spring Boot (Java 17)
│   └── aura-api/               # Endpoints REST para autenticación, artículos, fotos y reservaciones
└── db/                         # Base de Datos
    └── aura_eventos.sql        # Esquema MySQL e insumos iniciales
```

---

## Conexión al Servidor Real

1. En la pantalla de **Login**, toca el ícono del engrane ⚙ (arriba a la derecha).
2. Ingresa la dirección IP o dominio de tu servidor (ejemplo: `http://192.168.1.100:8080/`).
3. Toca **Guardar y probar** para probar la conexión con el backend real.

---

## Requisitos y Compilación

* **Android Studio:** Koala o superior.
* **JDK:** Java 17+.
* **SDK objetivo:** Android 14 (API 34).
* **Pasos:** Abrir la carpeta del proyecto en Android Studio, ejecutar Gradle Sync y seleccionar `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
