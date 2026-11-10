package com.aura.eventos.data.demo

import com.aura.eventos.data.local.SessionManager
import com.aura.eventos.data.remote.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Hace lo mismo que el servidor Java (stock por rango de fechas, IVA 16%, flete obligatorio, aprobar/rechazar,
 * panel), pero con datos en memoria. Sirve para presentar la app sin API ni base de datos.
 */
@Singleton
class FakeAuraApi @Inject constructor(private val session: SessionManager) : AuraApi {

    private val lock = Any()
    private val categorias = listOf(
        CategoriaDto(1, "Sillas & Mesas", "MOBILIARIO"), CategoriaDto(2, "Lounges", "MOBILIARIO"),
        CategoriaDto(3, "Iluminación & Letreros", "MOBILIARIO"), CategoriaDto(4, "Arcos & Florales", "MOBILIARIO"),
        CategoriaDto(5, "Candy Bar", "MESA_DULCES"), CategoriaDto(6, "Stands & Bases", "MESA_DULCES"),
        CategoriaDto(7, "Fondue & Fuentes", "MESA_DULCES"), CategoriaDto(8, "Mesas Candy", "MESA_DULCES")
    )
    // Clientes de ejemplo: id -> (nombre, correo, teléfono)
    private val clientes = mapOf(
        1L to Triple("Ana Martínez", "ana@aura.com", "55 1234 5678"),
        2L to Triple("María García López", "maria@gmail.com", "55 9876 5432"),
        3L to Triple("Laura Romero", "laura@gmail.com", "55 4455 6677"),
        4L to Triple("Sofía Torres", "sofia@gmail.com", "55 3322 1100"),
        5L to Triple("Ana López", "ana.lopez@gmail.com", "55 7788 9900"),
        6L to Triple("Carmen Ríos", "carmen@yahoo.com", "55 1122 3344"),
        7L to Triple("Patricia Mora", "patri@gmail.com", "55 6677 8899")
    )
    private val articulos = mutableListOf<ArticuloDto>()
    private val reservaciones = mutableListOf<ReservacionDto>()
    private var sigArticulo = 100L
    private var sigReservacion = 854L

    init { reiniciar() }

    // ---------- utilidades ----------
    private suspend fun <T> sim(bloque: () -> Response<T>): Response<T> {
        delay(350)   // simula la espera de la red
        return synchronized(lock) { bloque() }
    }
    private fun <T> ok(v: T): Response<T> = Response.success(v)
    private fun <T> err(codigo: Int, mensaje: String): Response<T> = Response.error(
        codigo, JSONObject().put("error", mensaje).toString().toResponseBody("application/json".toMediaType())
    )
    private fun redondear(x: Double) = Math.round(x * 100.0) / 100.0

    private fun art(
        id: Long, cat: Long, nombre: String, desc: String, precio: Double, stock: Int, sku: String,
        dim: String?, mat: String?, inc: String?, peso: String?, activo: Boolean = true, mant: Boolean = false
    ): ArticuloDto {
        val c = categorias.first { it.id == cat }
        return ArticuloDto(id, c.id, c.nombre, c.seccion, nombre, desc, precio, stock, null, null, activo,
            sku, dim, mat, inc, peso, mant, 0)
    }

    private fun nueva(
        id: Long, clienteId: Long, fecha: LocalDate, fin: LocalDate, dir: String, entorno: String, notas: String?,
        lat: Double?, lng: Double?, estado: String, flete: Double, motivo: String?, creado: LocalDateTime,
        items: List<Pair<Long, Int>>, info: Triple<String, String, String?>? = null
    ): ReservacionDto {
        val (nombre, correo, tel) = info ?: clientes.getValue(clienteId)
        val detalles = items.map { (aid, c) ->
            val a = articulos.first { it.id == aid }
            DetalleDto(aid, a.sku, a.nombre, c, a.precioBase, a.precioBase * c)
        }
        val sub = detalles.sumOf { it.subtotal }
        val iva = redondear(sub * 0.16)
        val dias = max(1, java.time.temporal.ChronoUnit.DAYS.between(fecha, fin).toInt())
        return ReservacionDto(id, "AUR-%d-%04d".format(LocalDate.now().year, id), clienteId, nombre, correo, tel,
            fecha.toString(), fin.toString(), dias, dir, entorno, notas, lat, lng, estado, motivo,
            creado.toString(), flete, sub, iva, redondear(sub + iva + flete), detalles)
    }

