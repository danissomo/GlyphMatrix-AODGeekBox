package com.danissimo.glyphgeekbox.aod

import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.telephony.TelephonyManager
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.danissimo.glyphgeekbox.utils.renderKey
import com.danissimo.glyphgeekbox.utils.renderMobile
import com.danissimo.glyphgeekbox.utils.renderText3x5
import com.danissimo.glyphgeekbox.utils.renderWifi
import com.nothing.ketchum.Common
import com.nothing.ketchum.GlyphMatrixManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StatusBarService : GlyphMatrixService("StatusBarService") {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var frame = 0
    override fun performOnServiceConnected(
        context: Context,
        glyphMatrixManager: GlyphMatrixManager
    ) {
        backgroundScope.launch {
            while (isActive) {
                val size = Common.getDeviceMatrixLength()
                val buffer = IntArray(size * size)
                val vpnPos = 3 to 0
                val wifiPos = 1 to 3
                val mobPos = 8 to 2
                val batPos = 3 to 7
                val volPos = 0 to 4

                // 1. VPN (Key icon)
                renderKey(buffer, size, vpnPos.first, vpnPos.second, 100)
                if (isVpnActive(context)) {
                    renderKey(buffer, size, vpnPos.first, vpnPos.second, 1024)
                }

                // 2. WiFi level (1-3)
                val wifiLevel = getWifiLevel(context)
                //val wifiLevel = frame % 3 + 1
                renderWifi(buffer, size, 3, wifiPos.first, wifiPos.second, 100)
                if (wifiLevel > 0) {
                    renderWifi(buffer, size, wifiLevel, wifiPos.first, wifiPos.second, 1024)
                }

                // 3. Mobile level (1-4)
                val mobileLevel = getMobileLevel(context)
                //val mobileLevel = frame % 4 + 1
                    renderMobile(buffer, size, 4, mobPos.first, mobPos.second, 100)
                if (mobileLevel > 0) {
                    renderMobile(buffer, size, mobileLevel, mobPos.first, mobPos.second, 1024)
                }

                // 4. Battery Charge
                val batteryLevel = getBatteryLevel(context).coerceIn(0, 99)
                if (batteryLevel.toString().length < 2)
                    renderText3x5(buffer, size, "0$batteryLevel", batPos.first, batPos.second, 1024)
                else
                    renderText3x5(buffer, size, "$batteryLevel", batPos.first, batPos.second, 1024)

                // 5. Volume level (height 5)
                val volLevel = getVolumeLevel(context)
                for (i in 0 until 5) {
                    val drawY = volPos.second + (4 - i)
                    val brightness = if (i < volLevel) 1024 else 100
                    if (drawY in 0 until size && volPos.first in 0 until size) {
                        buffer[drawY * size + volPos.first] = brightness
                    }
                }

                uiScope.launch {
                    setMatrixFrame(context, glyphMatrixManager, buffer)
                }

                // Update every 2 seconds
                delay(2000)
                frame++
            }
        }
    }

    override fun performOnServiceDisconnected(context: Context) {
        backgroundScope.cancel()
    }

    private fun isVpnActive(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun getWifiLevel(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Iterate through all networks to find Wi-Fi, even if VPN is active
        val networks = cm.allNetworks
        for (network in networks) {
            val caps = cm.getNetworkCapabilities(network) ?: continue
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // When VPN is active, the transportInfo might be stripped from the VPN network,
                // but the underlying WiFi network capabilities might still contain it.
                val info = caps.transportInfo as? WifiInfo
                    ?: wm.connectionInfo // Fallback to WifiManager for basic RSSI

                if (info != null && info.rssi != -127) {
                    // Use instance method calculateSignalLevel(rssi)
                    val level = wm.calculateSignalLevel(info.rssi)
                    // Original logic expected 1-3
                    return level.coerceIn(1, 3)
                }
            }
        }
        return 0
    }

    private fun getMobileLevel(context: Context): Int {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.signalStrength?.level ?: 0
    }

    private fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getVolumeLevel(context: Context): Int {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 0
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return (current * 5) / max
    }
}
