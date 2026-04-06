package com.danissimo.glyphgeekbox.demos.animation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Liquid simulation for the back-panel Glyph matrix.
 * Modeled as a 2D container with 1px depth.
 * Final orientation fix for Nothing Phone back panel.
 */
class LiquidSimulationService : GlyphMatrixService("Liquid-Simulation"), SensorEventListener {

    private val backgroundScope = CoroutineScope(Dispatchers.Default)
    private var sensorManager: SensorManager? = null

    // Gravity vector in matrix space
    private var targetX = 0f
    private var targetY = 9.8f
    private var targetZ = 0f

    // Current filtered state for smooth movement
    private var currentX = 0f
    private var currentY = 9.8f
    private var currentZ = 0f
    private var velX = 0f
    private var velY = 0f
    private var velZ = 0f

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)

        backgroundScope.launch {
            while (isActive) {
                updatePhysics()
                val frame = renderLiquidFrame()

                withContext(Dispatchers.Main) {
                    glyphMatrixManager.setMatrixFrame(frame)
                }

                delay(30L) // 50 FPS
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        sensorManager?.unregisterListener(this)
        backgroundScope.cancel()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Final correction based on user feedback:
            // 1. X is inverted so tilt-left (back view) moves liquid left.
            targetX = event.values[0]
            // 2. Y is NOT inverted from raw sensor (-9.8 when upright) 
            //    to keep liquid at the physical bottom.
            targetY = event.values[1]
            // 3. Z is inverted so face-up position keeps liquid at the glass.
            targetZ = event.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updatePhysics() {
        val ax = (targetX - currentX) * SPRING_K
        val ay = (targetY - currentY) * SPRING_K
        val az = (targetZ - currentZ) * SPRING_K

        velX = (velX + ax) * DAMPING
        velY = (velY + ay) * DAMPING
        velZ = (velZ + az) * DAMPING

        currentX += velX
        currentY += velY
        currentZ += velZ
    }

    private fun renderLiquidFrame(): IntArray {
        val frame = IntArray(WIDTH * HEIGHT)
        
        val mag = sqrt(currentX * currentX + currentY * currentY + currentZ * currentZ)
        if (mag < 0.1f) return frame

        val nx = currentX / mag
        val ny = currentY / mag
        val nz = currentZ / mag

        // Container is x in [0, W], y in [0, H], z in [0, 1]
        // Center point is (W/2, H/2, 0.5)
        val d = (WIDTH / 2f) * nx + (HEIGHT / 2f) * ny + (0.5f) * nz

        for (y in 0 until HEIGHT) {
            val yOffset = y * WIDTH
            for (x in 0 until WIDTH) {
                val rhs = d - (x * nx + y * ny)
                
                val fill: Float = if (abs(nz) > 0.01f) {
                    if (x * nx + y * ny > d) 1.0f else 0.0f
                } else {
                    val zSurface = abs(rhs / nz)
                    zSurface.coerceIn(0.0f, 1.0f)
//                    if (nz <= 0) {
//                        // Gravity pulls towards Z=1 (body). Liquid fills [zSurface, 1]
//                        (1.0f - zSurface).coerceIn(0.0f, 1.0f)
//                    } else {
//                        // Gravity pulls towards Z=0 (glass). Liquid fills [0, zSurface]
//                        zSurface.coerceIn(0.0f, 1.0f)
//                    }
                }

                frame[yOffset + x] = (fill * 255).toInt().coerceIn(0, 255)
            }
        }
        
        return frame
    }

    override fun onTouchPointPressed() {}

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
        
        private const val SPRING_K = 0.5f
        private const val DAMPING = 0.6f
    }

}
