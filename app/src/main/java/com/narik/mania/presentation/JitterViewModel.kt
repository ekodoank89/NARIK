package com.narik.mania.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.narik.mania.data.JitterStateHolder
import com.narik.mania.data.JitterUiState
import com.narik.mania.domain.GeoPoint
import com.narik.mania.domain.JitterMode
import com.narik.mania.service.JitterService
import kotlinx.coroutines.flow.StateFlow

sealed interface JitterIntent {
    data class Start(val mode: JitterMode, val center: GeoPoint) : JitterIntent
    object Stop : JitterIntent
    data class SetStep(val value: Float) : JitterIntent
    data class SetRadius(val value: Float) : JitterIntent
    data class SetInterval(val value: Int) : JitterIntent
}

class JitterViewModel(app: Application) : AndroidViewModel(app) {

    val state: StateFlow<JitterUiState> = JitterStateHolder.state

    fun onIntent(intent: JitterIntent) {
        when (intent) {
            is JitterIntent.Start -> start(intent.mode, intent.center)
            JitterIntent.Stop -> stop()
            is JitterIntent.SetStep ->
                JitterStateHolder.update { it.copy(step = intent.value.coerceIn(0.1f, 20f)) }
            is JitterIntent.SetRadius ->
                JitterStateHolder.update { it.copy(radius = intent.value.coerceIn(0.1f, 20f)) }
            is JitterIntent.SetInterval ->
                JitterStateHolder.update { it.copy(interval = intent.value.coerceIn(1, 10)) }
        }
    }

    private fun start(mode: JitterMode, center: GeoPoint) {
        val context = getApplication<Application>()
        try {
            context.startForegroundService(JitterService.startIntent(context, mode, center))
        } catch (_: Exception) {
            // izin lokasi belum diberikan / FGS gagal distart
        }
    }

    private fun stop() {
        val context = getApplication<Application>()
        try {
            context.startService(JitterService.stopIntent(context))
        } catch (_: Exception) {
            JitterStateHolder.stopRun()
        }
    }
}