    private fun ocupadasEn(a: ArticuloDto, dia: LocalDate, cruzadas: List<ReservacionDto>): Int =
        cruzadas.filter { !dia.isBefore(LocalDate.parse(it.fechaEvento)) && !dia.isAfter(LocalDate.parse(it.fechaFin)) }
            .sumOf { r -> r.detalles.filter { it.articuloId == a.id }.sumOf { it.cantidad } }

    private fun cruzadas(desde: LocalDate, hasta: LocalDate, excluir: Long) =
        reservaciones.filter {
            it.estado == "APROBADO" && it.id != excluir &&
                !LocalDate.parse(it.fechaEvento).isAfter(hasta) && !LocalDate.parse(it.fechaFin).isBefore(desde)
        }

    /** Piezas libres: stock menos lo más ocupado que esté cualquier día del rango. */
    private fun libres(a: ArticuloDto, desde: LocalDate, hasta: LocalDate, excluir: Long = -1L): Int {
        if (a.enMantenimiento) return 0
        val cr = cruzadas(desde, hasta, excluir)
        var peor = 0
        var d = desde
        while (!d.isAfter(hasta)) { peor = max(peor, ocupadasEn(a, d, cr)); d = d.plusDays(1) }
        return max(0, a.stockTotal - peor)
    }

    private fun rentasDe(id: Long) = reservaciones.count { r -> r.estado == "APROBADO" && r.detalles.any { it.articuloId == id } }
    private fun conDatos(a: ArticuloDto, disp: Int?) = a.copy(disponibles = disp, rentas = rentasDe(a.id))

