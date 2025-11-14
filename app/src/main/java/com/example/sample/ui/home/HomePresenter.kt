package com.example.sample.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.sample.data.repository.api.DeviceConnectionRepository
import com.example.sample.model.DeviceNetworkInfo
import com.example.sample.model.WifiInfo
import com.example.sample.ui.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.launch

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator, // 샘플 프로젝트로 단일 피처이기 때문에 사용하지 않았습니다.
    private val deviceConnectionRepository: DeviceConnectionRepository,
) : Presenter<HomeUiState> {

    @Composable
    override fun present(): HomeUiState {
        val scope = rememberCoroutineScope()
        var sideEffect by rememberRetained { mutableStateOf<HomeSideEffect?>(null) }
        var connectionState by rememberRetained { mutableStateOf<ConnectionState>(ConnectionState.Disconnected) }

        val isConnected by deviceConnectionRepository.isConnected.collectAsRetainedState()
        val deviceName by deviceConnectionRepository.connectedDeviceName.collectAsRetainedState()
        val deviceStatus by deviceConnectionRepository.deviceStatus.collectAsRetainedState()

        LaunchedEffect(isConnected) {
            if (isConnected && connectionState !is ConnectionState.Connected) {
                connectionState = ConnectionState.Connected
            } else if (!isConnected && connectionState is ConnectionState.Connected) {
                connectionState = ConnectionState.Disconnected
            }
        }

        fun handleEvent(event: HomeUiEvent) {
            when (event) {
                HomeUiEvent.OnConnectClick -> {
                    scope.launch {
                        val dummyInfo = DeviceNetworkInfo(
                            name = "데모 스마트 화분",
                            deviceId = "demo_device_01",
                            ipv4 = "192.168.0.1",
                            wifi = WifiInfo("fake_ssid", "fake_pw")
                        )
                        deviceConnectionRepository.connectToDevice(dummyInfo)
                            .onSuccess {
                                connectionState = ConnectionState.Connected
                                sideEffect = HomeSideEffect.ShowSnackbar("기기 연결 완료!")
                            }.onFailure { exception ->
                                val errorMsg = exception.message ?: "알 수 없는 오류"
                                connectionState = ConnectionState.Failed(errorMsg)
                                sideEffect = HomeSideEffect.ShowSnackbar("연결 실패: $errorMsg")
                            }
                    }
                }

                HomeUiEvent.OnDisconnectClick -> {
                    scope.launch {
                        deviceConnectionRepository.disconnectDevice()
                        connectionState = ConnectionState.Disconnected
                        sideEffect = HomeSideEffect.ShowSnackbar("연결이 해제되었습니다")
                    }
                }

                HomeUiEvent.OnToggleFan -> {
                    scope.launch {
                        val currentState = deviceStatus?.controlState ?: return@launch
                        val newFanState = if (currentState.fan == "ON") "OFF" else "ON"
                        val newControl = currentState.copy(fan = newFanState)

                        val success = deviceConnectionRepository.sendControlCommand(newControl)
                        sideEffect = if (success) {
                            HomeSideEffect.ShowSnackbar("팬 상태: $newFanState")
                        } else {
                            HomeSideEffect.ShowSnackbar("제어 실패")
                        }
                    }
                }

                HomeUiEvent.OnToggleLed -> {
                    scope.launch {
                        val currentState = deviceStatus?.controlState ?: return@launch
                        val newLedState = if (currentState.led == "ON") "OFF" else "ON"
                        val newControl = currentState.copy(led = newLedState)

                        val success = deviceConnectionRepository.sendControlCommand(newControl)
                        sideEffect = if (success) {
                            HomeSideEffect.ShowSnackbar("LED 상태: $newLedState")
                        } else {
                            HomeSideEffect.ShowSnackbar("제어 실패")
                        }
                    }
                }

                HomeUiEvent.OnToggleSpray -> {
                    scope.launch {
                        val currentState = deviceStatus?.controlState ?: return@launch
                        val newSprayState = when (currentState.spray) {
                            "AUTO" -> "ON"
                            "ON" -> "OFF"
                            else -> "AUTO"
                        }
                        val newControl = currentState.copy(spray = newSprayState)

                        val success = deviceConnectionRepository.sendControlCommand(newControl)
                        sideEffect = if (success) {
                            HomeSideEffect.ShowSnackbar("워터펌프 상태: $newSprayState")
                        } else {
                            HomeSideEffect.ShowSnackbar("제어 실패")
                        }
                    }
                }

                HomeUiEvent.OnSideEffectConsumed -> {
                    sideEffect = null
                }
            }
        }

        return HomeUiState(
            connectionState = connectionState,
            connectedDeviceName = deviceName,
            deviceStatus = deviceStatus,
            sideEffect = sideEffect,
            eventSink = ::handleEvent
        )
    }

    @CircuitInject(HomeScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            navigator: Navigator,
        ): HomePresenter
    }

}