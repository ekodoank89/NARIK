package com.narik.mania.data

import com.narik.mania.domain.GeoPoint
import com.narik.mania.domain.JitterMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Status satu run (GRB atau GJK). */
data class JitterRun(
    val isRunning: Boolean = false,
    val center: GeoPoint? = null,
    val point: GeoPoint? = null
)

data class JitterUiState(
    val runs: Map<JitterMode, JitterRun> = JitterMode.entries.associateWith { JitterRun() },
    val step: Float = 1.0f,
    val radius: Float = 5.0f,
    val interval: Int = 1
)

/** Single source of truth (repository) — dipakai UI & Service. */
object JitterStateHolder {

    private val _state = MutableStateFlow(JitterUiState())
    val state: StateFlow<JitterUiState> = _state.asStateFlow()

    fun update(transform: (JitterUiState) -> JitterUiState) = _state.update(transform)

    fun startRun(mode: JitterMode, center: GeoPoint) = _state.update {
        it.copy(runs = it.runs + (mode to JitterRun(isRunning = true, center = center, point = center)))
    }

    fun stopRun(mode: JitterMode) = _state.update {
        it.copy(runs = it.runs + (mode to JitterRun()))
    }

    fun updatePoint(mode: JitterMode, point: GeoPoint) = _state.update {
        val run = it.runs[mode] ?: return@update it
        it.copy(runs = it.runs + (mode to run.copy(point = point)))
    }
}