    /** Vuelve a los datos de ejemplo originales. */
    fun reiniciar() = synchronized(lock) {
        sigArticulo = 100L; sigReservacion = 854L
        articulos.clear()
        articulos.addAll(listOf(
            art(1, 1, "Sillas Chiavari Blancas x10", "Set de 10 sillas con cojín satinado. Perfectas para ceremonias y banquetes formales.", 2400.0, 15, "SC-004-BLA", "0.92m de alto", "Madera lacada", "Cojín satinado", "120 kg"),
            art(2, 1, "Mesa Rectangular Rústica", "Mesa de madera para 8 personas.", 380.0, 12, "SC-010-RUS", "2.4m × 0.9m", "Madera de pino", "Montaje", "150 kg"),
            art(3, 2, "Set Lounge Blanco Moderno", "Sofá 3 plazas, 2 sillones y mesa de centro. Ideal para áreas VIP y lounges de eventos.", 3500.0, 6, "SL-005-BLA", "3.2m × 1.8m", "Piel sintética blanca", "Mesa de centro", "250 kg"),
            art(4, 3, "Letrero LED \"LOVE\" Rosa", "Letrero neón flexible con cable 3m. Brillo ajustable. Ideal para fondos fotográficos.", 650.0, 5, "LN-003-ROS", "1.2m × 0.5m", "Neón LED flexible", "Cable 3m", "2 kg"),
            art(5, 3, "Lámpara Vintage Colgante", "Lámpara decorativa de luz cálida.", 260.0, 10, "LV-011-CAL", "0.4m de diámetro", "Metal y vidrio", "Foco LED", "3 kg"),
            art(6, 4, "Arco Orgánico Pastel XL", "Arco de globos orgánico tonos pastel. Incluye instalación y desmontaje. Personalizable.", 850.0, 8, "AG-002-PAS", "3m de ancho", "Globos de látex", "Instalación y desmontaje", null),
            art(7, 4, "Columnas de Globos Blancas", "Par de columnas con arreglo floral.", 480.0, 6, "AG-012-COL", "2.2m de alto", "Globos y flor artificial", "Base", null),
            art(8, 5, "Carrito Vintage para Dulces", "Carrito de madera blanca con ruedas doradas. Incluye faldón y decoración base.", 1800.0, 3, "CB-006-VIN", "1.2m × 0.6m × 1.4m", "Madera lacada", "Faldón y decoración", "60 kg"),
            art(9, 6, "Torre de Cupcakes 3 Niveles", "Torre acrílica para 60+ cupcakes en 3 niveles. Plateada o dorada. Totalmente personalizable.", 950.0, 10, "ST-007-ACR", "0.8m de alto", "Acrílico", "Para 60 cupcakes", "15 kg"),
            art(10, 7, "Fuente de Chocolate Premium", "Fuente 3 pisos con motor silencioso. Capacidad 4kg de chocolate. Incluye fondue de fresa.", 1400.0, 4, "FF-008-CHO", "0.6m de alto", "Acero inoxidable", "Fondue de fresa", "4 kg"),
            art(11, 8, "Mesa Decorativa de Postres", "Mesa vintage blanca 1.8m, 2 niveles con luces LED cálidas y faldón incluido.", 1200.0, 2, "MC-009-POS", "1.8m × 0.6m", "MDF lacado blanco", "Luces LED y faldón", "60 kg"),
            art(12, 8, "Mesa Candy Premium Dorada Deluxe", "Mesa decorativa de madera blanca con herrajes dorados. Perfecta para exposición de postres, dulces y bebidas en eventos. Incluye mantel de organza, faldón y 2 niveles de exhibición iluminados con tira LED cálida.", 1200.0, 12, "MC-001-ORO", "1.8m × 0.6m × 0.9m", "MDF lacado blanco", "Montaje + desmontaje", "80 kg"),
            art(13, 4, "Arco de Neón Circular", "Arco circular de neón (en reparación).", 900.0, 2, "AG-013-NEO", "1.5m de diámetro", "Neón LED", "Base", "5 kg", mant = true),
            art(14, 4, "Backdrop Floral Rosa", "Pared floral rosa (dado de baja).", 1500.0, 2, "AG-014-BCK", "2.4m × 2.4m", "Flor artificial", "Estructura", "20 kg", activo = false)
        ))
        reservaciones.clear()
        val hoy = LocalDate.now()
        val ahora = LocalDateTime.now()
        fun r(id: Long, cli: Long, d1: Long, d2: Long, dir: String, ent: String, notas: String?, lat: Double?, lng: Double?,
              est: String, flete: Double, motivo: String?, hace: Long, vararg items: Pair<Long, Int>) =
            reservaciones.add(nueva(id, cli, hoy.plusDays(d1), hoy.plusDays(d2), dir, ent, notas, lat, lng, est, flete, motivo, ahora.minusHours(hace), items.toList()))
        // Meses anteriores (llenan la gráfica de ingresos)
        r(700, 6, -75, -73, "Col. Roma Norte, CDMX", "INTERIOR", null, null, null, "APROBADO", 300.0, null, 1900, 12 to 1, 9 to 2, 1 to 1)
        r(710, 7, -45, -44, "Col. Condesa, CDMX", "EXTERIOR", null, null, null, "APROBADO", 450.0, null, 1200, 3 to 2, 6 to 1)
        r(720, 3, -20, -19, "Col. Santa Fe, CDMX", "INTERIOR", null, null, null, "APROBADO", 350.0, null, 600, 10 to 1, 4 to 1)
        // De María (cliente de la demostración)
        r(755, 2, -40, -39, "Av. Presidente Masaryk 134, Polanco", "INTERIOR", null, null, null, "RECHAZADO", 0.0, "Fecha ocupada: inventario insuficiente en esa zona.", 1100, 12 to 1, 6 to 1)
        r(779, 2, -25, -24, "Av. Presidente Masaryk 134, Polanco", "INTERIOR", null, 19.4326, -99.1960, "APROBADO", 300.0, null, 800, 4 to 1, 6 to 1)
        r(801, 2, 4, 5, "Av. Presidente Masaryk 134, Polanco, CDMX", "INTERIOR", "2do piso, portón negro. Tiene rampa.", 19.4326, -99.1960, "APROBADO", 450.0, null, 100, 3 to 1, 1 to 1)
        r(820, 2, 17, 17, "Av. Revolución 234, Col. Juárez, CDMX", "INTERIOR", null, null, null, "PENDIENTE", 0.0, null, 30, 3 to 1)
        r(847, 2, 7, 10, "Av. Presidente Masaryk 134, Polanco, CDMX", "INTERIOR", "2do piso, portón negro. Tiene rampa.", 19.4326, -99.1960, "PENDIENTE", 0.0, null, 4, 12 to 1, 6 to 1, 4 to 2)
        // De otros clientes (para la vista de la administradora)
        r(790, 6, 0, 1, "Col. Roma Norte, CDMX", "INTERIOR", "Entrega por la puerta trasera.", 19.4150, -99.1620, "APROBADO", 350.0, null, 60, 9 to 2, 10 to 1)
        r(811, 5, 10, 13, "Col. Coyoacán, CDMX", "EXTERIOR", null, 19.3500, -99.1620, "APROBADO", 400.0, null, 40, 8 to 3)
        r(830, 4, 19, 19, "Col. Condesa, CDMX", "EXTERIOR", null, null, null, "PENDIENTE", 0.0, null, 20, 1 to 1)
        r(851, 3, 8, 8, "Col. Santa Fe, CDMX", "INTERIOR", null, null, null, "PENDIENTE", 0.0, null, 10, 1 to 1, 2 to 4)
        r(853, 4, 12, 13, "Col. Condesa, CDMX", "INTERIOR", null, null, null, "PENDIENTE", 0.0, null, 6, 3 to 1, 12 to 1)
        Unit
    }

