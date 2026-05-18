package com.danissimo.glyphgeekbox.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

object SettingsManager {
    private const val PREFS_NAME = "ultimate_key_settings"
    private const val WIDGET_PREFS_NAME = "glyph_widget_settings"
    private const val KEY_ANIMATION_ORDER = "animation_order"
    private const val KEY_ENABLED_ANIMATIONS = "enabled_animations"
    private const val KEY_SCROLLING_TEXT = "scrolling_text"
    private const val KEY_AUTO_CYCLE_ENABLED = "auto_cycle_enabled"
    private const val KEY_AUTO_CYCLE_INTERVAL = "auto_cycle_interval"

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
        "AnalogClock",
        "StatusBar"
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

    fun getAutoCycleEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_CYCLE_ENABLED, false)
    }

    fun setAutoCycleEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_CYCLE_ENABLED, enabled).apply()
    }

    fun getAutoCycleInterval(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_AUTO_CYCLE_INTERVAL, 30) // Default 30 seconds
    }

    fun setAutoCycleInterval(context: Context, interval: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_AUTO_CYCLE_INTERVAL, interval).apply()
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

    // Widget Settings
    private fun getWidgetPrefs(context: Context) = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)

    fun getWidgetSpacing(context: Context, appWidgetId: Int): Int {
        return getWidgetPrefs(context).getInt("spacing_$appWidgetId", 2)
    }

    fun saveWidgetSpacing(context: Context, appWidgetId: Int, spacing: Int) {
        getWidgetPrefs(context).edit().putInt("spacing_$appWidgetId", spacing).apply()
    }

    fun getWidgetRadius(context: Context, appWidgetId: Int): Int {
        return getWidgetPrefs(context).getInt("radius_$appWidgetId", 4)
    }

    fun saveWidgetRadius(context: Context, appWidgetId: Int, radius: Int) {
        getWidgetPrefs(context).edit().putInt("radius_$appWidgetId", radius).apply()
    }

    fun getWidgetBackground(context: Context, appWidgetId: Int): Int {
        return getWidgetPrefs(context).getInt("bg_$appWidgetId", Color.parseColor("#2B2B2B"))
    }

    fun saveWidgetBackground(context: Context, appWidgetId: Int, color: Int) {
        getWidgetPrefs(context).edit().putInt("bg_$appWidgetId", color).apply()
    }
}
