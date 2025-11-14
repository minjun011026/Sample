package com.example.sample.p2p.di

import android.net.wifi.ScanResult
import com.example.sample.p2p.controller.api.SocketController
import com.example.sample.p2p.controller.api.WifiController
import com.example.sample.p2p.controller.impl.FakeSocketController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object P2pModule {

    @Provides
    @Singleton
    fun provideSocketController(): SocketController = FakeSocketController()

    @Provides
    @Singleton
    fun provideWifiController(): WifiController {
        return object : WifiController {
            override fun getDeviceNetworks(): Flow<List<ScanResult>> = flowOf(emptyList())

            override suspend fun connectToDevice(ssid: String, password: String?, bssid: String?) = true
        }
    }
}