    // ---------- sesión (en la demostración se entra con las cuentas de prueba del login) ----------
    override suspend fun login(r: LoginRequest): Response<AuthResponse> = err(400, "Usa las cuentas de prueba que aparecen abajo")
    override suspend fun registro(r: RegistroRequest): Response<AuthResponse> = err(400, "Usa las cuentas de prueba que aparecen abajo")

    // ---------- catálogo ----------
    override suspend fun categorias(): Response<List<CategoriaDto>> = sim { ok(categorias) }

    override suspend fun articulos(desde: String?, hasta: String?): Response<List<ArticuloDto>> = sim {
        val d = desde?.let { LocalDate.parse(it) }
        val h = hasta?.let { LocalDate.parse(it) } ?: d
        ok(articulos.filter { it.activo && !it.enMantenimiento }.map { a -> conDatos(a, if (d == null) null else libres(a, d, h!!)) })
    }

    override suspend fun disponibilidad(items: String, mes: String): Response<List<String>> = sim {
        val pedido = items.split(",").mapNotNull { p -> p.split(":").takeIf { it.size == 2 }?.let { it[0].toLong() to it[1].toInt() } }
        val ym = YearMonth.parse(mes)
        val ocupadas = sortedSetOf<LocalDate>()
        for ((id, cant) in pedido) {
            val a = articulos.firstOrNull { it.id == id } ?: continue
            val cr = cruzadas(ym.atDay(1), ym.atEndOfMonth(), -1L)
            for (n in 1..ym.lengthOfMonth()) {
                val dia = ym.atDay(n)
                if (a.enMantenimiento || a.stockTotal - ocupadasEn(a, dia, cr) < cant) ocupadas.add(dia)
            }
        }
        ok(ocupadas.map { it.toString() })
    }

    // ---------- inventario ----------
    override suspend fun adminArticulos(): Response<List<ArticuloDto>> = sim {
        val hoy = LocalDate.now()
        ok(articulos.map { conDatos(it, libres(it, hoy, hoy)) })
    }

    private fun aArticulo(id: Long, r: ArticuloRequest, activo: Boolean): ArticuloDto? {
        val c = categorias.firstOrNull { it.id == r.categoriaId } ?: return null
        return ArticuloDto(id, c.id, c.nombre, c.seccion, r.nombre, r.descripcion, r.precioBase, r.stockTotal, null,
            r.imagenUrl, activo, r.sku, r.dimensiones, r.material, r.incluye, r.pesoMax, r.enMantenimiento, 0)
    }

    override suspend fun crearArticulo(r: ArticuloRequest): Response<ArticuloDto> = sim {
        val nuevo = aArticulo(sigArticulo++, r, true) ?: return@sim err(404, "Categoría no encontrada")
        articulos.add(nuevo); ok(nuevo)
    }

