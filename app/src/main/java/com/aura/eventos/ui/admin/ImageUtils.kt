package com.aura.eventos.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Procesa una imagen elegida de la galería:
     * 1. Corrige la rotación EXIF.
     * 2. La escala a máximo 1280 px (ancho o alto) manteniendo la relación de aspecto.
     * 3. La comprime en formato JPG con calidad 82.
     * 4. La guarda en filesDir/articulos.
     * @return El archivo File generado o null en caso de error.
     */
    fun procesarImagenGaleria(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) return null

            // 1. Obtener dimensiones originales
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
            val origW = boundsOptions.outWidth
            val origH = boundsOptions.outHeight

            if (origW <= 0 || origH <= 0) return null

            // 2. Submuestreo para ahorrar memoria en imágenes grandes
            var sampleSize = 1
            while (origW / sampleSize > 2560 || origH / sampleSize > 2560) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

            // 3. Orientación EXIF
            val exifInterface = try {
                ExifInterface(bytes.inputStream())
            } catch (e: Exception) {
                null
            }
            val orientation = exifInterface?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            // 4. Escalar a máximo 1280 px
            val width = bitmapOriginal.width
            val height = bitmapOriginal.height
            val maxDimension = 1280f
            val scale = if (width > maxDimension || height > maxDimension) {
                maxDimension / maxOf(width, height)
            } else {
                1f
            }
            matrix.postScale(scale, scale)

            val bitmapFinal = Bitmap.createBitmap(
                bitmapOriginal, 0, 0, width, height, matrix, true
            )

            // 5. Guardar en filesDir/articulos como JPG calidad 82
            val dir = File(context.filesDir, "articulos").apply { mkdirs() }
            val destFile = File(dir, "ART_TMP_${System.currentTimeMillis()}.jpg")
            FileOutputStream(destFile).use { out ->
                bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 82, out)
            }

            if (bitmapOriginal != bitmapFinal && !bitmapOriginal.isRecycled) {
                bitmapOriginal.recycle()
            }
            if (!bitmapFinal.isRecycled) {
                bitmapFinal.recycle()
            }

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
