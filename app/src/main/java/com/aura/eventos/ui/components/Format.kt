package com.aura.eventos.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MX = Locale("es", "MX")

fun Double.mxn(): String = NumberFormat.getCurrencyInstance(MX).format(this)
fun Double.mxnSinCentavos(): String = "$" + NumberFormat.getIntegerInstance(MX).format(Math.round(this))

private fun parse(s: String): LocalDate? = runCatching { LocalDate.parse(s.take(10)) }.getOrNull()

/** 21 jul 2026 */
fun String.fechaBonita(): String = parse(this)?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", MX)) ?: this
/** 21 de julio de 2026 */
fun String.fechaLarga(): String = parse(this)?.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", MX)) ?: this
/** 21 jul */
fun String.fechaCorta(): String = parse(this)?.format(DateTimeFormatter.ofPattern("dd MMM", MX)) ?: this

/** "21–24 jul 2026" o "21 jul 2026" si es de un solo día. */
fun rangoFechas(inicio: String, fin: String): String {
    val a = parse(inicio) ?: return inicio
    val b = parse(fin) ?: return inicio
    if (a == b) return inicio.fechaBonita()
    val mes = DateTimeFormatter.ofPattern("MMM yyyy", MX)
    return if (a.month == b.month && a.year == b.year) "${a.dayOfMonth}–${b.dayOfMonth} ${a.format(mes)}"
    else "${inicio.fechaCorta()} – ${fin.fechaBonita()}"
}

/** Hora legible de la solicitud: "hoy 09:14", "ayer 18:30" o la fecha. */
fun String?.recibida(): String {
    val f = runCatching { LocalDateTime.parse(this) }.getOrNull() ?: return ""
    val hora = f.format(DateTimeFormatter.ofPattern("HH:mm"))
    return when (f.toLocalDate()) {
        LocalDate.now() -> "hoy a las $hora"
        LocalDate.now().minusDays(1) -> "ayer a las $hora"
        else -> f.toLocalDate().toString().fechaBonita()
    }
}

/** El folio se muestra con # como en la web. */
fun String.folioVisible(): String = if (startsWith("#")) this else "#$this"

fun String.iniciales(): String =
    split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }

fun abrirPdf(ctx: Context, archivo: File) {
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try { ctx.startActivity(Intent.createChooser(intent, "Abrir nota de remisión")) }
    catch (e: ActivityNotFoundException) { Toast.makeText(ctx, "Instala un lector de PDF para abrir la nota", Toast.LENGTH_LONG).show() }
}

fun abrirMapa(ctx: Context, lat: Double, lng: Double, etiqueta: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(etiqueta)})")
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    catch (e: ActivityNotFoundException) { Toast.makeText(ctx, "No hay una app de mapas instalada", Toast.LENGTH_LONG).show() }
}