    override suspend fun actualizarArticulo(id: Long, r: ArticuloRequest): Response<ArticuloDto> = sim {
        val i = articulos.indexOfFirst { it.id == id }
        if (i < 0) return@sim err(404, "Artículo no encontrado")
        val nuevo = aArticulo(id, r, articulos[i].activo) ?: return@sim err(404, "Categoría no encontrada")
        articulos[i] = nuevo; ok(conDatos(nuevo, null))
    }

    private fun estado(id: Long, activo: Boolean): Response<ArticuloDto> {
        val i = articulos.indexOfFirst { it.id == id }
        if (i < 0) return err(404, "Artículo no encontrado")
        articulos[i] = articulos[i].copy(activo = activo); return ok(conDatos(articulos[i], null))
    }
    override suspend fun baja(id: Long): Response<ArticuloDto> = sim { estado(id, false) }
    override suspend fun alta(id: Long): Response<ArticuloDto> = sim { estado(id, true) }

    // ---------- reservaciones ----------
    override suspend fun crearReservacion(r: ReservacionRequest): Response<ReservacionDto> = sim {
        val u = session.session.value ?: return@sim err(401, "Inicia sesión")
        val fecha = try { LocalDate.parse(r.fechaEvento) } catch (e: Exception) { return@sim err(400, "Fecha inválida") }
        val fin = r.fechaFin?.let { LocalDate.parse(it) } ?: fecha
        if (fecha.isBefore(LocalDate.now())) return@sim err(409, "La fecha del evento no puede ser anterior a hoy")
        if (fin.isBefore(fecha)) return@sim err(409, "La recogida no puede ser antes de la entrega")
        if (r.items.isEmpty()) return@sim err(400, "items: no debe estar vacío")
        val pedido = r.items.groupBy { it.articuloId }.mapValues { e -> e.value.sumOf { it.cantidad } }.toSortedMap()
        for ((id, cant) in pedido) {
            val a = articulos.firstOrNull { it.id == id && it.activo } ?: return@sim err(404, "El artículo $id no está disponible")
            if (cant > a.stockTotal) return@sim err(409, "No hay suficientes piezas de ${a.nombre} en el negocio")
            if (cant > libres(a, fecha, fin)) return@sim err(409, "${a.nombre} no tiene disponibilidad en esas fechas, elige otras")
        }
        // Quien se registra en la demostración usa su propio nombre y correo
        val info: Triple<String, String, String?> = clientes[u.id] ?: Triple(u.nombre, u.correo, null)
        val creada = nueva(sigReservacion++, u.id, fecha, fin, r.direccion, r.entorno, r.notas, r.latitud, r.longitud,
            "PENDIENTE", 0.0, null, LocalDateTime.now(), pedido.map { it.key to it.value }, info)
        reservaciones.add(creada); ok(creada)
    }

    override suspend fun reservaciones(estado: String?): Response<List<ReservacionDto>> = sim {
        val u = session.session.value
        val base = if (u?.esAdmin == true) reservaciones else reservaciones.filter { it.clienteId == u?.id }
        ok(base.filter { estado == null || it.estado == estado }.sortedBy { it.fechaEvento })
    }

    override suspend fun reservacion(id: Long): Response<ReservacionDto> = sim {
        val r = reservaciones.firstOrNull { it.id == id } ?: return@sim err(404, "Reservación no encontrada")
        val u = session.session.value
        if (u?.esAdmin != true && r.clienteId != u?.id) return@sim err(404, "Reservación no encontrada")
        ok(r)
    }

    override suspend fun aprobar(id: Long, r: AprobacionRequest): Response<ReservacionDto> = sim {
        val i = reservaciones.indexOfFirst { it.id == id }
        if (i < 0) return@sim err(404, "Reservación no encontrada")
        val res = reservaciones[i]
        if (res.estado != "PENDIENTE") return@sim err(409, "Solo se pueden aprobar solicitudes pendientes")
        val flete = r.costoFlete ?: return@sim err(409, "Asigna el costo de flete antes de aprobar")
        for (d in res.detalles) {
            val a = articulos.first { it.id == d.articuloId }
            if (d.cantidad > libres(a, LocalDate.parse(res.fechaEvento), LocalDate.parse(res.fechaFin), res.id))
                return@sim err(409, "Ya no hay disponibilidad de ${a.nombre} para esas fechas")
        }
        val nuevo = res.copy(estado = "APROBADO", costoFlete = flete, total = redondear(res.subtotal + res.iva + flete), motivoRechazo = null)
        reservaciones[i] = nuevo; ok(nuevo)
    }

