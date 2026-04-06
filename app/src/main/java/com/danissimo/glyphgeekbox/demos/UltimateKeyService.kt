package com.danissimo.glyphgeekbox.demos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.animation.*

/**
 * Обертка-хост, которая переключает логику между существующими сервисами анимаций.
 * При каждом переключении создается новый экземпляр класса, чтобы сбросить внутренние стейты.
 */
class UltimateKeyService : GlyphMatrixService("Ultimate-Key-Service") {

    private var matrixManager: GlyphMatrixManager? = null
    private var currentMode = 0
    
    // Текущий активный "контроллер" логики
    private var currentLogicController: GlyphMatrixService? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_SWITCH_MODE) {
                switchMode()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(ACTION_SWITCH_MODE)
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        stopCurrentLogic()
    }

    override fun performOnServiceConnected(context: Context, glyphMatrixManager: GlyphMatrixManager) {
        matrixManager = glyphMatrixManager
        startLogicForMode(currentMode)
    }

    override fun performOnServiceDisconnected(context: Context) {
        stopCurrentLogic()
        matrixManager = null
    }

    private fun switchMode() {
        val gmm = matrixManager ?: return
        
        // 1. Останавливаем и уничтожаем старый экземпляр
        stopCurrentLogic()
        
        // 2. Инкрементируем режим
        currentMode = (currentMode + 1) % 5
        
        // 3. Очищаем матрицу
        gmm.turnOff()
        
        // 4. Создаем и запускаем НОВЫЙ экземпляр контроллера
        startLogicForMode(currentMode)
    }

    private fun startLogicForMode(mode: Int) {
        val gmm = matrixManager ?: return
        
        // Создаем новый экземпляр каждый раз, чтобы CoroutineScope и переменные были чистыми
        currentLogicController = when (mode) {
            0 -> AnimationDemoService()
            1 -> BadAppleService()
            2 -> GameOfLifeService()
            3 -> LiquidSimulationService()
            4 -> PerlNoiseService()
            else -> AnimationDemoService()
        }

        Log.d("UltimateKeyService", "Started NEW instance of: ${currentLogicController?.javaClass?.simpleName}")
        currentLogicController?.performOnServiceConnected(this, gmm)
    }

    private fun stopCurrentLogic() {
        currentLogicController?.let {
            Log.d("UltimateKeyService", "Stopping instance of: ${it.javaClass.simpleName}")
            it.performOnServiceDisconnected(this)
        }
        currentLogicController = null
    }

    companion object {
        const val ACTION_SWITCH_MODE = "com.nothinglondon.sdkdemo.SWITCH_MODE"
    }
}