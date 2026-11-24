# Aura Eventos — Plan de Pruebas y Manual Técnico (v1.2.1)

---

## PARTE 1: PLAN DE PRUEBAS Y RESULTADOS DE EJECUCIÓN

### Pruebas Unitarias (`./gradlew testDebugUnitTest --rerun-tasks`)

**Salida real de ejecución del comando:**
```text
> Task :app:testDebugUnitTest

com.aura.eventos.AuraUnitTest > testUT01_ivaSoloSobreSubtotal PASSED
com.aura.eventos.AuraUnitTest > testUT02_totalEsSubtotalMasIvaMasFlete PASSED
com.aura.eventos.AuraUnitTest > testUT03_correoInvalido PASSED
com.aura.eventos.AuraUnitTest > testUT04_contrasenaInvalidaSieteCaracteres PASSED
com.aura.eventos.AuraUnitTest > testUT05_noPedirMasDeLoLibreEnCualquierDiaDelRango PASSED

Finished generating test XML results into: app/build/test-results/testDebugUnitTest
Finished generating test HTML results into: app/build/reports/tests/testDebugUnitTest

BUILD SUCCESSFUL in 1m 40s
31 actionable tasks: 31 executed
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

*Criterio de estado:* Se marca **Aprobado** únicamente si se cuenta con captura de pantalla PNG real almacenada en `docs/evidencias/`. Los pasos validados visualmente sin captura se marcan **Validado manualmente (sin captura)**. Los casos que requieren interacción con el backend en vivo se indican como **No probado**.

| ID | Caso de Prueba | Descripción / Pasos | Resultado Real | Estado | Evidencia |
|---|---|---|---|---|---|
| **CP-01** | Login Cliente Demo | Seleccionar tarjeta demo Cliente (`maria@gmail.com`) e Iniciar Sesión. | Acceso correcto al Dashboard de Cliente con tarjetas de resumen. | **Aprobado** | [`CP-01_login_cliente.png`](evidencias/CP-01_login_cliente.png) |
| **CP-02** | Login Admin Demo | Seleccionar tarjeta demo Admin (`ana@aura.com`) e Iniciar Sesión. | Acceso correcto al Perfil y Panel de Administradora. | **Aprobado** | [`CP-02_login_admin.png`](evidencias/CP-02_login_admin.png) |
| **CP-03** | Credenciales inválidas | Ingresar correo no registrado o clave errónea en modo demo. | Muestra mensaje de error solicitando verificar credenciales. | **Validado manualmente (sin captura)** | N/A |
| **CP-04** | Registro en servidor real | Registrar nuevo usuario mediante API REST remota. | Pendiente de verificación: requiere servidor backend Docker activo. | **No probado** | Sin servidor activo |
| **CP-05** | Validación contraseña corta | Intentar registrarse con clave de 7 caracteres. | El botón permanece deshabilitado y exige mínimo 8 caracteres. | **Validado manualmente (sin captura)** | N/A |
| **CP-06** | Olvidó contraseña | Pulsar "¿Olvidaste tu contraseña?" en login. | Abre diálogo informando contactar a la administradora. | **Validado manualmente (sin captura)** | N/A |
| **CP-07** | Navegación Catálogo | Cambiar entre "Mobiliario y Decoración" y "Mesas de Dulces". | Conmuta de sección mostrando los artículos correspondientes. | **Validado manualmente (sin captura)** | N/A |
| **CP-08** | Filtro y búsqueda | Filtrar por categoría (ej. Sillas & Mesas) y buscar por texto. | Actualiza la lista con las coincidencias. | **Validado manualmente (sin captura)** | N/A |
| **CP-09** | Detalle de artículo | Presionar una tarjeta de artículo. | Abre vista con especificaciones y dimensiones. | **Validado manualmente (sin captura)** | N/A |
| **CP-10** | Favoritos | Tocar el ícono de corazón en un artículo. | Agrega el artículo a la lista de favoritos en Perfil. | **Validado manualmente (sin captura)** | N/A |
| **CP-11** | Agregar al carrito | Seleccionar cantidad de unidades y presionar "Agregar". | El contador del carrito se incrementa. | **Validado manualmente (sin captura)** | N/A |
| **CP-12** | Gestión del carrito | Modificar cantidades con `+` / `-` o eliminar un ítem. | Recalcula subtotal, IVA y total estimado. | **Validado manualmente (sin captura)** | N/A |
| **CP-13** | Visualización Calendario | Abrir selector de fechas en el flujo de reserva. | Días sin stock suficiente se muestran en gris ("No disponible"). | **Validado manualmente (sin captura)** | N/A |
| **CP-14** | Validación de conflicto fecha | Intentar seleccionar un día gris bloqueado en el calendario. | La app impide la selección de los días 1 al 21 en gris. | **Aprobado** | [`CP-14_conflicto_calendario.png`](evidencias/CP-14_conflicto_calendario.png) |
| **CP-15** | Selección rango fecha | Seleccionar fecha de entrega (toque 1) y recogida (toque 2). | Resalta el rango en verde y habilita continuar. | **Validado manualmente (sin captura)** | N/A |
| **CP-16** | Formulario de reserva | Ingresar dirección de entrega, tipo de entorno y ubicación GPS. | Captura dirección y coordenadas correctamente. | **Validado manualmente (sin captura)** | N/A |
| **CP-17** | Confirmar reservación | Presionar "Enviar solicitud". | Genera folio de reservación (ej. `#AUR-2026-0847`). | **Validado manualmente (sin captura)** | N/A |
| **CP-18** | Solicitudes pendientes | Ir a pestaña "Solicitudes" como cliente. | Lista las solicitudes en estado PENDIENTE con flete por asignar. | **Aprobado** | [`CP-18_solicitudes_pendientes.png`](evidencias/CP-18_solicitudes_pendientes.png) |
| **CP-19** | Aprobar solicitud (Admin) | En Agenda Admin, ingresar flete y presionar "Aprobar". | Cambia estado a APROBADO y calcula total final. | **Validado manualmente (sin captura)** | N/A |
| **CP-20** | Rechazar solicitud (Admin) | En Agenda Admin, ingresar motivo y presionar "Rechazar". | Cambia estado a RECHAZADO especificando el motivo. | **Validado manualmente (sin captura)** | N/A |
| **CP-21** | Descarga Nota PDF | En detalle de solicitud aprobada, presionar "Descargar nota (PDF)". | Genera y abre `nota-AUR-2026-0801.pdf` en el visor del sistema. | **Aprobado** | [`CP-21_nota_remision_pdf.png`](evidencias/CP-21_nota_remision_pdf.png) |
| **CP-22** | Dashboard Admin | Revisar resumen financiero y top de rentas en Panel. | Muestra métricas e ingresos de 6 meses. | **Validado manualmente (sin captura)** | N/A |
| **CP-23** | Búsqueda en Inventario | En pestaña Inventario Admin, buscar por SKU o nombre. | Filtra los elementos del almacén. | **Validado manualmente (sin captura)** | N/A |
| **CP-24** | Alta de artículo con foto | Presionar "+ Agregar artículo" y seleccionar foto. | Abre el selector para registrar artículo. | **Validado manualmente (sin captura)** | N/A |
| **CP-25** | Edición de artículo | Presionar "Editar" e invocar Photo Picker. | Abre `com.google.android.photopicker` para seleccionar foto. | **Aprobado** | [`CP-25_photo_picker.png`](evidencias/CP-25_photo_picker.png) |
| **CP-26** | Baja de artículo | Presionar "Deshabilitar" en un artículo activo. | Cambia estado a inactivo y actualiza contadores. | **Validado manualmente (sin captura)** | N/A |
| **CP-27** | Entregas del día | Ir a pestaña "Entregas" en el menú de la Administradora. | Lista solicitudes programadas para entrega. | **Validado manualmente (sin captura)** | N/A |
| **CP-28** | Captura de evidencia | En Entregas, capturar fotografía y coordenadas GPS. | Muestra vista previa con ubicación detectada. | **Validado manualmente (sin captura)** | N/A |
| **CP-29** | Persistencia local evidencia | Presionar "Guardar evidencia". | Almacena evidencia en la base `Room` local. | **Validado manualmente (sin captura)** | N/A |
| **CP-30** | Sincronización en línea | Subida de evidencias locales al backend remoto. | Pendiente de verificación: requiere servidor backend Docker activo. | **No probado** | Sin servidor activo |
| **CP-31** | Modo Avión (Offline) | Activar modo avión y guardar evidencia. | Muestra indicador SyncBar en ámbar. | **Validado manualmente (sin captura)** | N/A |
| **CP-32** | Sincronización diferida | Desactivar modo avión tras guardar en offline. | Muestra progreso de sincronización y pasa a verde. | **Validado manualmente (sin captura)** | N/A |
| **CP-33** | Configurar Servidor | Probar conexión en vivo con backend remoto. | Pendiente de verificación: requiere servidor backend Docker activo. | **No probado** | Sin servidor activo |
| **CP-34** | Cierre de sesión seguro | Intentar cerrar sesión con evidencias pendientes. | Muestra diálogo de advertencia informando resguardo. | **Validado manualmente (sin captura)** | N/A |

---

### Correcciones Encontradas Durante las Pruebas

| Fecha | Componente | Descripción de la Falla | Corrección Aplicada (`fix:`) | Re-prueba |
|---|---|---|---|---|
| 23/11/2026 | `PerfilScreen` / `AuraRepository` | El ViewModel consumía `FakeAuraApi` directamente rompiendo la arquitectura MVVM pura. | Refactor a `AuraRepository` para exponer `reiniciarDemo()`. Commit `f569a8a`. | Aprobado |
| 23/11/2026 | `app/build.gradle.kts` | Falta de firmado en `assembleRelease` por ruta relativa de `storeFile`. | Se ajustó a `rootProject.file(storePath)` para resolver correctamente `aura-release.jks`. Commit `f569a8a`. | Aprobado |

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