    override suspend fun rechazar(id: Long, r: RechazoRequest): Response<ReservacionDto> = sim {
        val i = reservaciones.indexOfFirst { it.id == id }
        if (i < 0) return@sim err(404, "Reservación no encontrada")
        if (reservaciones[i].estado != "PENDIENTE") return@sim err(409, "Solo se pueden rechazar solicitudes pendientes")
        val nuevo = reservaciones[i].copy(estado = "RECHAZADO", motivoRechazo = r.motivo)
        reservaciones[i] = nuevo; ok(nuevo)
    }

    override suspend fun nota(id: Long): Response<ResponseBody> = sim {
        val r = reservaciones.firstOrNull { it.id == id } ?: return@sim err(404, "Reservación no encontrada")
        val u = session.session.value
        if (u?.esAdmin != true && r.clienteId != u?.id) return@sim err(404, "Reservación no encontrada")
        if (r.estado != "APROBADO") return@sim err(409, "La nota de remisión solo está disponible para solicitudes aprobadas")
        ok(NotaDemoPdf.generar(r).toResponseBody("application/pdf".toMediaType()))
    }

    // ---------- dashboard ----------
    override suspend fun dashboard(): Response<DashboardDto> = sim {
        val aprobadas = reservaciones.filter { it.estado == "APROBADO" }
        val hoy = LocalDate.now(); val actual = YearMonth.from(hoy)
        fun suma(ym: YearMonth?) = aprobadas.filter { ym == null || YearMonth.from(LocalDate.parse(it.fechaEvento)) == ym }.sumOf { it.total }
        val mes = suma(actual); val pasado = suma(actual.minusMonths(1))
        val variacion = if (pasado == 0.0) 0 else Math.round((mes - pasado) * 100 / pasado).toInt()
        val dias = mutableSetOf<LocalDate>()
        aprobadas.forEach { r ->
            var d = LocalDate.parse(r.fechaEvento); val f = LocalDate.parse(r.fechaFin)
            while (!d.isAfter(f)) { if (YearMonth.from(d) == actual) dias.add(d); d = d.plusDays(1) }
        }
        val meses = (5 downTo 0).map { n ->
            val ym = actual.minusMonths(n.toLong())
            MesIngreso(ym.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX")), suma(ym))
        }
        val conteo = aprobadas.flatMap { it.detalles }.groupBy { it.nombre }.map { it.key to it.value.sumOf { d -> d.cantidad } }
        val totalPiezas = max(1, conteo.sumOf { it.second })
        val top = conteo.sortedByDescending { it.second }.take(4).map { TopArticulo(it.first, it.second, Math.round(100f * it.second / totalPiezas)) }
        ok(DashboardDto(
            pendientes = reservaciones.count { it.estado == "PENDIENTE" }, aprobados = aprobadas.size,
            ingresosMes = mes, ingresosTotales = suma(null), variacionMes = variacion,
            ocupacionMes = Math.round(100.0 * dias.size / actual.lengthOfMonth()).toInt(),
            ingresosMensuales = meses, topArticulos = top,
            recientes = reservaciones.sortedByDescending { it.creadoEn }.take(5),
            entregasHoy = aprobadas.filter { it.fechaEvento == hoy.toString() },
            proxima = aprobadas.filter { !LocalDate.parse(it.fechaEvento).isBefore(hoy) }.minByOrNull { it.fechaEvento }
        ))
    }
}

/** Sube "evidencias" de mentira: tarda un poco para que se vea la barra de progreso. */
@Singleton
class FakeEvidenceApi @Inject constructor() : EvidenceApi {
    override suspend fun upload(
        photo: MultipartBody.Part, id: RequestBody, folio: RequestBody, tipo: RequestBody,
        comentario: RequestBody, latitud: RequestBody, longitud: RequestBody,
        precision: RequestBody, capturadoEn: RequestBody
    ): Response<Unit> {
        delay(1500)
        return Response.success(Unit)
    }
}
