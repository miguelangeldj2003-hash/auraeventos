package com.aura.eventos.data.demo

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.aura.eventos.data.remote.ReservacionDto
import com.aura.eventos.ui.components.fechaLarga
import com.aura.eventos.ui.components.mxn
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale

/** En modo demo la Nota de Remisión se dibuja en el teléfono con el mismo diseño que genera la API. */
object NotaDemoPdf {
    private const val NAVY = 0xFF0B1B32.toInt()
    private const val VERDE = 0xFF12B88F.toInt()
    private const val GRIS = 0xFFF4F8FB.toInt()
    private const val DORADO = 0xFFC9A24D.toInt()
    private const val BLANCO = 0xFFFFFFFF.toInt()
    private const val SUAVE = 0xFF6B778C.toInt()
    private const val CLARO = 0xFFB8C4D6.toInt()

    fun generar(r: ReservacionDto): ByteArray {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val c = page.canvas

        fun rect(l: Float, t: Float, rr: Float, b: Float, color: Int) =
            c.drawRect(l, t, rr, b, Paint().apply { this.color = color; style = Paint.Style.FILL })
        fun txt(s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
            c.drawText(s, x, y, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; textSize = size; textAlign = align
                typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            })

        // Encabezado
        rect(0f, 0f, 595f, 104f, NAVY)
        txt("AURA", 36f, 52f, 24f, BLANCO, true)
        txt("EVENTOS", 100f, 52f, 24f, VERDE, true)
        txt("Renta de mobiliario y candy bar para eventos · México", 36f, 74f, 8.5f, CLARO)
        txt("NOTA DE REMISIÓN", 559f, 40f, 16f, BLANCO, true, Paint.Align.RIGHT)
        txt("#${r.folio}", 559f, 58f, 10.5f, VERDE, true, Paint.Align.RIGHT)
        txt("Fecha de emisión: ${LocalDate.now().toString().fechaLarga()}", 559f, 74f, 8.5f, CLARO, false, Paint.Align.RIGHT)
        txt("● " + when (r.estado) { "APROBADO" -> "APROBADO"; "RECHAZADO" -> "RECHAZADO"; else -> "PENDIENTE DE APROBACIÓN" },
            559f, 90f, 8.5f, VERDE, true, Paint.Align.RIGHT)

        // Datos del cliente y del evento
        var y = 136f
        txt("DATOS DEL CLIENTE", 36f, y, 8f, DORADO, true); txt("DETALLES DEL EVENTO", 310f, y, 8f, DORADO, true)
        y += 16f
        fun par(e1: String, v1: String, e2: String, v2: String) {
            txt("$e1:", 36f, y, 8.5f, SUAVE); txt(v1.take(34), 110f, y, 9.5f, NAVY, true)
            txt("$e2:", 310f, y, 8.5f, SUAVE); txt(v2.take(36), 410f, y, 9.5f, NAVY, true)
            y += 16f
        }
        val rango = if (r.fechaEvento == r.fechaFin) r.fechaEvento.fechaLarga() else "${r.fechaEvento.fechaLarga()} al ${r.fechaFin.fechaLarga()}"
        par("Nombre", r.cliente, "Fecha del evento", rango)
        par("Teléfono", r.telefono ?: "—", "Días de renta", "${r.dias} ${if (r.dias == 1) "día" else "días"}")
        par("Correo", r.correo ?: "—", "Entorno", r.entorno.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() })
        txt("Dirección de entrega:", 310f, y, 8.5f, SUAVE); txt(r.direccion.take(40), 410f, y, 9.5f, NAVY, true)
        y += 30f

