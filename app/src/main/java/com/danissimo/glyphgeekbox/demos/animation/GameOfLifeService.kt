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
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameOfLifeService : GlyphMatrixService("Game of Life") {

    private val backgroundScope = CoroutineScope(Dispatchers.Default)
    private var grid = Array(HEIGHT) { IntArray(WIDTH) }
    private var nextGrid = Array(HEIGHT) { IntArray(WIDTH) }
    private var lastStaticFrames = 0

    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        randomizeGrid()
        backgroundScope.launch {
            while (isActive) {
                val frame = updateAndGetFrame()

                withContext(Dispatchers.Main) {
                    glyphMatrixManager.setMatrixFrame(frame)
                }

                delay(200L) // Slow down for visibility
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun randomizeGrid() {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                grid[y][x] = if (Random.nextFloat() > 0.7f) 1 else 0
            }
        }
    }

    private fun updateAndGetFrame(): IntArray {
        var changed = false
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                val neighbors = countNeighbors(x, y)
                val isAlive = grid[y][x] == 1

                val nextState = when {
                    isAlive && (neighbors < 2 || neighbors > 3) -> 0
                    !isAlive && neighbors == 3 -> 1
                    else -> grid[y][x]
                }
                
                if (nextState != grid[y][x]) changed = true
                nextGrid[y][x] = nextState
            }
        }

        // Swap grids
        val temp = grid
        grid = nextGrid
        nextGrid = temp

        if (!changed) {
            lastStaticFrames++
        } else {
            lastStaticFrames = 0
        }

        // If it gets stuck or empty, randomize again
        if (lastStaticFrames > 10 || grid.all { row -> row.all { it == 0 } }) {
            randomizeGrid()
        }

        val frame = IntArray(WIDTH * HEIGHT)
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                frame[y * WIDTH + x] = if (grid[y][x] == 1) 1024 else 0
            }
        }
        return frame
    }

    private fun countNeighbors(x: Int, y: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val ni = (y + i + HEIGHT) % HEIGHT
                val nj = (x + j + WIDTH) % WIDTH
                if (grid[ni][nj] == 1) count++
            }
        }
        return count
    }

    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()
    }

}
