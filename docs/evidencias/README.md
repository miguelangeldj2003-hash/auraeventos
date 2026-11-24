# Evidencias Reales de Pruebas — Aura Eventos

Este directorio contiene las capturas de pantalla PNG reales tomadas directamente mediante `adb exec-out screencap -p` en un emulador ejecutando el APK firmado de release (`app-release.apk`), así como la salida real de las pruebas unitarias:

- `unit-tests.txt`: Salida terminal de la ejecución real de `./gradlew testDebugUnitTest --rerun-tasks`.
- `CP-01_login_cliente.png`: Captura de pantalla real del Dashboard tras inicio de sesión exitoso como Cliente (`maria@gmail.com`).
- `CP-02_login_admin.png`: Captura de pantalla real del Perfil/Panel tras inicio de sesión exitoso como Administradora (`ana@aura.com`).
- `CP-14_conflicto_calendario.png`: Captura de pantalla real del bloqueo de días ocupados/sin stock (días 1 al 21 en gris) al seleccionar fechas.
- `CP-18_solicitudes_pendientes.png`: Captura de pantalla real del listado de solicitudes en estado `PENDIENTE`.
- `CP-21_nota_remision_pdf.png`: Captura de pantalla real de la Nota de Remisión en PDF abierta en el visor del sistema (`nota-AUR-2026-0801.pdf`).
- `CP-25_photo_picker.png`: Captura de pantalla real de la apertura nativa de Android Photo Picker (`com.google.android.photopicker`).
