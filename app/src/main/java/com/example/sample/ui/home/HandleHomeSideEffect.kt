package com.example.sample.ui.home

import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun HandleHomeSideEffects(
    state: HomeUiState,
    eventSink: (HomeUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {

    val context = LocalContext.current

    LaunchedEffect(state.sideEffect) {
        when (state.sideEffect) {
            is HomeSideEffect.ShowToast -> {
                Toast.makeText(context, state.sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            is HomeSideEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(state.sideEffect.message)
                eventSink(HomeUiEvent.OnSideEffectConsumed)
            }

            null -> {}
        }
    }

}