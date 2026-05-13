package com.danissimo.glyphgeekbox.aod

import android.content.Context
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.danissimo.glyphgeekbox.utils.SettingsManager
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScrollingTextService : GlyphMatrixService("Scrolling-Text") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var scrollX = WIDTH // Start from left, off-screen

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {

        // Approximate text length: each character is roughly 6 pixels wide plus spacing


        backgroundScope.launch {
            while (isActive) {
                val text = SettingsManager.getScrollingText(context)
                if (text.isEmpty()) continue
                val textWidth = text.length * 7
                val matrixObj = GlyphMatrixObject.Builder()
                    .setText(text)
                    .setPosition(scrollX, 3) // Roughly centered vertically
                    .build()

                val frame = GlyphMatrixFrame.Builder()
                    .addTop(matrixObj)
                    .build(context)

                val buffer = frame.render()

                uiScope.launch {
                    setMatrixFrame(context, glyphMatrixManager, buffer)
                }

                delay(100)

                scrollX--
                if (scrollX < -textWidth) {
                    scrollX = WIDTH
                }
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
    }
}