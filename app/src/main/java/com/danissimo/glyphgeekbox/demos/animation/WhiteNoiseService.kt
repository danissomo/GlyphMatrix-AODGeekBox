package com.danissimo.glyphgeekbox.demos.animation

import android.content.Context
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class WhiteNoiseService : GlyphMatrixService("Animation-Demo") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

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
                delay(100)
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun generateNextAnimationFrame(): IntArray {
        val grid = Array(HEIGHT * WIDTH) { 0 }
        for (i in grid.indices){
            grid[i] =  Random.nextInt(0, 1024)
        }
        return grid.toIntArray()
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
    }

}