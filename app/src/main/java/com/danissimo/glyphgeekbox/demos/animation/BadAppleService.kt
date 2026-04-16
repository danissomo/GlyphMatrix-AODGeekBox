package com.danissimo.glyphgeekbox.demos.animation

import android.content.Context
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class BadAppleService : GlyphMatrixService("Bad-apple-demo") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var frame = 0
    private var animationFrames: List<Pair<Long, IntArray>>? = null

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        if (animationFrames == null) {
            animationFrames = loadAnimationData(context)
        }

        backgroundScope.launch {
            while (isActive) {
                val frames = animationFrames ?: break
                if (frames.isEmpty()) break

                val array = generateNextAnimationFrame()
                val duration = frames[frame].first

                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(array)
                }
                
                delay(duration)
                
                frame = (frame + 1) % frames.size
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun loadAnimationData(context: Context): List<Pair<Long, IntArray>> {
        return try {
            val jsonString = context.assets.open("bad_apple_animation.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val framesArray = jsonObject.getJSONArray("frames")
            List(framesArray.length()) { i ->
                val frameObj = framesArray.getJSONObject(i)
                val duration = frameObj.optLong("d", 50L)
                val pArray = frameObj.getJSONArray("p")
                val pixels = IntArray(pArray.length()) { j -> pArray.getInt(j) }.map{p -> (1024*p/255f).toInt()}.toTypedArray().toIntArray()

                duration to pixels
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun generateNextAnimationFrame(): IntArray {
        val frames = animationFrames ?: return IntArray(WIDTH * HEIGHT)
        if (frames.isEmpty()) return IntArray(WIDTH * HEIGHT)
        return frames[frame % frames.size].second
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
    }

}
