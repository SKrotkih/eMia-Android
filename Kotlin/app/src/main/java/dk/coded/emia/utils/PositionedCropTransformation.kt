package dk.coded.emia.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.support.annotation.FloatRange

import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils

import java.security.MessageDigest

import com.bumptech.glide.load.resource.bitmap.TransformationUtils.PAINT_FLAGS

class PositionedCropTransformation(@FloatRange(from = 0.0, to = 1.0) xPercentage: Float, @FloatRange(from = 0.0, to = 1.0) yPercentage: Float) : BitmapTransformation() {

    private val xPercentage = 0.5f
    private val yPercentage = 0.5f


    init {
        this.xPercentage = xPercentage
        this.yPercentage = yPercentage
    }


    // Bitmap doesn't implement equals, so == and .equals are equivalent here.
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return crop(pool, toTransform, outWidth, outHeight, xPercentage, yPercentage)
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun equals(o: Any?): Boolean {
        return o is PositionedCropTransformation
    }

    companion object {
        private val ID = "PositionedCropTransformation.com.bumptech.glide.load.resource.bitmap.x:0.y:0"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)

        /**
         * A potentially expensive operation to crop the given Bitmap so that it fills the given dimensions. This operation
         * is significantly less expensive in terms of memory if a mutable Bitmap with the given dimensions is passed in
         * as well.
         *
         * @param pool     The BitmapPool to obtain a bitmap from.
         * @param inBitmap   The Bitmap to resize.
         * @param width    The width in pixels of the final Bitmap.
         * @param height   The height in pixels of the final Bitmap.
         * @param xPercentage The horizontal percentage of the crop. 0.0f => left, 0.5f => center, 1.0f => right or anything in between 0 and 1
         * @param yPercentage The vertical percentage of the crop. 0.0f => top, 0.5f => center, 1.0f => bottom or anything in between 0 and 1
         * @return The resized Bitmap (will be recycled if recycled is not null).
         */
        private fun crop(pool: BitmapPool, inBitmap: Bitmap, width: Int, height: Int, xPercentage: Float, yPercentage: Float): Bitmap {
            if (inBitmap.width == width && inBitmap.height == height) {
                return inBitmap
            }

            // From ImageView/Bitmap.createScaledBitmap.
            val scale: Float
            var dx = 0f
            var dy = 0f
            val m = Matrix()
            if (inBitmap.width * height > width * inBitmap.height) {
                scale = height.toFloat() / inBitmap.height.toFloat()
                dx = width - inBitmap.width * scale
                dx *= xPercentage
            } else {
                scale = width.toFloat() / inBitmap.width.toFloat()
                dy = height - inBitmap.height * scale
                dy *= yPercentage
            }

            m.setScale(scale, scale)
            m.postTranslate((dx + 0.5f).toInt().toFloat(), (dy + 0.5f).toInt().toFloat())

            val result = pool.get(width, height, getSafeConfig(inBitmap))
            // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
            TransformationUtils.setAlpha(inBitmap, result)

            applyMatrix(inBitmap, result, m)
            return result
        }

        private fun applyMatrix(inBitmap: Bitmap, targetBitmap: Bitmap,
                                matrix: Matrix) {
            TransformationUtils.getBitmapDrawableLock().lock()
            try {
                val canvas = Canvas(targetBitmap)
                canvas.drawBitmap(inBitmap, matrix, Paint(PAINT_FLAGS))
                canvas.setBitmap(null)
            } finally {
                TransformationUtils.getBitmapDrawableLock().unlock()
            }
        }

        private fun getSafeConfig(bitmap: Bitmap): Bitmap.Config {
            return if (bitmap.config != null) bitmap.config else Bitmap.Config.ARGB_8888
        }
    }
}