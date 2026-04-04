package com.nothinglondon.sdkdemo.demos.animation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphToy
import com.nothinglondon.sdkdemo.demos.GlyphMatrixService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AnimationDemoService : GlyphMatrixService("Animation-Demo") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var frame = 0

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        backgroundScope.launch {
            while (isActive) {
                val array = generateNextAnimationFrame()
                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(array)
                }
                // wait a bit
                delay(30)
                // next frame
                frame++
                if (frame >= WIDTH) {
                    frame = 0
                }
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun generateNextAnimationFrame(): IntArray {
        val shiftAmount = ANGLE_PER_PIXEL_DEGREES * frame
        val grid = Array(HEIGHT * WIDTH) { 0 }
        for (i2 in 0..<UPSAMPLE*HEIGHT) {
            val i = i2 /UPSAMPLE.toDouble();
            val angle = Math.toRadians(i * ANGLE_PER_PIXEL_DEGREES + shiftAmount)
            val value = sin(angle)
            val row = (value * HALF_HEIGHT).toInt() + MID_POINT
            grid[row * WIDTH + i.toInt()] = 255
        }
        return grid.toIntArray()
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
        private val HALF_HEIGHT = HEIGHT.toDouble() / 2
        private val MID_POINT = HEIGHT / 2
        private val ANGLE_PER_PIXEL_DEGREES = 360.0 / WIDTH

        private val UPSAMPLE = if (Common.is25111p()) 3 else 1

    }

}