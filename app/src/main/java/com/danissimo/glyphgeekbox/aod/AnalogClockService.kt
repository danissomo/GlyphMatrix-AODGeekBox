package com.danissimo.glyphgeekbox.aod

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
import java.util.Calendar
import kotlin.math.*
import com.danissimo.glyphgeekbox.utils.SettingsManager
import com.danissimo.glyphgeekbox.utils.generate_circle_points

class AnalogClockService : GlyphMatrixService("AnalogClock") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        backgroundScope.launch {
            while (isActive) {
                val array = if (SettingsManager.getClockType(context)) generateCircularClock() else generateLineClock()
                uiScope.launch {
                    setMatrixFrame(context, glyphMatrixManager, array)
                }
                delay(1000)
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun generateLineClock(): IntArray {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val frame = IntArray(WIDTH * HEIGHT)
        val centerX = (WIDTH - 1) / 2f
        val centerY = (HEIGHT - 1) / 2f

        // Hour hand: 360 / 12 = 30 degrees per hour
        val hourAngle = (hour % 12 + minute / 60f) * 30f - 90f
        drawHand(frame, centerX, centerY, hourAngle, (WIDTH / 2f) * 0.5f, 1024)

        // Minute hand: 360 / 60 = 6 degrees per minute
        val minuteAngle = (minute + second / 60f) * 6f - 90f
        drawHand(frame, centerX, centerY, minuteAngle, (WIDTH / 2f) * 0.75f, 512)

        // Second hand: 360 / 60 = 6 degrees per second
        val secondAngle = second * 6f - 90f
        drawHand(frame, centerX, centerY, secondAngle, (WIDTH / 2f) * 0.9f, 255)

        // Center point
        val cX = centerX.roundToInt()
        val cY = centerY.roundToInt()
        if (cX in 0 until WIDTH && cY in 0 until HEIGHT) {
            frame[cY * WIDTH + cX] = 1024
        }

        return frame
    }
    private fun <T> Array<T>.rotate(n: Int) =
        sliceArray(n until size) + sliceArray(0 until n)
    private fun generateCircularClock(): IntArray {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val frame = IntArray(WIDTH * HEIGHT)

        var secCircle = generate_circle_points(WIDTH, WIDTH / 2, WIDTH / 2).reversed().toTypedArray()
        var minCircle = generate_circle_points(WIDTH -2, WIDTH / 2, WIDTH / 2).reversed().toTypedArray()
        var hourCircle = generate_circle_points(WIDTH -4, WIDTH / 2, WIDTH / 2).reversed().toTypedArray()

        secCircle = secCircle.rotate(secCircle.size / 2 - 1)
        minCircle = minCircle.rotate(minCircle.size / 2 - 1)
        hourCircle = hourCircle.rotate(hourCircle.size / 2 -1)


        val secI = (second / 60f)*secCircle.size
        val minI = (minute / 60f)*minCircle.size
        val hourI = (hour % 12 / 12f)*hourCircle.size

        val secCord = secCircle[secI.toInt()]
        val minCord = minCircle[minI.toInt()]
        val hourCord = hourCircle[hourI.toInt()]

        for (cord in secCircle){
            frame[cord.first * WIDTH + cord.second] = 100
        }
        for (cord in minCircle){
            frame[cord.first * WIDTH + cord.second] = 255
        }
        for (cord in hourCircle){
            frame[cord.first * WIDTH + cord.second] = 100
        }

        frame[secCord.first * WIDTH + secCord.second] = 4000
        frame[minCord.first * WIDTH + minCord.second] = 4000
        frame[hourCord.first * WIDTH + hourCord.second] = 4000

        return frame
    }

    private fun drawHand(frame: IntArray, centerX: Float, centerY: Float, angleDeg: Float, length: Float, brightness: Int) {
        val angleRad = angleDeg * PI / 180.0
        val x1 = centerX + length * cos(angleRad).toFloat()
        val y1 = centerY + length * sin(angleRad).toFloat()
        drawLine(frame, centerX, centerY, x1, y1, brightness)
    }

    private fun drawLine(frame: IntArray, x0: Float, y0: Float, x1: Float, y1: Float, brightness: Int) {
        var ix0 = x0.roundToInt().coerceIn(0, WIDTH - 1)
        var iy0 = y0.roundToInt().coerceIn(0, HEIGHT - 1)
        val ix1 = x1.roundToInt().coerceIn(0, WIDTH - 1)
        val iy1 = y1.roundToInt().coerceIn(0, HEIGHT - 1)

        val dx = abs(ix1 - ix0)
        val dy = -abs(iy1 - iy0)
        val sx = if (ix0 < ix1) 1 else -1
        val sy = if (iy0 < iy1) 1 else -1
        var err = dx + dy

        while (true) {
            frame[iy0 * WIDTH + ix0] = brightness
            if (ix0 == ix1 && iy0 == iy1) break
            val e2 = 2 * err
            if (e2 >= dy) {
                err += dy
                ix0 += sx
            }
            if (e2 <= dx) {
                err += dx
                iy0 += sy
            }
        }
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
    }
}
