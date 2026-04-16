package com.danissimo.glyphgeekbox.games

import android.content.Context
import androidx.core.content.ContextCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.danissimo.glyphgeekbox.R
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.GlyphMatrixUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.danissimo.glyphgeekbox.utils.generate_circle_points
import kotlin.math.sqrt
import kotlin.random.Random

class PongService : GlyphMatrixService("PongGame"), SensorEventListener {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val raiting = 0
    private val ai_speed = 1

    private val allowedCord : List<Pair<Int, Int>> = generate_circle_points(WIDTH, WIDTH/2, HEIGHT/2)
    private val playerCords = allowedCord.withIndex().partition{it.index < allowedCord.size/2}.first
    private val botCords = allowedCord.withIndex().partition{it.index < allowedCord.size/2}.second
    private var playerIndex = playerCords.size/2f
    private var botIndex = botCords.size/2f


    // Gravity vector in matrix space
    private var targetX = 0f
    private var targetY = 9.8f
    private var targetZ = 0f


    private var lastUpdate = System.currentTimeMillis()
    private var ballPos = Pair(WIDTH/2F, HEIGHT/2F)
    private var ballSpeed = Pair((Random.nextFloat()-0.5F)/100, (Random.nextFloat()-0.5F)/100)

    enum class APP_STATE {
        WIN,
        LOSE,
        PLAY
    }

