# AURA EVENTOS – App Android (v1.2.0)

Plataforma móvil para la gestión y renta de mobiliario y mesas de dulces para eventos sociales. Construida con Kotlin, Jetpack Compose, arquitectura MVVM, Room, Retrofit y Hilt.

---

## Cuentas de Demostración (Funcionan sin servidor)

| Rol | Correo | Contraseña |
|---|---|---|
| **Cliente** | `maria@gmail.com` | `Maria123*` |
| **Administradora** | `ana@aura.com` | `AdminClave123*` |

*Las cuentas de prueba aparecen en tarjetas al pie de la pantalla de Login; al tocarlas se rellenan automáticamente los campos.*

---

## Funcionalidades Principales

* **Estructura MVVM Clean Architecture:** Separación estricta entre capa de presentación (Compose), lógica de estado (`StateFlow`) y repositorio único (`AuraRepository`, `EvidenceRepository`).
* **Selección de Foto de Galería (Photo Picker):** Selección de imágenes de inventario mediante Android Photo Picker (`PickVisualMedia`), submuestreo, compresión y corrección EXIF.
* **Tolerancia Offline y Sincronización:** Persistencia local de evidencias en base de datos **Room** (`aura.db`) y subida diferida con **WorkManager** al recuperar Internet.
* **Barra de Estado de Sincronización (`SyncBar`):** Componente dinámico que notifica el estado de red y sincronización (Sin conexión / Subiendo / Sincronizado).
* **Cámara y Geolocalización GPS:** Integración con **CameraX** y **Play Services Location** para capturar fotos y ubicación exacta en entregas.
* **Generación de PDF Nativo:** Generación y visualización de la Nota de Remisión formal en PDF.
* **Firmado de Release de Producción:** Configuración de `signingConfigs.release` con ProGuard y verificación `apksigner`.

---

## Cómo Compilar y Ejecutar

### 1. Compilación de Desarrollo (Debug)
```bash
.\gradlew.bat assembleDebug
```

### 2. Generación de Release Firmada
1. Generar la llave JKS con `keytool`:
   ```bash
   keytool -genkeypair -v -keystore aura-release.jks -alias aura-key -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Aura Eventos, O=Aura Eventos, C=MX" -storepass TU_CLAVE -keypass TU_CLAVE
   ```
2. Copiar `keystore.properties.example` a `keystore.properties` y configurar las credenciales.
3. Ejecutar la compilación de Release:
   ```bash
   .\gradlew.bat assembleRelease
   ```
4. Verificar con `apksigner`:
   ```bash
   apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
   ```

---

## Cómo Levantar el Servidor Backend (Docker Compose)

El backend en Spring Boot + PostgreSQL se despliega mediante Docker:

1. Crear el archivo `.env` en la raíz del proyecto backend basado en `.env.example`.
2. Levantar el contenedor:
   ```bash
   docker compose up -d --build
   ```
3. Verificar salud del servidor: `GET http://localhost:8080/api/salud`.

---

## Conexión al Servidor desde la App

1. En la pantalla de **Login** o en **Perfil**, toca el ícono del engrane ⚙ (arriba a la derecha).
2. Ingresa la URL del servidor (ejemplo: `http://10.0.2.2:8080/` para emulador o `http://192.168.1.100:8080/` para dispositivo físico).
3. Toca **Guardar y probar** para comprobar la conexión activa con el servidor.

---

## Estructura de Carpetas

```text
aura-android/
├── app/                                  # Aplicación Android
│   ├── src/main/java/com/aura/eventos/
│   │   ├── data/                         # Repositorios, DTOs, Room DB y Demo API
│   │   ├── di/                           # Módulos Hilt (AppModule)
│   │   ├── sync/                         # ConnectivityObserver y WorkManager
│   │   └── ui/                           # Pantallas Compose y ViewModels
│   ├── src/test/                         # Pruebas unitarias (UT-01 a UT-05)
│   └── proguard-rules.pro                # Reglas ProGuard para Release
├── docs/                                 # Documentación técnica y legal
│   ├── ARQUITECTURA.md                   # Diagrama de capas y flujo de caso de uso
│   ├── PLAN_DE_PRUEBAS_Y_MANUAL_TECNICO.md # Resultados de pruebas y manual de usuario/técnico
│   └── PRIVACIDAD.md                     # Política de privacidad
├── api/aura-api/                         # Servidor backend Java Spring Boot
├── keystore.properties.example           # Plantilla de configuración de firma
└── docker-compose.yml                    # Orquestador Docker
```

---

## Documentación Adicional

- [Arquitectura de la Aplicación](docs/ARQUITECTURA.md)
- [Plan de Pruebas y Manual Técnico](docs/PLAN_DE_PRUEBAS_Y_MANUAL_TECNICO.md)
- [Política de Privacidad](docs/PRIVACIDAD.md)
