package com.example.sample.ui.home

import androidx.compose.runtime.Immutable
import com.example.sample.model.DeviceStatus
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import java.util.UUID

data class HomeUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val connectedDeviceName: String? = null,
    val deviceStatus: DeviceStatus? = null,
    val sideEffect: HomeSideEffect? = null,
    val eventSink: (HomeUiEvent) -> Unit,
) : CircuitUiState

@Immutable
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
    data class Failed(val error: String) : ConnectionState
}
@Immutable
sealed interface HomeSideEffect {
    data class ShowToast(
        val message: String,
        private val key: String = UUID.randomUUID().toString(),
    ) : HomeSideEffect

    data class ShowSnackbar(
        val message: String,
        private val key: String = UUID.randomUUID().toString(),
    ) : HomeSideEffect
}

sealed interface HomeUiEvent : CircuitUiEvent {
    data object OnConnectClick : HomeUiEvent
    data object OnDisconnectClick : HomeUiEvent
    data object OnToggleFan : HomeUiEvent
    data object OnToggleLed : HomeUiEvent
    data object OnToggleSpray : HomeUiEvent
    data object OnSideEffectConsumed : HomeUiEvent
}