package com.example.sample.model

import kotlinx.serialization.Serializable

@Serializable
data class ControlState(
    val fan: String,
    val led: String,
    val spray: String,
)