package com.example.sample.p2p.controller.impl

import com.example.sample.p2p.controller.api.SocketController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

internal class FakeSocketController @Inject constructor() : SocketController {

    private var isConnected = false

    override suspend fun connectSocketToDevice(host: String?, port: Int?): Boolean {
        delay(800L)
        isConnected = true
        return true
    }

    override fun startListening(): Flow<ByteArray> = flow {
        while (true) {
            if (!isConnected) break
            delay(1_000L)

            val temp = Random.nextInt(20, 31)
            val hum = Random.nextInt(40, 61)
            val lux = Random.nextInt(100, 501)
            val fanMode = if (Random.nextBoolean()) "ON" else "OFF"

            val fakeJson = """
                {
                    "temp": $temp,
                    "hum": $hum,
                    "lux": $lux,
                    "control": {
                        "fan": "$fanMode",
                        "led": "OFF",
                        "spray": "AUTO"
                    }
                }
            """.trimIndent()

            emit(fakeJson.toByteArray())
        }
    }

    override fun sendMessage(message: String): Boolean {
        println("[FakeSocket] 명령 수신: $message")
        return true
    }

    override suspend fun close() {
        isConnected = false
    }
}