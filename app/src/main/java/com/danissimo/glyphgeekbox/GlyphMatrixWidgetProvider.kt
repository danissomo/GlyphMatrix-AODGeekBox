package com.danissimo.glyphgeekbox

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.nothing.ketchum.Common
import com.danissimo.glyphgeekbox.utils.SettingsManager
import com.danissimo.glyphgeekbox.utils.generate_all_circle_points
import kotlin.math.log10

open class GlyphMatrixWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.danissimo.glyphgeekbox.ACTION_UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_glyph_matrix)
        
        val spacing = SettingsManager.getWidgetSpacing(context, appWidgetId)
        val radius = SettingsManager.getWidgetRadius(context, appWidgetId)
        val backgroundColor = SettingsManager.getWidgetBackground(context, appWidgetId)

        // Tint the existing background (which is an oval) to preserve the round shape
        views.setColorStateList(R.id.widget_root, "setBackgroundTintList", ColorStateList.valueOf(backgroundColor))

        val frame = GlyphMatrixService.lastFrame
        if (frame != null) {
            val bitmap = renderFrameToBitmap(frame, spacing, radius)
            views.setImageViewBitmap(R.id.matrix_image, bitmap)
        } else {
            views.setImageViewResource(R.id.matrix_image, android.R.color.transparent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun renderFrameToBitmap(frame: IntArray, spacing: Int, radius: Int): Bitmap {
        val size = Common.getDeviceMatrixLength()
        val cellSize = 20
        val bitmap = Bitmap.createBitmap(size * cellSize, size * cellSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }

        val p = generate_all_circle_points(size)
        val maxVal = frame.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
        
        for ((x, y) in p){
            val index = y * size + x
            if (index >= frame.size) continue
            val intensity = frame[index]
            val alpha = (log10(9f * intensity.toFloat() / maxVal + 1f) * 255f).toInt().coerceIn(0, 255)
            paint.color = Color.argb(255, alpha, alpha, alpha)
            
            val left = (x * cellSize + spacing).toFloat()
            val top = (y * cellSize + spacing).toFloat()
            val right = ((x + 1) * cellSize - spacing).toFloat()
            val bottom = ((y + 1) * cellSize - spacing).toFloat()

            if (radius > 0) {
                canvas.drawRoundRect(left, top, right, bottom, radius.toFloat(), radius.toFloat(), paint)
            } else {
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }

        return bitmap
    }
}
