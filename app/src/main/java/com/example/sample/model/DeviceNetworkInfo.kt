
package com.example.sample.model

data class DeviceNetworkInfo(
    val name: String,
    val deviceId: String,
    val ipv4: String,
    val wifi: WifiInfo? = null,
)

data class WifiInfo(
    val ssid: String,
    val password: String,
)