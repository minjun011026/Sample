package com.example.sample.p2p.controller.api

import kotlinx.coroutines.flow.Flow

interface SocketController {
    suspend fun connectSocketToDevice(host: String? = "dummy", port: Int? = 8888): Boolean
    fun startListening(): Flow<ByteArray>
    fun sendMessage(message: String): Boolean
    suspend fun close()
}