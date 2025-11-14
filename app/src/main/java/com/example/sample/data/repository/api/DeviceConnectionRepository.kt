package com.example.sample.data.repository.api

import com.example.sample.model.ControlState
import com.example.sample.model.DeviceNetworkInfo
import com.example.sample.model.DeviceStatus
import kotlinx.coroutines.flow.StateFlow

interface DeviceConnectionRepository {

    val isConnected: StateFlow<Boolean>
    val connectedDeviceName: StateFlow<String?>
    val connectedDeviceId: StateFlow<String?>
    val deviceStatus: StateFlow<DeviceStatus?>

    suspend fun connectToDevice(deviceNetworkInfo: DeviceNetworkInfo) : Result<Unit>

    suspend fun disconnectDevice()

    suspend fun sendControlCommand(controlState: ControlState): Boolean

}