package com.danissimo.glyphgeekbox.utils

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "ultimate_key_settings"
    private const val KEY_ANIMATION_ORDER = "animation_order"
    private const val KEY_ENABLED_ANIMATIONS = "enabled_animations"
    private const val KEY_SCROLLING_TEXT = "scrolling_text"

    val allAnimations = listOf(
        "AnimationDemo",
        "BadApple",
        "GameOfLife",
        "LiquidSimulation",
        "PerlNoise",
        "Pong",
        "WhiteNoise",
        "Mandelbrot",
        "Charge",
        "ScrollingText",
        "AnalogClock"
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
        
        return filtered
    }

    fun saveEnabledAnimations(context: Context, enabled: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ENABLED_ANIMATIONS, enabled).apply()
    }

    fun getScrollingText(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SCROLLING_TEXT, "NOTHING") ?: "NOTHING"
    }

    fun getClockType(context: Context) : Boolean  {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("clock_type", false)
    }

    fun setClockType(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("clock_type", value).apply()
    }

    fun saveScrollingText(context: Context, text: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCROLLING_TEXT, text).apply()
    }
}
