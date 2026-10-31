package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageCompressor {
    /**
     * Reads image from Uri, auto-rotates according to EXIF data, downscales
     * to maxWidth/maxHeight (default 1600px), and compresses to JPEG (quality 80%).
     * Returns ByteArray suitable for network upload (~100KB - 500KB).
     */
    fun compressImageUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1600,
        maxHeight: Int = 1600,
        quality: Int = 80
    ): ByteArray? {
        return try {
            // First decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            // Calculate sample size
            var inSampleSize = 1
            while (srcWidth / inSampleSize > maxWidth || srcHeight / inSampleSize > maxHeight) {
                inSampleSize *= 2
            }

            // Decode actual bitmap with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return null

            // Rotate based on EXIF orientation
            try {
                val exifStream = context.contentResolver.openInputStream(uri)
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED
                    )
                    exifStream.close()

                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                        )
                        if (rotatedBitmap != bitmap) {
                            bitmap.recycle()
                            bitmap = rotatedBitmap
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore EXIF errors
            }

            // Scale down if still slightly over max bounds
            if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                val scale = Math.min(
                    maxWidth.toFloat() / bitmap.width,
                    maxHeight.toFloat() / bitmap.height
                )
                val newW = (bitmap.width * scale).toInt()
                val newH = (bitmap.height * scale).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }

            // Compress to JPEG
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val bytes = baos.toByteArray()
            bitmap.recycle()
            bytes
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: raw read if compression fails
            try {
                val isRaw = context.contentResolver.openInputStream(uri)
                val rawBytes = isRaw?.readBytes()
                isRaw?.close()
                rawBytes
            } catch (_: Exception) {
                null
            }
        }
    }
}
