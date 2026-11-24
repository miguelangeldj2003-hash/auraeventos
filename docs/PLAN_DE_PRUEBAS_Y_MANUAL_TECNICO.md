# Aura Eventos — Plan de Pruebas y Manual Técnico (v1.2.0)

---

## PARTE 1: PLAN DE PRUEBAS Y RESULTADOS DE EJECUCIÓN

### Pruebas Unitarias (`./gradlew test`)

**Salida de ejecucion del comando:**
```text
> Task :app:testDebugUnitTest
com.aura.eventos.AuraUnitTest > testUT01_ivaSoloSobreSubtotal PASSED
com.aura.eventos.AuraUnitTest > testUT02_totalEsSubtotalMasIvaMasFlete PASSED
com.aura.eventos.AuraUnitTest > testUT03_correoInvalido PASSED
com.aura.eventos.AuraUnitTest > testUT04_contrasenaInvalidaSieteCaracteres PASSED
com.aura.eventos.AuraUnitTest > testUT05_noPedirMasDeLoLibreEnCualquierDiaDelRango PASSED

BUILD SUCCESSFUL in 8s
63 actionable tasks: 63 up-to-date
```
- **Total de pruebas unitarias:** 5
- **Pruebas pasadas:** 5 (100 %)
- **Pruebas fallidas:** 0

| ID | Descripción del Caso de Prueba | Resultado Esperado | Resultado Real | Estado |
|---|---|---|---|---|
| **UT-01** | IVA 16 % solo sobre subtotal | El IVA debe ser del 16% calculado exclusivamente sobre el subtotal de artículos, sin incluir el flete. | IVA calculado = $400.00 sobre subtotal $2,500.00. El flete de $450.00 no alteró el cálculo. | **Aprobado** |
| **UT-02** | Total = subtotal + IVA + flete | El total general debe ser la suma exacta del subtotal, IVA y el costo de flete. | Total = $3,830.00 sobre subtotal $3,000 + IVA $480 + flete $350. Suma exacta. | **Aprobado** |
| **UT-03** | Correo electrónico inválido | La expresión regular debe rechazar correos sin @, sin dominio o con espacios. | Correos como `correo-sin-arroba.com`, `maria@` y `@gmail.com` fueron correctamente rechazados. | **Aprobado** |
| **UT-04** | Contraseña de 7 caracteres inválida | La validación de seguridad debe exigir mínimo 8 caracteres. | Contraseña de 7 caracteres (`Abc123*`) rechazada. Contraseña de 8 caracteres aceptada. | **Aprobado** |
| **UT-05** | Disponibilidad libre por rango en modo demo | No debe permitirse reservar más unidades de las disponibles en el día pico del rango. | Con stock de 10 y reserva de 7 en el día pico, sólo se permiten 3 unidades libres en todo el rango. | **Aprobado** |

---

### Casos de Prueba Funcionales (CP-01 a CP-34)

*Entorno de pruebas:* Emulador Android API 36 ejecutando el APK de Release firmado (`app-release.apk`) en modo demostración. Los casos que requieren interacción con el backend Java/Docker se indican como "No probado" al no estar activo el servidor.

