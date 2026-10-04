package com.narik.mania.data

import com.narik.mania.domain.GeoPoint
import com.narik.mania.domain.JitterMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class JitterUiState(
    val isRunning: Boolean = false,
    val mode: JitterMode? = null,
    val step: Float = 1.0f,
    val radius: Float = 5.0f,
    val interval: Int = 1,
    val center: GeoPoint? = null,
    val point: GeoPoint? = null
)

/** Single source of truth (repository) — dipakai UI & Service. */
object JitterStateHolder {

    private val _state = MutableStateFlow(JitterUiState())
    val state: StateFlow<JitterUiState> = _state.asStateFlow()

    fun update(transform: (JitterUiState) -> JitterUiState) = _state.update(transform)

    fun startRun(mode: JitterMode, center: GeoPoint) = _state.update {
        it.copy(isRunning = true, mode = mode, center = center, point = center)
    }

    fun stopRun() = _state.update {
        it.copy(isRunning = false, mode = null, center = null, point = null)
    }
}
