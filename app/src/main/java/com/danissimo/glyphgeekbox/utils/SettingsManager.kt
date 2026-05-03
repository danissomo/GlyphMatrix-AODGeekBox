package com.danissimo.glyphgeekbox.utils

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "ultimate_key_settings"
    private const val KEY_ANIMATION_ORDER = "animation_order"
    private const val KEY_ENABLED_ANIMATIONS = "enabled_animations"

    val allAnimations = listOf(
        "AnimationDemo",
        "BadApple",
        "GameOfLife",
        "LiquidSimulation",
        "PerlNoise",
        "Pong",
        "WhiteNoise",
        "Mandelbrot",
        "Charge"
    )

    fun getAnimationOrder(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val orderStr = prefs.getString(KEY_ANIMATION_ORDER, null) ?: return allAnimations
        
        val savedOrder = orderStr.split(",").filter { it.isNotEmpty() && allAnimations.contains(it) }
        
        // Находим новые анимации, которых еще нет в сохраненном порядке
        val missing = allAnimations.filter { it !in savedOrder }
        
        return savedOrder + missing
    }

    fun saveAnimationOrder(context: Context, order: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ANIMATION_ORDER, order.joinToString(",")).apply()
    }

    fun getEnabledAnimations(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedEnabled = prefs.getStringSet(KEY_ENABLED_ANIMATIONS, null) ?: return allAnimations.toSet()
        
        val filtered = savedEnabled.filter { it in allAnimations }.toMutableSet()
        
        // По желанию: можно автоматически включать новые анимации, если их еще нет в настройках
        // Но даже без этого они теперь будут отображаться в списке (как выключенные)
        return filtered
    }

    fun saveEnabledAnimations(context: Context, enabled: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ENABLED_ANIMATIONS, enabled).apply()
    }
}
