package com.danissimo.glyphgeekbox

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.nothing.ketchum.Common
import com.danissimo.glyphgeekbox.utils.generate_all_circle_points
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
        
        val frame = GlyphMatrixService.lastFrame
        if (frame != null) {
            val bitmap = renderFrameToBitmap(frame)
            views.setImageViewBitmap(R.id.matrix_image, bitmap)
        } else {
            views.setImageViewResource(R.id.matrix_image, android.R.color.transparent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun renderFrameToBitmap(frame: IntArray): Bitmap {
        val size = Common.getDeviceMatrixLength()
        val cellSize = 20
        val bitmap = Bitmap.createBitmap(size * cellSize, size * cellSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        val p = generate_all_circle_points(size)
        for ((x, y) in p){
            val intensity = frame[y * size + x]
            val alpha = (intensity.toFloat() / frame.max() * 255f).toInt().coerceIn(0, 255)
            paint.color = Color.argb(255, alpha, alpha, alpha)
            canvas.drawRect(
                (x * cellSize).toFloat(),
                (y * cellSize).toFloat(),
                ((x + 1) * cellSize).toFloat(),
                ((y + 1) * cellSize).toFloat(),
                paint
            )
        }

        return bitmap
    }
}