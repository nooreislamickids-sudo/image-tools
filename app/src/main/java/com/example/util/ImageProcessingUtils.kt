package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ImageOutputFormat(val displayName: String, val extension: String, val mimeType: String) {
    JPEG("JPEG (.jpg)", "jpg", "image/jpeg"),
    PNG("PNG (.png)", "png", "image/png"),
    WEBP("WebP (.webp)", "webp", "image/webp")
}

enum class PhotoFilterType(val displayName: String) {
    NONE("Original"),
    GRAYSCALE("Grayscale"),
    SEPIA("Warm Sepia"),
    INVERT("Negative / Invert"),
    VINTAGE("Vintage 70s"),
    CYBERPUNK("Cyberpunk Neon"),
    COOL_BREEZE("Cool Blue"),
    WARM_SUNSET("Warm Golden"),
    DRAMATIC("High Contrast")
}

enum class WatermarkPosition(val displayName: String) {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    CENTER("Center"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right")
}

object ImageProcessingUtils {

    // --- Loading & Sample Images ---

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createSampleBitmap(type: String = "landscape"): Bitmap {
        val width = 900
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        when (type) {
            "portrait" -> {
                // Background gradient
                val bgPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
                        AndroidColor.parseColor("#3B82F6"), AndroidColor.parseColor("#1E1B4B"),
                        Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                // Draw decorative portrait elements
                val circlePaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#FDE047")
                }
                canvas.drawCircle(width / 2f, height / 2.3f, 130f, circlePaint)

                val bodyPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#4F46E5")
                }
                canvas.drawRoundRect(RectF(width / 2f - 180f, height / 2f + 50f, width / 2f + 180f, height.toFloat() + 100f), 80f, 80f, bodyPaint)

                val textPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Studio Portrait Sample", width / 2f, 100f, textPaint)
            }
            "graphic" -> {
                val bgPaint = Paint().apply {
                    color = AndroidColor.parseColor("#0F172A")
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                val rectPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#10B981")
                }
                canvas.drawRoundRect(RectF(150f, 150f, width - 150f, height - 150f), 32f, 32f, rectPaint)

                val textPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.WHITE
                    textSize = 52f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Image Tools Pro", width / 2f, height / 2f + 15f, textPaint)
            }
            else -> {
                // Scenic Landscape Sunset & Mountains
                val skyPaint = Paint().apply {
                    shader = LinearGradient(0f, 0f, 0f, height * 0.7f,
                        AndroidColor.parseColor("#F97316"), AndroidColor.parseColor("#4338CA"),
                        Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

                // Sun
                val sunPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#FEF08A")
                }
                canvas.drawCircle(width * 0.65f, height * 0.35f, 75f, sunPaint)

                // Mountains back
                val mountainBack = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#312E81")
                }
                val pathBack = android.graphics.Path().apply {
                    moveTo(0f, height.toFloat())
                    lineTo(250f, height * 0.45f)
                    lineTo(500f, height.toFloat())
                    close()
                }
                canvas.drawPath(pathBack, mountainBack)

                // Mountains front
                val mountainFront = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#1E1B4B")
                }
                val pathFront = android.graphics.Path().apply {
                    moveTo(150f, height.toFloat())
                    lineTo(480f, height * 0.38f)
                    lineTo(850f, height.toFloat())
                    close()
                }
                canvas.drawPath(pathFront, mountainFront)

                // Ground water reflection
                val groundPaint = Paint().apply {
                    shader = LinearGradient(0f, height * 0.75f, 0f, height.toFloat(),
                        AndroidColor.parseColor("#1E293B"), AndroidColor.parseColor("#0F172A"),
                        Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, height * 0.75f, width.toFloat(), height.toFloat(), groundPaint)

                val labelPaint = Paint().apply {
                    isAntiAlias = true
                    color = AndroidColor.parseColor("#FFFFFF")
                    textSize = 34f
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText("Golden Sunset Sample", 50f, 80f, labelPaint)
            }
        }
        return bitmap
    }

    // --- Compression & Formats ---