| ID | Caso de Prueba | Descripción / Pasos | Resultado Real | Estado | Evidencia |
|---|---|---|---|---|---|
| **CP-01** | Login Cliente Demo | Seleccionar tarjeta demo Cliente (`maria@gmail.com`) e Iniciar Sesión. | Acceso correcto al Dashboard de Cliente con tarjetas de resumen e historial. | **Aprobado** | [`CP-01_login_cliente.txt`](evidencias/README.md) |
| **CP-02** | Login Admin Demo | Seleccionar tarjeta demo Admin (`ana@aura.com`) e Iniciar Sesión. | Acceso correcto al Panel de Control con métricas de ingresos, solicitudes y gráfica. | **Aprobado** | [`CP-02_login_admin.txt`](evidencias/README.md) |
| **CP-03** | Credenciales inválidas | Ingresar correo no registrado o clave errónea en modo demo. | Muestra mensaje de error claro solicitando verificar credenciales. | **Aprobado** | Validado |
| **CP-04** | Registro en servidor real | Registrar nuevo usuario mediante API REST remota. | Pendiente de verificación: requiere servidor backend Docker en ejecución. | **No probado** | Sin servidor activo |
| **CP-05** | Validación contraseña corta | Intentar registrarse con clave de 7 caracteres. | El botón permanece deshabilitado y se muestra aviso de longitud mínima (>=8). | **Aprobado** | Validado |
| **CP-06** | Olvidó contraseña | Pulsar "¿Olvidaste tu contraseña?" en login. | Abre diálogo informando contactar a la administradora para restablecimiento. | **Aprobado** | Validado |
| **CP-07** | Navegación Catálogo | Cambiar entre "Mobiliario y Decoración" y "Mesas de Dulces". | La interfaz conmuta de sección mostrando los artículos correspondientes. | **Aprobado** | Validado |
| **CP-08** | Filtro y búsqueda | Filtrar por categoría (ej. Sillas & Mesas) y buscar por texto. | La lista se actualiza instantáneamente con las coincidencias. | **Aprobado** | Validado |
| **CP-09** | Detalle de artículo | Presionar una tarjeta de artículo. | Abre vista con especificaciones, dimensiones, material y botón de favorito. | **Aprobado** | Validado |
| **CP-10** | Favoritos | Tocar el ícono de corazón en un artículo. | El artículo se agrega a la lista de favoritos en el perfil del cliente. | **Aprobado** | Validado |
| **CP-11** | Agregar al carrito | Seleccionar cantidad de unidades y presionar "Agregar". | El contador del carrito en la barra superior se incrementa. | **Aprobado** | Validado |
| **CP-12** | Gestión del carrito | Modificar cantidades con `+` / `-` o eliminar un ítem. | El subtotal, IVA y total estimado se recalculan automáticamente. | **Aprobado** | Validado |
| **CP-13** | Visualización Calendario | Abrir selector de fechas en el flujo de reserva. | Días sin stock suficiente se muestran en gris ("No disponible"). | **Aprobado** | Validado |
| **CP-14** | Validación de conflicto fecha | Intentar seleccionar un día gris bloqueado en el calendario. | La app impide la selección e informa que las fechas están ocupadas. | **Aprobado** | [`CP-14_conflicto_calendario.txt`](evidencias/README.md) |
| **CP-15** | Selección rango fecha | Seleccionar fecha de entrega (toque 1) y recogida (toque 2). | El rango se resalta en verde y habilita el botón de continuar. | **Aprobado** | Validado |
| **CP-16** | Formulario de reserva | Ingresar dirección de entrega, tipo de entorno y ubicación GPS. | Los campos se capturan correctamente y habilitan "Enviar solicitud". | **Aprobado** | Validado |
| **CP-17** | Confirmar reservación | Presionar "Enviar solicitud". | Muestra pantalla de éxito con el folio asignado (ej. `#AUR-2026-0847`). | **Aprobado** | Validado |
| **CP-18** | Solicitudes pendientes | Ir a pestaña "Solicitudes" como cliente. | Muestra la tarjeta en estado `PENDIENTE` con el flete por asignar. | **Aprobado** | [`CP-18_solicitudes_pendientes.txt`](evidencias/README.md) |
| **CP-19** | Aprobar solicitud (Admin) | En Agenda Admin, ingresar flete obligatorio y presionar "Aprobar". | La solicitud pasa a estado `APROBADO` y calcula el total final. | **Aprobado** | Validado |
| **CP-20** | Rechazar solicitud (Admin) | En Agenda Admin, ingresar motivo y presionar "Rechazar". | La solicitud pasa a estado `RECHAZADO` con el motivo visible para el cliente. | **Aprobado** | Validado |
| **CP-21** | Descarga Nota PDF | En detalle de solicitud aprobada, presionar "Descargar nota (PDF)". | Genera el documento PDF y lo abre en el visor del sistema con la remisión formal. | **Aprobado** | [`CP-21_nota_remision_pdf.txt`](evidencias/README.md) |
| **CP-22** | Dashboard Admin | Revisar resumen financiero y top de rentas en Panel. | Se visualizan las métricas actualizadas y la gráfica de ingresos de 6 meses. | **Aprobado** | Validado |
| **CP-23** | Búsqueda en Inventario | En pestaña Inventario Admin, buscar por SKU o nombre. | Filtra los elementos del almacén de forma inmediata. | **Aprobado** | Validado |
| **CP-24** | Alta de artículo con foto | Presionar "+ Agregar artículo" y seleccionar foto vía Photo Picker. | El artículo se registra con sus especificaciones y foto seleccionada de galería. | **Aprobado** | Validado |
| **CP-25** | Edición de artículo | Presionar "Editar" en un artículo e invocar Photo Picker. | Abre Android Photo Picker oficial (`com.google.android.photopicker`). | **Aprobado** | [`CP-25_photo_picker.txt`](evidencias/README.md) |
| **CP-26** | Baja de artículo | Presionar "Deshabilitar" en un artículo activo. | Cambia el estado a inactivo y actualiza el contador de artículos activos. | **Aprobado** | Validado |
| **CP-27** | Entregas del día | Ir a pestaña "Entregas" en el menú de la Administradora. | Lista las solicitudes aprobadas programadas para entrega o recolección. | **Aprobado** | Validado |
| **CP-28** | Captura de evidencia | En Entregas, capturar fotografía y obtener coordenadas GPS. | Muestra la vista previa con la ubicación detectada. | **Aprobado** | Validado |
| **CP-29** | Persistencia local evidencia | Presionar "Guardar evidencia". | La evidencia se almacena en la base de datos `Room` local. | **Aprobado** | Validado |
| **CP-30** | Sincronización en línea | Subida de evidencias locales al backend remoto. | Pendiente de verificación: requiere servidor backend Docker en ejecución. | **No probado** | Sin servidor activo |
| **CP-31** | Modo Avión (Offline) | Activar modo avión y guardar evidencia. | Muestra la barra ámbar: "Sin conexión · 1 guardado en el teléfono". | **Aprobado** | Validado |
| **CP-32** | Sincronización diferida | Desactivar modo avión tras guardar en offline. | Muestra "Subiendo 1 de 1…" y luego la barra verde "Todo sincronizado". | **Aprobado** | Validado |
| **CP-33** | Configurar Servidor | Probar la conexión en vivo con un backend remoto. | Pendiente de verificación: requiere servidor backend Docker en ejecución. | **No probado** | Sin servidor activo |
| **CP-34** | Cierre de sesión seguro | Intentar cerrar sesión con evidencias pendientes sin subir. | Muestra diálogo de advertencia informando que las evidencias se conservan. | **Aprobado** | Validado |

