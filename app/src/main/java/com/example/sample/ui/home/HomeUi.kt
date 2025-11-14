package com.example.sample.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sample.R
import com.example.sample.model.ControlState
import com.example.sample.model.DashBoardState
import com.example.sample.model.DeviceStatus
import com.example.sample.ui.HomeScreen
import com.example.sample.ui.theme.SampleTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(HomeScreen::class, SingletonComponent::class)
@Composable
internal fun HomeUi(state: HomeUiState, modifier: Modifier = Modifier) {

    val snackbarHostState = rememberRetained { SnackbarHostState() }

    HandleHomeSideEffects(
        state = state,
        eventSink = state.eventSink,
        snackbarHostState = snackbarHostState,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_title_iot_demo)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ConnectionSection(
                connectionState = state.connectionState,
                deviceName = state.connectedDeviceName,
                onConnect = { state.eventSink(HomeUiEvent.OnConnectClick) },
                onDisconnect = { state.eventSink(HomeUiEvent.OnDisconnectClick) }
            )

            HorizontalDivider()

            when (state.connectionState) {
                is ConnectionState.Connected -> {
                    if (state.deviceStatus != null) {
                        DashboardSection(status = state.deviceStatus)

                        HorizontalDivider()

                        ControlSection(
                            status = state.deviceStatus,
                            onToggleFan = { state.eventSink(HomeUiEvent.OnToggleFan) },
                            onToggleLed = { state.eventSink(HomeUiEvent.OnToggleLed) },
                            onToggleSpray = { state.eventSink(HomeUiEvent.OnToggleSpray) }
                        )
                    } else {
                        Text(stringResource(R.string.waiting_device_data_receive_message))
                        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                    }
                }
                is ConnectionState.Connecting -> {
                    Text(stringResource(R.string.device_connecting_message))
                    CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
                }
                is ConnectionState.Failed -> {
                    Text(
                        text = stringResource(R.string.connection_failed_with_error, state.connectionState.error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ConnectionState.Disconnected -> {
                    Text(
                        text = stringResource(R.string.home_press_connect_button_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun ConnectionSection(
    connectionState: ConnectionState,
    deviceName: String?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, containerColor, contentColor) = when (connectionState) {
        is ConnectionState.Connected -> Triple(
            stringResource(R.string.connection_status_connected),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        is ConnectionState.Connecting -> Triple(
            stringResource(R.string.device_connecting_message),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        is ConnectionState.Failed -> Triple(
            stringResource(R.string.connection_status_failed),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        is ConnectionState.Disconnected -> Triple(
            stringResource(R.string.connection_status_disconnected),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )

            Spacer(Modifier.height(8.dp))

            when (connectionState) {
                is ConnectionState.Connected -> {
                    Text(deviceName ?: stringResource(R.string.unknown_device), color = contentColor)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.home_button_disconnect_demo))
                    }
                }
                is ConnectionState.Connecting -> {
                    Text(stringResource(R.string.realtime_socket_connecting_message), color = contentColor)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ConnectionState.Failed -> {
                    Text(stringResource(R.string.error_with_message, connectionState.error), color = contentColor)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
                is ConnectionState.Disconnected -> {
                    Text(stringResource(R.string.home_socket_simulation_description), color = contentColor)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.home_button_connect_demo))
                    }
                }
            }
        }
    }
}

/**
 * 실시간 센서 데이터를 표시하는 섹션
 */
@Composable
internal fun DashboardSection(
    status: DeviceStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.home_title_realtime_sensor_data), style = MaterialTheme.typography.headlineSmall)

        val data = status.dashBoardState
        SensorCard(label = stringResource(R.string.sensor_label_temperature), value = "${data.temperature}°C")
        SensorCard(label = stringResource(R.string.sensor_label_humidity), value = "${data.humidity}%")
        SensorCard(label = stringResource(R.string.sensor_label_light), value = "${data.light} lx")
    }
}

@Composable
internal fun ControlSection(
    status: DeviceStatus,
    onToggleFan: () -> Unit,
    onToggleLed: () -> Unit,
    onToggleSpray: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.home_title_device_control), style = MaterialTheme.typography.headlineSmall)

        val control = status.controlState

        ControlCard(
            label = stringResource(R.string.control_label_fan_status),
            value = control.fan,
            onClick = onToggleFan,
            isActive = control.fan == "ON"
        )

        ControlCard(
            label = stringResource(R.string.control_label_led_status),
            value = control.led,
            onClick = onToggleLed,
            isActive = control.led == "ON"
        )

        ControlCard(
            label = stringResource(R.string.control_label_water_pump_status),
            value = control.spray,
            onClick = onToggleSpray,
            isActive = control.spray != "OFF"
        )
    }
}

@Composable
internal fun SensorCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun ControlCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview
@Composable
internal fun ConnectedHomeUiPreview() {
    SampleTheme {
        HomeUi(
            state = HomeUiState(
                connectionState = ConnectionState.Connected,
                connectedDeviceName = "디바이스 이름",
                deviceStatus = DeviceStatus(
                    dashBoardState = DashBoardState(
                        temperature = 25,
                        humidity = 60,
                        light = 1000
                    ),
                    controlState = ControlState(
                        fan = "ON",
                        led = "OFF",
                        spray = "ON"
                    )
                ),
                eventSink = {},
            )
        )
    }
}

@Preview
@Composable
internal fun UnConnectedHomeUiPreview() {
    SampleTheme {
        HomeUi(
            state = HomeUiState(
                connectionState = ConnectionState.Disconnected,
                eventSink = {},
            )
        )
    }
}