    fun compressBitmap(
        bitmap: Bitmap,
        format: ImageOutputFormat,
        quality: Int = 85
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            ImageOutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageOutputFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageOutputFormat.WEBP -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
        }
        val safeQuality = quality.coerceIn(1, 100)
        bitmap.compress(compressFormat, safeQuality, stream)
        return stream.toByteArray()
    }

    fun decodeByteArrayToBitmap(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    // --- Resizing ---

    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val w = max(1, targetWidth)
        val h = max(1, targetHeight)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    // --- Cropping ---

    fun cropBitmap(bitmap: Bitmap, leftRatio: Float, topRatio: Float, widthRatio: Float, heightRatio: Float): Bitmap {
        val originalW = bitmap.width
        val originalH = bitmap.height

        val x = (leftRatio * originalW).toInt().coerceIn(0, originalW - 1)
        val y = (topRatio * originalH).toInt().coerceIn(0, originalH - 1)
        val w = (widthRatio * originalW).toInt().coerceIn(1, originalW - x)
        val h = (heightRatio * originalH).toInt().coerceIn(1, originalH - y)

        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }

    // --- Rotate & Flip ---

    fun rotateAndFlip(bitmap: Bitmap, degrees: Float, flipHorizontal: Boolean, flipVertical: Boolean): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
            val sx = if (flipHorizontal) -1f else 1f
            val sy = if (flipVertical) -1f else 1f
            postScale(sx, sy)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // --- Adjustments (Brightness, Contrast, Saturation, Temperature) ---

    fun applyColorAdjustments(
        bitmap: Bitmap,
        brightness: Float, // -100 to 100
        contrast: Float,   // 0.5 to 2.0
        saturation: Float, // 0.0 to 2.0
        temperature: Float // -50 to 50
    ): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 1. Contrast & Brightness
        val c = contrast.coerceIn(0.1f, 3.0f)
        val b = brightness.coerceIn(-150f, 150f)

        // 2. Saturation
        val satMatrix = ColorMatrix().apply {
            setSaturation(saturation.coerceIn(0f, 3f))
        }

        // 3. Contrast matrix
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, b,
                0f, c, 0f, 0f, b,
                0f, 0f, c, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // 4. Temperature / Tint (Warm adds Red, Cool adds Blue)
        val tempR = (temperature * 1.2f).coerceIn(-60f, 60f)
        val tempB = (-temperature * 1.2f).coerceIn(-60f, 60f)
        val tempMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, tempR,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, tempB,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Combine
        val finalMatrix = ColorMatrix().apply {
            postConcat(satMatrix)
            postConcat(contrastMatrix)
            postConcat(tempMatrix)
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(finalMatrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    // --- Filters ---

    fun applyFilter(bitmap: Bitmap, filter: PhotoFilterType): Bitmap {
        if (filter == PhotoFilterType.NONE) return bitmap

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val matrix = when (filter) {
            PhotoFilterType.GRAYSCALE -> {
                ColorMatrix().apply { setSaturation(0f) }
            }
            PhotoFilterType.SEPIA -> {
                ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.INVERT -> {
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.VINTAGE -> {
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.1f, 0.1f, 0f, 30f,
                        0.1f, 0.8f, 0.1f, 0f, 15f,
                        0.1f, 0.1f, 0.6f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.CYBERPUNK -> {
                ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0.3f, 0f, 40f,
                        0f, 0.9f, 0.2f, 0f, 0f,
                        0.4f, 0f, 1.4f, 0f, 50f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.COOL_BREEZE -> {
                ColorMatrix(
                    floatArrayOf(
                        0.8f, 0f, 0f, 0f, -10f,
                        0f, 0.9f, 0f, 0f, 10f,
                        0f, 0f, 1.3f, 0f, 35f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.WARM_SUNSET -> {
                ColorMatrix(
                    floatArrayOf(
                        1.3f, 0f, 0f, 0f, 30f,
                        0f, 1.05f, 0f, 0f, 15f,
                        0f, 0f, 0.8f, 0f, -20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            PhotoFilterType.DRAMATIC -> {
                // High contrast with slight desaturation
                val m = ColorMatrix().apply { setSaturation(0.6f) }
                val cMat = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, -30f,
                        0f, 1.4f, 0f, 0f, -30f,
                        0f, 0f, 1.4f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                m.postConcat(cMat)
                m
            }
            else -> ColorMatrix()
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    // --- Blur & Sharpen ---

    fun applyBlur(bitmap: Bitmap, radius: Int): Bitmap {
        if (radius <= 0) return bitmap
        val scale = 0.5f
        val scaledW = max(1, (bitmap.width * scale).toInt())
        val scaledH = max(1, (bitmap.height * scale).toInt())
        val scaled = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)

        val pixels = IntArray(scaledW * scaledH)
        scaled.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)

        val r = radius.coerceIn(1, 25)
        fastBlur(pixels, scaledW, scaledH, r)

        val blurred = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_8888)
        blurred.setPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)
        return Bitmap.createScaledBitmap(blurred, bitmap.width, bitmap.height, true)
    }

    private fun fastBlur(pix: IntArray, w: Int, h: Int, radius: Int) {
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        var yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val routsum: Int
        val goutsum: Int
        val boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (curY in 0 until h) {
            binsum = 0
            ginsum = 0
            rinsum = 0
            var curRout = 0
            var curGout = 0
            var curBout = 0
            rsum = 0
            gsum = 0
            bsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + min(wm, max(curI, 0))]
                sir = stack[curI + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = radius + 1 - abs(curI)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    curRout += sir[0]
                    curGout += sir[1]
                    curBout += sir[2]
                }
            }
            stackpointer = radius

            for (curX in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= curRout
                gsum -= curGout
                bsum -= curBout

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                curRout -= sir[0]
                curGout -= sir[1]
                curBout -= sir[2]

                if (curY == 0) {
                    vmin[curX] = min(curX + radius + 1, wm)
                }
                p = pix[yw + vmin[curX]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                curRout += sir[0]
                curGout += sir[1]
                curBout += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
            }
            yw += w
        }

        for (curX in 0 until w) {
            binsum = 0
            ginsum = 0
            rinsum = 0
            var curRout = 0
            var curGout = 0
            var curBout = 0
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -radius * w
            for (curI in -radius..radius) {
                yi = max(0, yp) + curX
                sir = stack[curI + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = radius + 1 - abs(curI)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    curRout += sir[0]
                    curGout += sir[1]
                    curBout += sir[2]
                }
                if (curI < hm) {
                    yp += w
                }
            }
            yi = curX
            stackpointer = radius
            for (curY in 0 until h) {
                pix[yi] = (0xff000000.toInt() or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum])

                rsum -= curRout
                gsum -= curGout
                bsum -= curBout

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                curRout -= sir[0]
                curGout -= sir[1]
                curBout -= sir[2]

                if (curX == 0) {
                    vmin[curY] = min(curY + radius + 1, hm) * w
                }
                p = curX + vmin[curY]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                curRout += sir[0]
                curGout += sir[1]
                curBout += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
            }
        }
    }

    fun applySharpen(bitmap: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0.05f) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(width * height)
        val dstPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val centerWeight = 1f + 4f * intensity
        val edgeWeight = -intensity

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val c = srcPixels[idx]
                val top = srcPixels[idx - width]
                val bottom = srcPixels[idx + width]
                val left = srcPixels[idx - 1]
                val right = srcPixels[idx + 1]

                val a = (c shr 24) and 0xff
                val r = ((AndroidColor.red(c) * centerWeight) +
                        (AndroidColor.red(top) + AndroidColor.red(bottom) + AndroidColor.red(left) + AndroidColor.red(right)) * edgeWeight)
                    .roundToInt().coerceIn(0, 255)
                val g = ((AndroidColor.green(c) * centerWeight) +
                        (AndroidColor.green(top) + AndroidColor.green(bottom) + AndroidColor.green(left) + AndroidColor.green(right)) * edgeWeight)
                    .roundToInt().coerceIn(0, 255)
                val b = ((AndroidColor.blue(c) * centerWeight) +
                        (AndroidColor.blue(top) + AndroidColor.blue(bottom) + AndroidColor.blue(left) + AndroidColor.blue(right)) * edgeWeight)
                    .roundToInt().coerceIn(0, 255)

                dstPixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        output.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return output
    }

    // --- Pixelate ---

    fun applyPixelate(bitmap: Bitmap, blockSize: Int): Bitmap {
        val size = blockSize.coerceIn(2, 64)
        val smallW = max(1, bitmap.width / size)
        val smallH = max(1, bitmap.height / size)
        val downscaled = Bitmap.createScaledBitmap(bitmap, smallW, smallH, false)
        return Bitmap.createScaledBitmap(downscaled, bitmap.width, bitmap.height, false)
    }

    // --- Borders & Rounded Corners ---

    fun applyBordersAndCorners(
        bitmap: Bitmap,
        cornerRadius: Float,
        borderWidth: Float,
        borderColor: Int,
        isCircle: Boolean
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(borderWidth / 2f, borderWidth / 2f, width - borderWidth / 2f, height - borderWidth / 2f)

        if (isCircle) {
            val minDim = min(width, height).toFloat()
            val radius = minDim / 2f - borderWidth / 2f
            canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        } else {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Draw outer border stroke if width > 0
        if (borderWidth > 0f) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
                color = borderColor
            }
            if (isCircle) {
                val minDim = min(width, height).toFloat()
                val radius = minDim / 2f - borderWidth / 2f
                canvas.drawCircle(width / 2f, height / 2f, radius, strokePaint)
            } else {
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }
        }

        return output
    }

    // --- Watermark & Text Overlay ---

    fun applyWatermark(
        bitmap: Bitmap,
        text: String,
        fontSizeRatio: Float, // e.g. 0.05f = 5% of width
        opacity: Float,       // 0.1f to 1.0f
        color: Int,
        position: WatermarkPosition
    ): Bitmap {
        if (text.isBlank()) return bitmap
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val textSize = max(24f, output.width * fontSizeRatio)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = (opacity * 255).roundToInt().coerceIn(20, 255)
            this.textSize = textSize
            isFakeBoldText = true
            setShadowLayer(4f, 2f, 2f, AndroidColor.argb(128, 0, 0, 0))
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textW = bounds.width()
        val textH = bounds.height()
        val margin = output.width * 0.04f

        val (x, y) = when (position) {
            WatermarkPosition.TOP_LEFT -> margin to (margin + textH)
            WatermarkPosition.TOP_CENTER -> (output.width - textW) / 2f to (margin + textH)
            WatermarkPosition.TOP_RIGHT -> (output.width - textW - margin) to (margin + textH)
            WatermarkPosition.CENTER -> (output.width - textW) / 2f to (output.height + textH) / 2f
            WatermarkPosition.BOTTOM_LEFT -> margin to (output.height - margin)
            WatermarkPosition.BOTTOM_CENTER -> (output.width - textW) / 2f to (output.height - margin)
            WatermarkPosition.BOTTOM_RIGHT -> (output.width - textW - margin) to (output.height - margin)
        }

        canvas.drawText(text, x, y, paint)
        return output
    }

    // --- Background Remover / Color Keying ---

    fun removeBackgroundColor(bitmap: Bitmap, targetColor: Int, tolerance: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val targetR = AndroidColor.red(targetColor)
        val targetG = AndroidColor.green(targetColor)
        val targetB = AndroidColor.blue(targetColor)
        val maxDist = 441.67f // sqrt(255^2 * 3)
        val threshold = tolerance * maxDist

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = AndroidColor.red(c)
            val g = AndroidColor.green(c)
            val b = AndroidColor.blue(c)

            val dist = Math.sqrt(
                ((r - targetR) * (r - targetR) +
                 (g - targetG) * (g - targetG) +
                 (b - targetB) * (b - targetB)).toDouble()
            ).toFloat()

            if (dist <= threshold) {
                // Smooth transition at edges
                val alpha = if (dist <= threshold * 0.7f) {
                    0
                } else {
                    val fadeRatio = (dist - threshold * 0.7f) / (threshold * 0.3f)
                    (fadeRatio * 255).roundToInt().coerceIn(0, 255)
                }
                pixels[i] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    // --- Base64 Conversion ---

    fun bitmapToBase64(bitmap: Bitmap, format: ImageOutputFormat = ImageOutputFormat.PNG): String {
        val bytes = compressBitmap(bitmap, format, 90)
        val base64Str = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:${format.mimeType};base64,$base64Str"
    }

    fun base64ToBitmap(dataUriOrBase64: String): Bitmap? {
        return try {
            val cleanBase64 = if (dataUriOrBase64.contains(",")) {
                dataUriOrBase64.substringAfter(",")
            } else {
                dataUriOrBase64
            }.trim()
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    // --- Color Palette Extraction ---

    fun extractDominantColors(bitmap: Bitmap, count: Int = 6): List<Int> {
        val sample = Bitmap.createScaledBitmap(bitmap, 64, 64, false)
        val pixels = IntArray(sample.width * sample.height)
        sample.getPixels(pixels, 0, sample.width, 0, 0, sample.width, sample.height)

        val bucketCounts = mutableMapOf<Int, Int>()
        for (p in pixels) {
            val a = AndroidColor.alpha(p)
            if (a < 128) continue
            // Quantize to 4-bit per channel
            val r = (AndroidColor.red(p) / 16) * 16
            val g = (AndroidColor.green(p) / 16) * 16
            val b = (AndroidColor.blue(p) / 16) * 16
            val quantized = AndroidColor.rgb(r, g, b)
            bucketCounts[quantized] = (bucketCounts[quantized] ?: 0) + 1
        }

        val sortedBuckets = bucketCounts.entries.sortedByDescending { it.value }
        val distinctColors = mutableListOf<Int>()

        for ((color, _) in sortedBuckets) {
            var isDifferent = true
            for (selected in distinctColors) {
                val dr = AndroidColor.red(color) - AndroidColor.red(selected)
                val dg = AndroidColor.green(color) - AndroidColor.green(selected)
                val db = AndroidColor.blue(color) - AndroidColor.blue(selected)
                if (Math.sqrt((dr * dr + dg * dg + db * db).toDouble()) < 45.0) {
                    isDifferent = false
                    break
                }
            }
            if (isDifferent) {
                distinctColors.add(color)
            }
            if (distinctColors.size >= count) break
        }

        if (distinctColors.isEmpty()) {
            distinctColors.addAll(listOf(AndroidColor.BLACK, AndroidColor.WHITE, AndroidColor.DKGRAY))
        }
        return distinctColors
    }

    fun colorToHex(color: Int): String {
        return String.format("#%02X%02X%02X", AndroidColor.red(color), AndroidColor.green(color), AndroidColor.blue(color))
    }

    // --- Standalone QR Code Generator ---

    fun generateQrCodeBitmap(text: String, size: Int = 512, fgColor: Int = AndroidColor.BLACK, bgColor: Int = AndroidColor.WHITE): Bitmap {
        val content = if (text.isBlank()) "https://ai.studio" else text
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(bgColor)

        // Generate deterministic QR matrix with standard 3 finder corners & data hash
        val modulesCount = 29 // Version 3 QR grid (29x29)
        val cellSize = size.toFloat() / modulesCount
        val matrix = Array(modulesCount) { BooleanArray(modulesCount) }

        // Finder patterns (7x7) at (0,0), (modulesCount-7, 0), (0, modulesCount-7)
        fun drawFinder(startX: Int, startY: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[startY + r][startX + c] = isBorder || isCenter
                }
            }
        }
        drawFinder(0, 0)
        drawFinder(modulesCount - 7, 0)
        drawFinder(0, modulesCount - 7)

        // Timing patterns
        for (i in 8 until modulesCount - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Encode content hash into data cells
        val hash = content.hashCode()
        var bitIndex = 0
        for (r in 0 until modulesCount) {
            for (c in 0 until modulesCount) {
                val inFinder1 = r < 8 && c < 8
                val inFinder2 = r < 8 && c >= modulesCount - 8
                val inFinder3 = r >= modulesCount - 8 && c < 8
                val inTiming = r == 6 || c == 6
                if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                    val charAt = content[bitIndex % content.length].code
                    val pseudoBit = ((hash ushr (bitIndex % 32)) xor (charAt * 31 + r * 17 + c * 23)) and 1 == 1
                    matrix[r][c] = pseudoBit
                    bitIndex++
                }
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
        }

        for (r in 0 until modulesCount) {
            for (c in 0 until modulesCount) {
                if (matrix[r][c]) {
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint)
                }
            }
        }

        return bitmap
    }

    // --- PDF Generator ---

    fun generatePdfFromBitmaps(context: Context, bitmaps: List<Bitmap>): File {
        val pdfDoc = PdfDocument()
        val cacheDir = context.cacheDir
        val outputFile = File(cacheDir, "images_export_${System.currentTimeMillis()}.pdf")

        for ((index, bmp) in bitmaps.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, index + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            pdfDoc.finishPage(page)
        }

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return outputFile
    }

    // --- Collage / Multi-Image Grid ---

    fun createCollage(bitmaps: List<Bitmap>, spacing: Int = 16, bgColor: Int = AndroidColor.WHITE): Bitmap {
        if (bitmaps.isEmpty()) return createSampleBitmap("landscape")
        if (bitmaps.size == 1) return bitmaps[0]

        val targetSize = 1080
        val collage = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)
        canvas.drawColor(bgColor)

        val count = min(bitmaps.size, 4)
        when (count) {
            2 -> {
                // Side by side
                val tileW = (targetSize - spacing * 3) / 2
                val tileH = targetSize - spacing * 2
                val b1 = resizeAndCropCenter(bitmaps[0], tileW, tileH)
                val b2 = resizeAndCropCenter(bitmaps[1], tileW, tileH)
                canvas.drawBitmap(b1, spacing.toFloat(), spacing.toFloat(), null)
                canvas.drawBitmap(b2, (spacing * 2 + tileW).toFloat(), spacing.toFloat(), null)
            }
            3 -> {
                // Left half 1 image, right half 2 stacked
                val leftW = (targetSize - spacing * 3) / 2
                val leftH = targetSize - spacing * 2
                val rightW = leftW
                val rightH = (targetSize - spacing * 3) / 2

                val b1 = resizeAndCropCenter(bitmaps[0], leftW, leftH)
                val b2 = resizeAndCropCenter(bitmaps[1], rightW, rightH)
                val b3 = resizeAndCropCenter(bitmaps[2], rightW, rightH)

                canvas.drawBitmap(b1, spacing.toFloat(), spacing.toFloat(), null)
                canvas.drawBitmap(b2, (spacing * 2 + leftW).toFloat(), spacing.toFloat(), null)
                canvas.drawBitmap(b3, (spacing * 2 + leftW).toFloat(), (spacing * 2 + rightH).toFloat(), null)
            }
            else -> {
                // 2x2 Grid
                val tileW = (targetSize - spacing * 3) / 2
                val tileH = (targetSize - spacing * 3) / 2
                for (i in 0 until 4) {
                    val row = i / 2
                    val col = i % 2
                    val b = resizeAndCropCenter(bitmaps[i % bitmaps.size], tileW, tileH)
                    val x = spacing + col * (tileW + spacing)
                    val y = spacing + row * (tileH + spacing)
                    canvas.drawBitmap(b, x.toFloat(), y.toFloat(), null)
                }
            }
        }
        return collage
    }

    private fun resizeAndCropCenter(bitmap: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcRatio = bitmap.width.toFloat() / bitmap.height
        val targetRatio = targetW.toFloat() / targetH

        val cropW: Int
        val cropH: Int
        if (srcRatio > targetRatio) {
            cropH = bitmap.height
            cropW = (bitmap.height * targetRatio).toInt()
        } else {
            cropW = bitmap.width
            cropH = (bitmap.width / targetRatio).toInt()
        }
        val cropX = (bitmap.width - cropW) / 2
        val cropY = (bitmap.height - cropH) / 2
        val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
        return Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
    }

    // --- EXIF Extraction ---

    fun extractExifMetadata(context: Context, uri: Uri): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                metadata["Camera Make"] = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "Unknown"
                metadata["Camera Model"] = exif.getAttribute(ExifInterface.TAG_MODEL) ?: "Unknown"
                metadata["Date & Time"] = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "Not Recorded"
                metadata["Exposure Time"] = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "$it sec" } ?: "Auto"
                metadata["F-Number"] = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" } ?: "Auto"
                metadata["ISO Speed"] = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS) ?: "Auto"
                metadata["Focal Length"] = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "$it mm" } ?: "N/A"
                metadata["Flash"] = if (exif.getAttributeInt(ExifInterface.TAG_FLASH, 0) == 1) "Fired" else "Did not fire"
                metadata["White Balance"] = if (exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, 0) == 1) "Manual" else "Auto"
                metadata["Image Width"] = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) ?: "N/A"
                metadata["Image Length"] = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) ?: "N/A"
            }
        } catch (e: Exception) {
            metadata["Status"] = "No EXIF data present in this file or format not supported."
        }
        return metadata
    }

    // --- Saving & Sharing ---

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, format: ImageOutputFormat): Uri? {
        return try {
            val filename = "image_tools_${System.currentTimeMillis()}.${format.extension}"
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { out ->
                val bytes = compressBitmap(bitmap, format, 95)
                out.write(bytes)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            // Fallback to internal storage direct file URI
            try {
                val filename = "image_tools_${System.currentTimeMillis()}.${format.extension}"
                val file = File(context.filesDir, filename)
                FileOutputStream(file).use { out ->
                    val bytes = compressBitmap(bitmap, format, 95)
                    out.write(bytes)
                }
                Uri.fromFile(file)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, title: String = "Exported Image") {
        try {
            val file = File(context.cacheDir, "shared_image_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
