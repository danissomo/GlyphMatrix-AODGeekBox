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
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

class MandelbrotService : GlyphMatrixService("Mandelbrot-Smart-Zoom") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    
    private var centerX = -0.75
    private var centerY = 0.1
    private var scale = 2.0
    
    // Параметры адаптивного зума
    private var currentZoomSpeed = 0.94
    private val minZoomSpeed = 0.88 // Быстрый пролет пустоты
    private val maxZoomSpeed = 0.98 // Медленное разглядывание деталей
    
    private var baseIterations = 64

    private val interestingPoints = listOf(
        Pair(-0.7436438870371587, 0.13182590420642),
        Pair(-0.10109636384562, 0.95628651080914),
        Pair(-1.25066, 0.02012),
        Pair(0.28227, 0.01357),
        Pair(-0.748, 0.1),
        Pair(-0.8, 0.156),
        Pair(-0.001643721971153, 0.822467633298876),
        Pair(-0.743644786, 0.131825253),
        Pair(-1.749759145246, 0.0),
        Pair(-0.16070135, 1.0375665)
    )

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        reset()
        backgroundScope.launch {
            while (isActive) {
                val array = generateNextAnimationFrame()
                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(array)
                }
                delay(40)
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun reset() {
        val p = interestingPoints.random()
        centerX = p.first
        centerY = p.second
        scale = 2.0
        currentZoomSpeed = 0.94
    }

    private fun generateNextAnimationFrame(): IntArray {
        val width = WIDTH
        val height = HEIGHT
        val result = IntArray(width * height)

        // Адаптивное увеличение итераций при приближении
        val zoomDepth = -log10(scale)
        val maxIterations = (baseIterations + zoomDepth * 20).toInt().coerceAtMost(256)

        var borderPixels = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val cRe = centerX + (x - width / 2.0) * scale / width
                val cIm = centerY + (y - height / 2.0) * scale / height

                var zRe = 0.0
                var zIm = 0.0
                var iteration = 0
                
                // Используем радиус выхода 16.0 для Smooth Coloring
                while (zRe * zRe + zIm * zIm <= 16.0 && iteration < maxIterations) {
                    val nextRe = zRe * zRe - zIm * zIm + cRe
                    val nextIm = 2.0 * zRe * zIm + cIm
                    zRe = nextRe
                    zIm = nextIm
                    iteration++
                }

                // Считаем "сложность" кадра (пиксели на границе)
                if (iteration > 0 && iteration < maxIterations) {
                    borderPixels++
                }

                val brightness = if (iteration == maxIterations) {
                    0
                } else {
                    // Алгоритм плавного окрашивания (Smooth Coloring)
                    val logZn = ln(zRe * zRe + zIm * zIm) / 2.0
                    val nu = ln(logZn / ln(2.0)) / ln(2.0)
                    val smoothIteration = iteration + 1 - nu
                    
                    val norm = (smoothIteration / maxIterations).coerceIn(0.0, 1.0)
                    // Гамма-коррекция для лучшей видимости на LED
                    (norm.pow(1.2) * 4095).toInt()
                }
                result[y * width + x] = brightness
            }
        }

        // --- УМНОЕ СКАЛИРОВАНИЕ ---
        
        // Анализируем детальность кадра
        val complexity = borderPixels.toDouble() / (width * height)
        
        // "Круиз-контроль":
        // Если деталей мало (пустота), ускоряем зум до minZoomSpeed.
        // Если мы в самой гуще (complexity > 0.4), замедляемся до maxZoomSpeed.
        val targetSpeed = when {
            complexity < 0.1 -> minZoomSpeed
            complexity > 0.4 -> maxZoomSpeed
            else -> 0.94
        }

        // Плавное изменение скорости (инерция зума), чтобы избежать рывков
        currentZoomSpeed = currentZoomSpeed * 0.9 + targetSpeed * 0.1
        scale *= currentZoomSpeed

        // Проверка предела точности Double
        if (scale < 1e-14) {
            reset()
        }

        return result
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
    }
}
