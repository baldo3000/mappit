package me.baldo.mappit.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Converts bitmap to byte array in PNG format
 * @param bitmap source bitmap
 * @return result byte array
 */
fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray? {
    var baos: ByteArrayOutputStream? = null
    try {
        baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    } finally {
        if (baos != null) {
            try {
                baos.close()
            } catch (e: IOException) {
                Log.e(
                    "TAG",
                    "ByteArrayOutputStream was not closed"
                )
            }
        }
    }
}

/**
 * Converts bitmap to the byte array without compression
 * @param bitmap source bitmap
 * @return result byte array
 */
fun convertBitmapToByteArrayUncompressed(bitmap: Bitmap): ByteArray? {
    val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
    bitmap.copyPixelsToBuffer(byteBuffer)
    byteBuffer.rewind()
    return byteBuffer.array()
}

/**
 * Converts compressed byte array to bitmap
 * @param src source array
 * @return result bitmap
 */
fun convertCompressedByteArrayToBitmap(src: ByteArray): Bitmap? {
    return BitmapFactory.decodeByteArray(src, 0, src.size)
}