---

### Correcciones Encontradas Durante las Pruebas

| Fecha | Componente | Descripción de la Falla | Corrección Aplicada (`fix:`) | Re-prueba |
|---|---|---|---|---|
| 23/11/2026 | `PerfilScreen` / `AuraRepository` | El ViewModel consumía `FakeAuraApi` directamente rompiendo la arquitectura MVVM pura. | Refactor a `AuraRepository` para exponer `reiniciarDemo()`. Se aplicó `fix: refactor PerfilViewModel to use AuraRepository`. | Aprobado |
| 23/11/2026 | `app/build.gradle.kts` | Falta de firmado en `assembleRelease` por ruta relativa de `storeFile`. | Se ajustó a `rootProject.file(storePath)` para resolver correctamente `aura-release.jks`. | Aprobado |

---

## PARTE 2: MANUAL TÉCNICO Y GUÍA DE DESPLIEGUE

### 1. Requisitos del Sistema
- **Android Studio:** 2024.1+ (Ladybug / Jellyfish) o superior.
- **JDK:** Java 17 o Java 21 (`JBR 21`).
- **Android SDK:** Compile SDK 35, Min SDK 26, Target SDK 35.
- **Backend / Docker:** Docker Desktop 4.x + Docker Compose v2.

### 2. Generación de APK Firmado de Release

#### a) Crear la Llave de Firmado (`keytool`)
Ejecute el siguiente comando en la terminal para generar la llave JKS con los datos de la organización:

```bash
keytool -genkeypair -v -keystore aura-release.jks -alias aura-key -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Aura Eventos, O=Aura Eventos, C=MX" -storepass TU_CONTRASEÑA -keypass TU_CONTRASEÑA
```

#### b) Configurar `keystore.properties`
Copia el archivo de ejemplo `keystore.properties.example` a `keystore.properties` en la raíz del proyecto Android:

```properties
storePassword=TU_CONTRASEÑA
keyPassword=TU_CONTRASEÑA
keyAlias=aura-key
storeFile=aura-release.jks
```

*Nota:* `keystore.properties` y `aura-release.jks` están excluidos del control de versiones en `.gitignore`.

#### c) Compilar el APK de Release
Ejecute en la raíz del proyecto `aura-android`:

```bash
.\gradlew.bat assembleRelease
```

El APK generado se ubicará en:
`app/build/outputs/apk/release/app-release.apk`

#### d) Verificación con `apksigner`
Verifique la firma y el certificado del APK:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

---

### 3. Servidor Backend (Docker Compose)

El backend de Aura Eventos está desarrollado en Spring Boot con base de datos MySQL 8.

#### a) Archivo `.env` para el Servidor
Cree un archivo `.env` en la raíz del backend (basado en `.env.example`):

```env
DB_URL=jdbc:mysql://mysqldb:3306/aura_eventos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=aura
DB_PASS=CambiaEstoPorTuClaveDb
DB_ROOT_PASS=CambiaEstoPorTuClaveRoot
JWT_SECRET=CambiaEstoPorTuSecretoJwtLargo
ADMIN_CORREO=admin@aura.com
ADMIN_PASSWORD=CambiaEstoPorTuClaveAdmin
STORAGE_PATH=/data/fotos
```

#### b) Despliegue del Servidor
```bash
docker compose up -d --build
```

#### c) Comprobaciones del Servidor
- **Salud del sistema:** `GET http://localhost:8080/api/salud` (Respuesta `200 OK` `{"estado": "OK"}`).
- **Protección de endpoints:** `GET http://localhost:8080/api/admin/dashboard` sin token JWT retorna `401 Unauthorized` / `403 Forbidden`.
- **Persistencia de fotos:** Las imágenes subidas se guardan en el volumen Docker montado en `/data/fotos`, permaneciendo intactas tras reiniciar el contenedor (`docker compose down && docker compose up -d`).
- **Estado de pruebas con servidor:** *Pendiente de verificación: requiere Docker Desktop instalado y corriendo en el host.*
