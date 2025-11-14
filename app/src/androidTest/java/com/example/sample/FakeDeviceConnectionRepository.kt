package com.example.sample

import com.example.sample.data.repository.api.DeviceConnectionRepository
import com.example.sample.model.ControlState
import com.example.sample.model.DeviceNetworkInfo
import com.example.sample.model.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDeviceConnectionRepository @Inject constructor() : DeviceConnectionRepository {
    override val isConnected = MutableStateFlow(false)
    override val connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceId = MutableStateFlow<String?>(null)
    override val deviceStatus = MutableStateFlow<DeviceStatus?>(null)

    override suspend fun connectToDevice(deviceNetworkInfo: DeviceNetworkInfo): Result<Unit> {
        isConnected.value = true
        return Result.success(Unit)
    }

    override suspend fun disconnectDevice() {
        isConnected.value = false
    }

    override suspend fun sendControlCommand(controlState: ControlState): Boolean = true
}