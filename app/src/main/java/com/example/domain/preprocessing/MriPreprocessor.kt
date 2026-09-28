package com.example.domain.preprocessing

import android.graphics.Bitmap

object MriPreprocessor {
    val IMAGENET_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    val IMAGENET_STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    const val TARGET_WIDTH = 128
    const val TARGET_HEIGHT = 128

    /**
     * Preprocesses a 2D image into a [1, 3, 128, 128] flattened tensor:
     * Channels first: C x H x W (3 x 128 x 128 = 49152 floats)
     */
    fun preprocessBitmap(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, TARGET_WIDTH, TARGET_HEIGHT, true)
        val pixels = IntArray(TARGET_WIDTH * TARGET_HEIGHT)
        scaled.getPixels(pixels, 0, TARGET_WIDTH, 0, 0, TARGET_WIDTH, TARGET_HEIGHT)

        val output = FloatArray(3 * TARGET_HEIGHT * TARGET_WIDTH)
        val channelSize = TARGET_HEIGHT * TARGET_WIDTH

        for (y in 0 until TARGET_HEIGHT) {
            for (x in 0 until TARGET_WIDTH) {
                val pixelIndex = y * TARGET_WIDTH + x
                val pixel = pixels[pixelIndex]

                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                // Normalize: (val - mean) / std
                output[0 * channelSize + pixelIndex] = (r - IMAGENET_MEAN[0]) / IMAGENET_STD[0]
                output[1 * channelSize + pixelIndex] = (g - IMAGENET_MEAN[1]) / IMAGENET_STD[1]
                output[2 * channelSize + pixelIndex] = (b - IMAGENET_MEAN[2]) / IMAGENET_STD[2]
            }
        }
        return output
    }

    /**
     * Overload for unit tests: preprocesses given RGB arrays directly without Android Bitmap.
     */
    fun preprocessRgb(red: FloatArray, green: FloatArray, blue: FloatArray, width: Int = 128, height: Int = 128): FloatArray {
        val channelSize = width * height
        val output = FloatArray(3 * channelSize)
        for (i in 0 until channelSize) {
            output[0 * channelSize + i] = (red[i] - IMAGENET_MEAN[0]) / IMAGENET_STD[0]
            output[1 * channelSize + i] = (green[i] - IMAGENET_MEAN[1]) / IMAGENET_STD[1]
            output[2 * channelSize + i] = (blue[i] - IMAGENET_MEAN[2]) / IMAGENET_STD[2]
        }
        return output
    }
}
