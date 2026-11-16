package com.aura.eventos.api.pdf;

import com.aura.eventos.api.model.Reservacion;
import com.aura.eventos.api.model.ReservacionDetalle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

public class NotaRemisionPdf {

    private static final Color NAVY = new Color(0x0B, 0x1B, 0x32);
    private static final Color GREEN = new Color(0x12, 0xB8, 0x8F);
    private static final Color GOLD = new Color(0xC9, 0xA2, 0x4D);

    public static byte[] generar(Reservacion r) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header Banner
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{60, 40});

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, NAVY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font folioFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, GREEN);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("AURA EVENTOS", titleFont));
            leftCell.addElement(new Paragraph("Renta de mobiliario y candy bar para eventos", subtitleFont));
            header.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(new Paragraph("NOTA DE REMISIÓN", titleFont));
            rightCell.addElement(new Paragraph("#" + r.getFolio(), folioFont));
            header.addCell(rightCell);

            document.add(header);
            document.add(new Paragraph(" "));

            // Details table
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 45, 10, 15, 15});

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            String[] headers = {"SKU", "Artículo", "Cant.", "P. Unitario", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setBackgroundColor(NAVY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font itemFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (ReservacionDetalle d : r.getDetalles()) {
                table.addCell(new PdfPCell(new Phrase(d.getArticulo().getSku() != null ? d.getArticulo().getSku() : "—", itemFont)));
                table.addCell(new PdfPCell(new Phrase(d.getArticulo().getNombre(), itemFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), itemFont)));
                table.addCell(new PdfPCell(new Phrase(fmt.format(d.getPrecioUnitario()), itemFont)));
                table.addCell(new PdfPCell(new Phrase(fmt.format(d.getSubtotal()), itemFont)));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Totals Summary
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NAVY);
            Paragraph pSub = new Paragraph("Subtotal: " + fmt.format(r.getSubtotal()), totalFont);
            Paragraph pIva = new Paragraph("IVA (16%): " + fmt.format(r.getIva()), totalFont);
            Paragraph pFlete = new Paragraph("Flete: " + fmt.format(r.getCostoFlete()), totalFont);
            Paragraph pTotal = new Paragraph("TOTAL: " + fmt.format(r.getTotal()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, GREEN));

            pSub.setAlignment(Element.ALIGN_RIGHT);
            pIva.setAlignment(Element.ALIGN_RIGHT);
            pFlete.setAlignment(Element.ALIGN_RIGHT);
            pTotal.setAlignment(Element.ALIGN_RIGHT);

            document.add(pSub);
            document.add(pIva);
            document.add(pFlete);
            document.add(pTotal);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la Nota de Remisión: " + e.getMessage(), e);
        }
    }
}
