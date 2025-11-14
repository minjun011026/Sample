package com.example.sample.p2p.controller.api

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow

interface WifiController {

    fun getDeviceNetworks(): Flow<List<ScanResult>>

    suspend fun connectToDevice(
        ssid: String,
        password: String? = null,
        bssid: String? = null,
    ): Boolean
}