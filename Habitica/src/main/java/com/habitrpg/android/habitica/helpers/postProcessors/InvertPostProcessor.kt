package com.habitrpg.android.habitica.helpers.postProcessors

import android.graphics.*
import com.facebook.cache.common.CacheKey
import com.facebook.cache.common.SimpleCacheKey
import com.facebook.imagepipeline.request.BasePostprocessor


class InvertPostProcessor: BasePostprocessor() {

    override fun getName(): String {
        return "invertPostProcessor"
    }


    override fun process(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val matrixInvert = ColorMatrix()
        matrixInvert.set(floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        ))

        val filter = ColorMatrixColorFilter(matrixInvert)
        paint.colorFilter = filter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }

    override fun getPostprocessorCacheKey(): CacheKey {
        return SimpleCacheKey("0")
    }
}