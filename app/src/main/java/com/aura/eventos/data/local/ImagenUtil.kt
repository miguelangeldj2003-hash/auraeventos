package com.aura.eventos.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImagenUtil {
    suspend fun prepararDesdeUri(
        ctx: Context,
        uri: Uri,
        maxLado: Int = 1280,
        calidad: Int = 82
    ): File = withContext(Dispatchers.IO) {
        val inputStream: InputStream = ctx.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el archivo seleccionado")
        val bytes = inputStream.readBytes()
        inputStream.close()

        if (bytes.isEmpty()) throw IllegalArgumentException("El archivo seleccionado está vacío")

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight

        if (origW <= 0 || origH <= 0) throw IllegalArgumentException("Formato de imagen no soportado")

        var sampleSize = 1
        while (origW / sampleSize > maxLado * 2 || origH / sampleSize > maxLado * 2) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmapOriginal = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
            ?: throw IllegalArgumentException("No se pudo decodificar la imagen")

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

        val width = bitmapOriginal.width
        val height = bitmapOriginal.height
        val maxDim = maxLado.toFloat()
        val scale = if (width > maxDim || height > maxDim) {
            maxDim / maxOf(width, height)
        } else {
            1f
        }
        matrix.postScale(scale, scale)

        val bitmapFinal = Bitmap.createBitmap(
            bitmapOriginal, 0, 0, width, height, matrix, true
        )

        val dir = File(ctx.filesDir, "articulos").apply { mkdirs() }
        val destFile = File(dir, "foto_${System.currentTimeMillis()}.jpg")
        FileOutputStream(destFile).use { out ->
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, calidad, out)
        }

        if (bitmapOriginal != bitmapFinal && !bitmapOriginal.isRecycled) {
            bitmapOriginal.recycle()
        }
        if (!bitmapFinal.isRecycled) {
            bitmapFinal.recycle()
        }

        destFile
    }
}