    private var curState = APP_STATE.PLAY


    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        val winIcon = GlyphMatrixObject.Builder().setImageSource(
            GlyphMatrixUtils.drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.pong_win_emoji))
        ).setPosition(0,0).build()
        val loseIcon = GlyphMatrixObject.Builder().setImageSource(
            GlyphMatrixUtils.drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.pong_lose_emoji))
        ).setPosition(0,0).build()
        val winFrame = GlyphMatrixFrame.Builder().addTop(winIcon).build(context)
        val loseFrame = GlyphMatrixFrame.Builder().addTop(loseIcon).build(context)

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)

        resetState()
        backgroundScope.launch {

            while (isActive) {
                if (curState == APP_STATE.PLAY){

                updatePhysics()
                val array = generateNextAnimationFrame()
                uiScope.launch {
                    glyphMatrixManager.setMatrixFrame(array)
                }


                }
                if (curState == APP_STATE.WIN){
                    glyphMatrixManager.setMatrixFrame(winFrame.render())
                    delay(1000)
                    resetState()
                    continue
                }
                if (curState == APP_STATE.LOSE){
                    glyphMatrixManager.setMatrixFrame(loseFrame.render())
                    delay(1000)
                    resetState()
                    continue
                }
                delay(30)
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            targetX = event.values[0]
            targetY = event.values[1]
            targetZ = event.values[2]
        }
    }

    private fun vector_cos(x1 : Float, y1 : Float, x2: Float, y2 : Float) : Float{
        return (x1*x2 + y1*y2)/(sqrt(x1*x1 + y1*y1) * sqrt(x2*x2+y2*y2))
    }
    private fun updatePhysics() {
        var  (x, y) = playerCords[playerIndex.toInt()%playerCords.size].value
        x -= WIDTH/2
        y -= WIDTH/2
        val k = if (playerIndex-1 < 0) playerCords.size -1 else playerIndex-1
        var (x1, y1) = playerCords[(k).toInt()%playerCords.size].value
        x1 -= WIDTH/2
        y1 -= WIDTH/2
        var (x2, y2) = playerCords[(playerIndex+1).toInt()%playerCords.size].value
        x2 -= WIDTH/2
        y2 -= WIDTH/2
        val cosA = vector_cos(x.toFloat(), y.toFloat(), targetX, targetY)
        val cosB = vector_cos(x1.toFloat(), y1.toFloat(), targetX, targetY)
        val cosC = vector_cos(x2.toFloat(), y2.toFloat(), targetX, targetY)
        val r  = arrayOf((cosB), (cosA), (cosC))
        val digit = r.indices.maxBy { r[it] } - 1
        val curTime = System.currentTimeMillis()
        playerIndex += ((curTime - lastUpdate)*PLAYER_SPEED*digit)
        playerIndex = playerIndex.coerceIn(1F, playerCords.size-2F)
        updateBall()
        updateAI()
        lastUpdate = System.currentTimeMillis()
    }

    private fun updateBall(){
        val elapsedTime = System.currentTimeMillis() - lastUpdate
        val nextPos = Pair(ballPos.first + elapsedTime*ballSpeed.first, ballPos.second + elapsedTime*ballSpeed.second)
        var radiusVec = Pair(nextPos.first - WIDTH/2f, nextPos.second - HEIGHT/2f)
        val norm = sqrt(radiusVec.first*radiusVec.first + radiusVec.second*radiusVec.second)
        radiusVec = Pair(radiusVec.first/norm, radiusVec.second/norm)
        if (norm >= (WIDTH-2)/2f){
            val dotProduct = radiusVec.first*ballSpeed.first + radiusVec.second*ballSpeed.second
            ballSpeed = Pair(
                (ballSpeed.first - 2*radiusVec.first*dotProduct) + (Random.nextFloat()-0.5F)/400F,
                (ballSpeed.second - 2*radiusVec.second*dotProduct) + (Random.nextFloat()-0.5F)/400F
            )
            val norm2 = sqrt(ballSpeed.first*ballSpeed.first + ballSpeed.second*ballSpeed.second)
            ballSpeed = Pair(BALL_SPEED*ballSpeed.first/norm2, BALL_SPEED*ballSpeed.second/norm2)
            chekState(nextPos)
            updateBall()
        }
        val norm2 = sqrt(ballSpeed.first*ballSpeed.first + ballSpeed.second*ballSpeed.second)
        ballSpeed = Pair(BALL_SPEED*ballSpeed.first/norm2, BALL_SPEED*ballSpeed.second/norm2)
        ballPos = Pair (nextPos.first.coerceIn(0F, WIDTH.toFloat()-1), nextPos.second.coerceIn(0F, HEIGHT.toFloat()-1))

    }
    private fun chekState(pos : Pair<Float, Float>) {
        var x = pos.first.toInt()
        var y = pos.second.toInt()
        for ((i, cord) in playerCords.withIndex()){
            //var isPlayerHere = i >= playerIndex-1 && i <= playerIndex+1
            var isPlayerHere = i == playerIndex.toInt()
            var isPointNear = x >= cord.value.first-1 && x <= cord.value.first+1 && y >= cord.value.second-1 && y <= cord.value.second+1
            if (isPointNear && !isPlayerHere){
                curState = APP_STATE.LOSE
            }
            if (isPointNear && isPlayerHere) {
                curState = APP_STATE.PLAY
                return
            }

        }
        for ((i, cord) in botCords.withIndex()){
            //var isBotHere = i >= botIndex-1 && i <= botIndex+1
            var isBotHere = botIndex.toInt() == i
            var isPointNear = x >= cord.value.first-1 && x <= cord.value.first+1 && y >= cord.value.second-1 && y <= cord.value.second+1
            if (isPointNear && !isBotHere){
                curState = APP_STATE.WIN
            }
            if (isPointNear && isBotHere) {
                curState = APP_STATE.PLAY
                return
            }
        }

    }

    private fun updateAI(){
        val (x, y) = botCords[botIndex.toInt()%botCords.size].value
        val (x1, y1) = botCords[(botIndex+1).toInt()%botCords.size].value
        val (x2, y2) = botCords[(botIndex-1).toInt()%botCords.size].value
        val (bx, by) = ballPos

        val d = Pair(bx - WIDTH/2f, by - HEIGHT/2f)
        val a = ballSpeed.first*ballSpeed.first + ballSpeed.second*ballSpeed.second
        val b = 2.0f*d.first*ballSpeed.first + 2.0f*d.second*ballSpeed.second
        val c = d.first*d.first + d.second*d.second - WIDTH*WIDTH/4f
        val D = b*b - 4*a*c
        if (D < 0) return
        val t1 = (-b + sqrt(D))/(2*a)
        val t2 = (-b - sqrt(D))/(2*a)
        val thit = if (t1 > 0) t1 else t2
        if (thit < 0) return
        val hitx = bx + ballSpeed.first*thit
        val hity = by + ballSpeed.second*thit


        val dist1 = sqrt((x-hitx)*(x-hitx) +   (y- hity)*(y- hity))
        val dist2 = sqrt((x1-hitx)*(x1-hitx) + (y1-hity)*(y1-hity))
        val dist3 = sqrt((x2-hitx)*(x2-hitx) + (y2-hity)*(y2-hity))
        val r  = arrayOf((dist3), (dist1), (dist2))
        val digit = r.indices.minBy { r[it] } - 1
        val curTime = System.currentTimeMillis()
        botIndex += ((curTime - lastUpdate)*(BOT_SPEED + raiting/10f)*digit)
        botIndex = botIndex.coerceIn(1F, botCords.size-2F)

    }

    private fun generateNextAnimationFrame(): IntArray {
        val res = IntArray(WIDTH * HEIGHT)

        val (x1, y1) = playerCords[(playerIndex.toInt()-1)%playerCords.size].value
        val (x2, y2) = playerCords[playerIndex.toInt()%playerCords.size].value
        val (x3, y3) = playerCords[(playerIndex.toInt()+1)%playerCords.size].value
        res[y1 * WIDTH + x1] = 1024
        res[y2 * WIDTH + x2] = 1024
        res[y3 * WIDTH + x3] = 1024
        res[ballPos.second.toInt() * WIDTH + ballPos.first.toInt()] = 4095
        val (bx1, by1) = botCords[(botIndex.toInt()-1)%botCords.size].value
        val (bx2, by2) = botCords[botIndex.toInt()%botCords.size].value
        val (bx3, by3) = botCords[(botIndex.toInt()+1)%botCords.size].value
        res[by1 * WIDTH + bx1] = 1024
        res[by2 * WIDTH + bx2] = 1024
        res[by3 * WIDTH + bx3] = 1024
        //frame++
        return res
    }

    private fun resetState(){
        lastUpdate = System.currentTimeMillis()
        ballPos = Pair(WIDTH/2F, HEIGHT/2F)
        ballSpeed = Pair((Random.nextFloat()-0.5F)/100, (Random.nextFloat()-0.5F)/100)
        playerIndex = playerCords.size/2f
        botIndex = botCords.size/2f
        curState = APP_STATE.PLAY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    private companion object {
        private val WIDTH = Common.getDeviceMatrixLength()
        private val HEIGHT = Common.getDeviceMatrixLength()

        private const val SPRING_K = 0.5f
        private const val DAMPING = 0.6f
        private const val PLAYER_SPEED = 0.01f
        private const val BOT_SPEED = 0.005f
        private const val BALL_SPEED = 0.007f

    }

}