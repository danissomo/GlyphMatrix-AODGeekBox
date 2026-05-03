package com.danissimo.glyphgeekbox.aod

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.os.BatteryManager
import android.util.Log

class ChargeService : GlyphMatrixService("Animation-Demo") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var internalContext: Context? = null

    private val wattSymbol = arrayOf(
        0 to 0,
        1 to 0,
        2 to 1,
        0 to 2,
        1 to 2,
        2 to 3,
        0 to 4,
        1 to 4
    )

    private val percentSymbol = arrayOf(
        0 to 0,
        2 to 0,
        1 to 1,
        0 to 2,
        2 to 2
    )

    private fun drawSymbol(buffer : IntArray, symbol: Array<Pair<Int, Int>>, x: Int, y : Int){
        for (p in symbol) {
            buffer[(p.first + y) * WIDTH + p.second + x] = 255
        }
    }

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        internalContext = context
        backgroundScope.launch {
            while (isActive) {
                var buffer : IntArray? = null
                val charge = getCharge()
                if (isCharging()) {
                    val chargePower = getPower().toInt().coerceIn(0, 99).toString()
                    var x : Int = if (chargePower.length == 2) 2 else 5
                    var y : Int = 0

                    val matrixObj = GlyphMatrixObject.Builder()
                        .setText(chargePower).setPosition(x, y)
                        .build()

                    val frame = GlyphMatrixFrame.Builder().addTop(matrixObj).build(context)
                    buffer = frame.render()
                    Log.d("Charge", "Charge: ${isCharging()}")
                    drawSymbol(buffer, wattSymbol, 4, 7)
                }else{
                    val charge = charge.coerceIn(0, 99).toString()
                    var x : Int = if (charge.length == 2) 2 else 5
                    var y : Int = 0
                    val matrixObj = GlyphMatrixObject.Builder()
                        .setText(charge).setPosition(x, y)
                        .build()
                    val frame = GlyphMatrixFrame.Builder().addTop(matrixObj).build(context)
                    buffer = frame.render()
                    drawSymbol(buffer, percentSymbol, 5, 7)
                }
                if (buffer == null) continue
                drawChargeBar(buffer)
                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(buffer)
                }
                // wait a bit
                delay(1000)

            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
        internalContext = null
    }

    private fun getPower() : Double {
        val context = internalContext ?: return 0.0
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentMicroAmps = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val voltageMilliVolts = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: 0
        val powerWatts = (currentMicroAmps.toDouble() / 1_000_000) * (voltageMilliVolts.toDouble() / 1_000)
        return powerWatts
    }

    private fun getCharge() : Int {
        val context = internalContext ?: return 0
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val cap = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        return cap
    }

    private fun isCharging() : Boolean {
        val context = internalContext ?: return false
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING
    }

    private var blinkState = true
    private fun drawChargeBar (buffer: IntArray){
        val charge = getCharge()
        var fill = (LOW_BAR_LEN*charge/100f).toInt()
        for (i in 0 until fill+1){
            buffer[LOW_BAR_START.second * WIDTH + LOW_BAR_START.first + i] = 255
        }
        if (isCharging())
            buffer[LOW_BAR_START.second * WIDTH + LOW_BAR_START.first + fill] =  if (blinkState) 1024 else 255
        blinkState = !blinkState
        for (i in fill+1 until LOW_BAR_LEN){
            buffer[LOW_BAR_START.second * WIDTH + LOW_BAR_START.first + i] = 100
        }
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
        private const val LOW_BAR_LEN = 9
        private  val LOW_BAR_START = Pair(2,11)

    }

}
