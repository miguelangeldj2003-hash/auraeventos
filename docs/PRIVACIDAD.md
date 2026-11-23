# Política de Privacidad — Aura Eventos

**Última actualización:** Noviembre 2026

En **Aura Eventos**, nos tomamos muy en serio la privacidad y la protección de los datos de nuestros usuarios. Esta Política de Privacidad describe cómo recopilamos, utilizamos, almacenamos y protegemos la información personal en la aplicación móvil **Aura Eventos**.

---

## 1. Datos que se Recopilan

La aplicación puede recopilar la siguiente información únicamente cuando el usuario la proporciona voluntariamente o al conceder los permisos correspondientes:

1. **Información de Registro y Cuenta:**
   - Nombre completo.
   - Correo electrónico.
   - Número telefónico (opcional).
   - Rol dentro de la plataforma (Cliente / Administradora).

2. **Información de Reservaciones y Entregas:**
   - Dirección del evento (calle, número, colonia, ciudad).
   - Notas adicionales para el equipo de logística.
   - Fechas solicitadas de inicio y fin de renta.

3. **Fotografías y Evidencias de Entrega:**
   - Fotografías capturadas con la cámara o seleccionadas desde la galería (Photo Picker) para actualizar el catálogo o registrar evidencias de entrega de inventario.

4. **Ubicación Geográfica (GPS):**
   - Coordenadas de latitud y longitud al solicitar la ubicación actual para la entrega o al capturar evidencias de entrega por parte del personal de operaciones.

---

## 2. Finalidad del Tratamiento de los Datos

Los datos personales recopilados se utilizan exclusivamente para las siguientes finalidades esenciales:

- **Gestión de la cuenta:** Autenticar al usuario y permitir el acceso a las funciones correspondientes (cliente o administradora).
- **Procesamiento de reservaciones:** Confirmar la disponibilidad del mobiliario, asignar logística de entrega/flete y generar la Nota de Remisión formal.
- **Evidencia operativa y calidad:** Registrar fotografías e impresiones de ubicación GPS en la entrega del mobiliario para garantizar el estado correcto de los artículos rendidos.
- **Sincronización fuera de línea:** Almacenar localmente en el dispositivo las evidencias capturadas sin conexión para ser sincronizadas automáticamente cuando se restablezca el acceso a Internet.

---

## 3. Dónde y Cómo se Guardan los Datos

- **Persistencia Local:** Los datos de sesión y las evidencias capturadas sin conexión se almacenan localmente en el dispositivo mediante bases de datos cifradas `Room` y preferencias de sesión protegidas.
- **Almacenamiento en Servidor:** Al sincronizar o estar en línea, los datos se transmiten de forma segura (HTTPS / TLS) hacia nuestros servidores backend en **AWS / Docker**, donde se resguardan en una base de datos PostgreSQL cifrada.
- **Privacidad de Imágenes:** La selección de imágenes se realiza utilizando el componente oficial **Android Photo Picker**, el cual garantiza que la aplicación solo tenga acceso al archivo individual seleccionado por el usuario y nunca a toda la biblioteca de fotos.

---

## 4. Solicitud de Borrado de Datos (Derechos ARCO)

El usuario tiene derecho a acceder, rectificar, cancelar u oponerse al tratamiento de sus datos personales en cualquier momento.

Para solicitar la eliminación definitiva de su cuenta y de todos los datos asociados:

1. Envíe un correo electrónico a **privacidad@auraeventos.mx** con el asunto *"Solicitud de Eliminación de Cuenta y Datos"*.
2. Indique el correo electrónico registrado en la plataforma y el nombre del titular.
3. El equipo procesará su solicitud y confirmará el borrado definitivo en un plazo no mayor a 5 días hábiles.
