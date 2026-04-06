package com.danissimo.glyphgeekbox

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.danissimo.glyphgeekbox.demos.UltimateKeyService

class EssentialKeyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastClickTime: Long = 0
    private var pendingSingleClickTask: Runnable? = null
    private var longPressTask: Runnable? = null
    private var isLongPressPerformed = false

    companion object {
        private const val TAG = "EssentialKeyService"
        private const val DOUBLE_CLICK_TIMEOUT = 300L
        private const val LONG_PRESS_TIMEOUT = 500L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Не требуется для обработки кнопок
    }

    override fun onInterrupt() {
        // Не требуется
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.scanCode == KeyEvent.KEYCODE_TV_INPUT_COMPONENT_2) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (event.repeatCount == 0) {
                        isLongPressPerformed = false
                        // Отменяем возможный одиночный клик от предыдущего нажатия
                        pendingSingleClickTask?.let { handler.removeCallbacks(it) }

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime < DOUBLE_CLICK_TIMEOUT) {
                            // Двойное нажатие обнаружено
                            lastClickTime = 0
                            longPressTask?.let { handler.removeCallbacks(it) }
                            onDoubleClick()
                        } else {
                            lastClickTime = currentTime
                            // Запускаем таймер для удержания
                            longPressTask = Runnable {
                                isLongPressPerformed = true
                                onLongPress()
                            }
                            handler.postDelayed(longPressTask!!, LONG_PRESS_TIMEOUT)
                        }
                    }
                }
                KeyEvent.ACTION_UP -> {
                    // Кнопка отпущена, отменяем таймер удержания
                    longPressTask?.let { handler.removeCallbacks(it) }
                    
                    if (!isLongPressPerformed && lastClickTime != 0L) {
                        // Если это не было удержанием, планируем одиночный клик
                        pendingSingleClickTask = Runnable {
                            onSingleClick()
                        }
                        handler.postDelayed(pendingSingleClickTask!!, DOUBLE_CLICK_TIMEOUT)
                    }
                }
            }
            return true // Поглощаем событие
        }
        return super.onKeyEvent(event)
    }

    private fun onSingleClick() {
        Log.d(TAG, "Detected: Single Click")
    }

    private fun onDoubleClick() {
        Log.d(TAG, "Detected: Double Click")
    }

    private fun onLongPress() {
        Log.d(TAG, "Detected: Long Press -> Switching UltimateKeyService mode")
        // Отправляем Broadcast для переключения режима в UltimateKeyService
        val intent = Intent(UltimateKeyService.ACTION_SWITCH_MODE).apply {
            // Для Android 14+ важно указать пакет или сделать приемник экспортируемым
            `package` = packageName
        }
        sendBroadcast(intent)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service Connected")
    }
}