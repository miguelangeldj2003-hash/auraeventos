package com.aura.eventos

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import kotlin.math.max

class AuraUnitTest {

    // UT-01 IVA 16 % solo sobre subtotal
    @Test
    fun testUT01_ivaSoloSobreSubtotal() {
        val subtotal = 2500.00
        val flete = 450.00
        val ivaCalculado = Math.round(subtotal * 0.16 * 100.0) / 100.0
        val ivaEsperado = 400.00

        assertEquals(ivaEsperado, ivaCalculado, 0.001)
        // Verificar que el flete no altera el calculo del IVA
        val ivaConFlete = Math.round((subtotal + flete) * 0.16 * 100.0) / 100.0
        assertNotEquals(ivaConFlete, ivaCalculado)
    }

    // UT-02 total = subtotal + IVA + flete
    @Test
    fun testUT02_totalEsSubtotalMasIvaMasFlete() {
        val subtotal = 3000.00
        val iva = Math.round(subtotal * 0.16 * 100.0) / 100.0 // 480.00
        val flete = 350.00
        val totalCalculado = Math.round((subtotal + iva + flete) * 100.0) / 100.0
        val totalEsperado = 3830.00

        assertEquals(totalEsperado, totalCalculado, 0.001)
    }

    // UT-03 correo inválido
    @Test
    fun testUT03_correoInvalido() {
        val regexCorreo = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        val correosInvalidos = listOf(
            "correo-sin-arroba.com",
            "maria@",
            "@gmail.com",
            "maria@gmail",
            "maria gmail.com",
            ""
        )

        for (correo in correosInvalidos) {
            val esValido = regexCorreo.matches(correo.trim())
            assertFalse("El correo '$correo' debería ser considerado inválido", esValido)
        }

        assertTrue("El correo 'maria@gmail.com' debería ser considerado válido", regexCorreo.matches("maria@gmail.com"))
    }

    // UT-04 contraseña de 7 caracteres inválida
    @Test
    fun testUT04_contrasenaInvalidaSieteCaracteres() {
        val minLongitud = 8
        val passSieteCaracteres = "Abc123*" // 7 caracteres

        val esValida = passSieteCaracteres.length >= minLongitud

        assertFalse("Una contraseña de 7 caracteres debe ser rechazada", esValida)

        val passOchoCaracteres = "Abc123*9" // 8 caracteres
        assertTrue("Una contraseña de 8 o más caracteres debe ser aceptada", passOchoCaracteres.length >= minLongitud)
    }

    // UT-05 no pedir más de lo libre en cualquier día del rango (modo demo)
    @Test
    fun testUT05_noPedirMasDeLoLibreEnCualquierDiaDelRango() {
        val stockTotal = 10
        val inicioRango = LocalDate.of(2026, 11, 10)
        val finRango = LocalDate.of(2026, 11, 12)

        // Simular reservas aprobadas en el rango:
        // Día 10: 2 reservadas (quedan 8)
        // Día 11: 7 reservadas (quedan 3) <- Día pico / peor ocupación
        // Día 12: 1 reservada (quedan 9)
        val ocupacionPorDia = mapOf(
            LocalDate.of(2026, 11, 10) to 2,
            LocalDate.of(2026, 11, 11) to 7,
            LocalDate.of(2026, 11, 12) to 1
        )

        // Calcular piezas libres (mínima disponibilidad en todo el rango)
        var peorOcupacion = 0
        var d = inicioRango
        while (!d.isAfter(finRango)) {
            val ocupadas = ocupacionPorDia[d] ?: 0
            peorOcupacion = max(peorOcupacion, ocupadas)
            d = d.plusDays(1)
        }
        val disponiblesLibres = max(0, stockTotal - peorOcupacion) // 10 - 7 = 3

        assertEquals(3, disponiblesLibres)

        val cantidadSolicitadaPasa = 3
        assertTrue("Solicitar hasta el máximo libre (3) debe permitirse", cantidadSolicitadaPasa <= disponiblesLibres)

        val cantidadSolicitadaExcede = 4
        assertFalse("Solicitar más de lo libre en cualquier día del rango (4 > 3) debe ser rechazado", cantidadSolicitadaExcede <= disponiblesLibres)
    }
}
