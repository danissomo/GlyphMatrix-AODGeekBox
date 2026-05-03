package com.danissimo.glyphgeekbox.demos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.danissimo.glyphgeekbox.aod.ChargeService
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.animation.*
import com.danissimo.glyphgeekbox.games.*
import com.danissimo.glyphgeekbox.utils.SettingsManager

/**
 * Обертка-хост, которая переключает логику между существующими сервисами анимаций.
 * Порядок и список включенных анимаций берутся из настроек.
 */
class UltimateKeyService : GlyphMatrixService("Ultimate-Key-Service") {

    private var matrixManager: GlyphMatrixManager? = null
    
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
        startLogicForMode(currentModeIndex)
    }

    override fun performOnServiceDisconnected(context: Context) {
        stopCurrentLogic()
        matrixManager = null
    }

    private fun switchMode() {
        val gmm = matrixManager ?: return
        
        // 1. Останавливаем и уничтожаем старый экземпляр
        stopCurrentLogic()
        
        // 2. Получаем актуальный список включенных анимаций
        val enabledAnims = getEnabledAnimationsList()
        
        // 3. Инкрементируем индекс режима
        if (enabledAnims.isNotEmpty()) {
            currentModeIndex = (currentModeIndex + 1) % enabledAnims.size
        } else {
            currentModeIndex = 0
        }
        
        // 4. Очищаем матрицу
        gmm.turnOff()
        
        // 5. Запускаем новый контроллер
        startLogicForMode(currentModeIndex)
    }

    private fun getEnabledAnimationsList(): List<String> {
        val order = SettingsManager.getAnimationOrder(this)
        val enabled = SettingsManager.getEnabledAnimations(this)
        return order.filter { enabled.contains(it) }
    }

    private fun startLogicForMode(index: Int) {
        val gmm = matrixManager ?: return
        val enabledAnims = getEnabledAnimationsList()
        
        if (enabledAnims.isEmpty()) {
            Log.w("UltimateKeyService", "No animations enabled in settings!")
            return
        }

        // Защита от выхода за пределы
        val safeIndex = index.coerceIn(0, enabledAnims.size - 1)
        val animName = enabledAnims[safeIndex]

        // Создаем новый экземпляр каждый раз
        currentLogicController = when (animName) {
            "AnimationDemo" -> AnimationDemoService()
            "BadApple" -> BadAppleService()
            "GameOfLife" -> GameOfLifeService()
            "LiquidSimulation" -> LiquidSimulationService()
            "PerlNoise" -> PerlNoiseService()
            "Pong" -> PongService()
            "WhiteNoise" -> WhiteNoiseService()
            "Mandelbrot" -> MandelbrotService()
            "Charge" -> ChargeService()
            else -> AnimationDemoService()
        }
        
        Log.d("UltimateKeyService", "Started mode [$safeIndex]: $animName")
        currentLogicController?.performOnServiceConnected(this, gmm)
    }

    private fun stopCurrentLogic() {
        currentLogicController?.let {
            it.performOnServiceDisconnected(this)
        }
        currentLogicController = null
    }

    companion object {
        const val ACTION_SWITCH_MODE = "com.nothinglondon.sdkdemo.SWITCH_MODE"
        
        // Глобальная переменная для сохранения состояния при перезапуске сервиса
        private var currentModeIndex = 0
    }
}
