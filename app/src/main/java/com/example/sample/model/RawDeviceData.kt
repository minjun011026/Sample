package com.example.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class RawDeviceData(
    val temp: Int,
    val hum: Int,
    val lux: Int,
    val control: ControlState,
)