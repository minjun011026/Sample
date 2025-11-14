package com.example.sample.data.repository.impl

import com.example.sample.data.di.ApplicationScope
import com.example.sample.data.repository.api.DeviceConnectionRepository
import com.example.sample.model.ControlState
import com.example.sample.model.DashBoardState
import com.example.sample.model.DeviceNetworkInfo
import com.example.sample.model.DeviceStatus
import com.example.sample.model.RawDeviceData
import com.example.sample.p2p.controller.api.SocketController
import com.example.sample.p2p.controller.api.WifiController
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class DeviceConnectionRepositoryImpl @Inject constructor(
    private val wifiController: WifiController,
    private val socketController: SocketController,
    @param:ApplicationScope private val externalScope: CoroutineScope
) : DeviceConnectionRepository {

    override val connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceId = MutableStateFlow<String?>(null)
    override val isConnected = MutableStateFlow(false)
    override val deviceStatus = MutableStateFlow<DeviceStatus?>(null)

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private var listeningJob: Job? = null
    private var requestJob: Job? = null

    override suspend fun connectToDevice(deviceNetworkInfo: DeviceNetworkInfo): Result<Unit> =
        runCatching {
            // Wifi 연결 시도
            val wifiInfo = deviceNetworkInfo.wifi ?: throw IllegalStateException("Wi-Fi 정보 없음")
            val wifiConnected = wifiController.connectToDevice(
                ssid = wifiInfo.ssid,
                password = wifiInfo.password
            )
            if (!wifiConnected) throw IllegalStateException("Wi-Fi 연결 실패")

            // 소켓 연결 시도
            val socketConnected = socketController.connectSocketToDevice()
            if (!socketConnected) throw IllegalStateException("소켓 연결 실패")

            // 상태 갱신
            isConnected.value = true
            connectedDeviceName.value = deviceNetworkInfo.name
            connectedDeviceId.value = deviceNetworkInfo.deviceId

            // 리스닝 및 요청 Job 시작
            startListeningJob()
            startPeriodicRequestJob()
        }

    private fun startListeningJob() {
        listeningJob?.cancel()
        listeningJob = externalScope.launch {
            socketController.startListening()
                .onEach { rawData ->
                    Logger.d("새 데이터 수신: ${rawData.decodeToString()}")
                    processMyData(rawData)
                        .onSuccess { status ->
                            Logger.d("데이터 수신 성공: $status")
                            deviceStatus.value = status
                        }
                        .onFailure { e ->
                            // 파싱 에러는 연결 해제까지 갈 필요 없이 로그만 남기고 해당 패킷만 무시합니다.
                            Logger.e(e, "데이터 파싱 실패. 원본 데이터: ${rawData.decodeToString()}")
                        }
                }
                .catch { e ->
                    Logger.e(e, "소켓 수신 오류 발생")
                }
                .onCompletion { cause ->
                    cause?.run {
                        Logger.d("소켓 스트림이 오류로 인해 완료됨: ${cause.message}")
                    } ?: {
                        Logger.d("소켓 스트림이 정상적으로 완료됨")
                    }

                    if (isConnected.value) {
                        disconnectDevice()
                    }
                }
                .collect()
        }
    }

    private fun processMyData(rawData: ByteArray): Result<DeviceStatus> {
        return runCatching {
            val rawJsonString = rawData.decodeToString()
            // 차후 비즈니스 로직에 따라 결정되는 부분으로 샘플 앱이기에 비어있는 문자열도 현재는 에러로 처리하였습니다.
            require(rawJsonString.isNotBlank()) { "Empty JSON string received" }

            val rawDataObj = jsonParser.decodeFromString<RawDeviceData>(rawJsonString)

            val dashBoard = DashBoardState(
                temperature = rawDataObj.temp,
                humidity = rawDataObj.hum,
                light = rawDataObj.lux,
            )

            DeviceStatus(
                dashBoardState = dashBoard,
                controlState = rawDataObj.control
            )
        }
    }

    override suspend fun disconnectDevice() {
        listeningJob?.cancel()
        requestJob?.cancel()
        socketController.close()
        isConnected.value = false
        connectedDeviceName.value = null
        deviceStatus.value = null
    }

    override suspend fun sendControlCommand(controlState: ControlState): Boolean {
        val jsonCommand = jsonParser.encodeToString(controlState)
        return socketController.sendMessage(jsonCommand)
    }

    private fun startPeriodicRequestJob() {
        requestJob?.cancel()
        requestJob = externalScope.launch {
            while (isConnected.value) {
                // 5초마다 "read" 요청을 보내는 로직 시뮬레이션
                val success = socketController.sendMessage("{\"read\":true}")
                if (!success) Logger.w("'read' 요청 실패")
                delay(5_000L)
            }
        }
    }
}