        // Tabla de artículos
        txt("ARTÍCULOS RENTADOS", 36f, y, 8f, DORADO, true)
        y += 8f
        rect(36f, y, 559f, y + 24f, NAVY)
        txt("CÓDIGO", 44f, y + 16f, 8f, BLANCO, true)
        txt("DESCRIPCIÓN", 120f, y + 16f, 8f, BLANCO, true)
        txt("CANT.", 340f, y + 16f, 8f, BLANCO, true, Paint.Align.CENTER)
        txt("DÍAS", 385f, y + 16f, 8f, BLANCO, true, Paint.Align.CENTER)
        txt("PRECIO", 465f, y + 16f, 8f, BLANCO, true, Paint.Align.RIGHT)
        txt("IMPORTE", 551f, y + 16f, 8f, BLANCO, true, Paint.Align.RIGHT)
        y += 24f
        var i = 0
        fun fila(codigo: String, desc: String, cant: String, dias: String, precio: String, importe: String, colorCodigo: Int) {
            rect(36f, y, 559f, y + 28f, if (i % 2 == 0) BLANCO else GRIS)
            txt(codigo.take(12), 44f, y + 18f, 8f, colorCodigo, true)
            txt(desc.take(34), 120f, y + 18f, 9.5f, NAVY, true)
            txt(cant, 340f, y + 18f, 9.5f, NAVY, false, Paint.Align.CENTER)
            txt(dias, 385f, y + 18f, 9.5f, NAVY, false, Paint.Align.CENTER)
            txt(precio, 465f, y + 18f, 9.5f, NAVY, false, Paint.Align.RIGHT)
            txt(importe, 551f, y + 18f, 9.5f, NAVY, true, Paint.Align.RIGHT)
            y += 28f; i++
        }
        r.detalles.forEach { fila(it.sku ?: "—", it.nombre, "${it.cantidad}", "${r.dias}", it.precioUnitario.mxn(), it.subtotal.mxn(), VERDE) }
        fila("FLT", "Flete / Envío", "—", "—", r.costoFlete.mxn(), r.costoFlete.mxn(), DORADO)

        // Totales
        y += 14f
        fun total(e: String, v: String) {
            rect(330f, y - 14f, 559f, y + 8f, GRIS)
            txt(e, 342f, y, 9f, NAVY); txt(v, 551f, y, 9.5f, NAVY, true, Paint.Align.RIGHT); y += 24f
        }
        total("Subtotal", r.subtotal.mxn()); total("IVA (16%)", r.iva.mxn()); total("Flete / Envío", r.costoFlete.mxn())
        rect(330f, y - 14f, 559f, y + 16f, NAVY)
        rect(330f, y - 14f, 559f, y - 12f, DORADO)
        txt("TOTAL A PAGAR", 342f, y + 5f, 9f, BLANCO, true)
        txt(r.total.mxn(), 551f, y + 6f, 13f, VERDE, true, Paint.Align.RIGHT)
        y += 48f

        // Notas y firmas
        txt("NOTAS Y CONDICIONES", 36f, y, 8f, DORADO, true); y += 14f
        listOf(
            "• Documento informativo del apartado. Dentro de la aplicación no se realizan cobros electrónicos.",
            "• El costo del flete fue asignado por la administradora al aprobar la solicitud.",
            "• En caso de cancelación con menos de 72 horas de anticipación, se aplicará cargo del 50% del subtotal.",
            "• Los precios incluyen IVA únicamente sobre el valor de renta. El flete no lleva IVA."
        ).forEach { txt(it, 36f, y, 7.5f, SUAVE); y += 12f }
        r.notas?.takeIf { it.isNotBlank() }?.let { txt("• Notas del cliente: ${it.take(90)}", 36f, y, 7.5f, SUAVE); y += 12f }

        y = 760f
        listOf(36f to "FIRMA DEL CLIENTE", 215f to "ASESOR AURA EVENTOS", 410f to "SELLO EMPRESA").forEach { (x, t) ->
            c.drawLine(x, y, x + 150f, y, Paint().apply { color = NAVY; strokeWidth = 1f })
            txt(t, x, y + 12f, 7.5f, NAVY, true)
        }
        txt("AURA EVENTOS · Nota de remisión #${r.folio} · Documento generado automáticamente", 297f, 815f, 7f, 0xFF888888.toInt(), false, Paint.Align.CENTER)

        doc.finishPage(page)
        val salida = ByteArrayOutputStream()
        doc.writeTo(salida)
        doc.close()
        return salida.toByteArray()
    }
}
