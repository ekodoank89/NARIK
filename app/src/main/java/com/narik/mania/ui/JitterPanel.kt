package com.narik.mania.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.narik.mania.data.JitterUiState
import com.narik.mania.domain.JitterMode
import java.util.Locale

@Composable
fun JitterPanel(
    state: JitterUiState,
    tab: JitterMode,
    onTabChange: (JitterMode) -> Unit,
    onStep: (Float) -> Unit,
    onRadius: (Float) -> Unit,
    onInterval: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 88.dp)
        ) {

            TabRow(selectedTabIndex = tab.ordinal) {
                JitterMode.entries.forEach { m ->
                    Tab(
                        selected = tab == m,
                        onClick = { onTabChange(m) },
                        text = { Text("Mode ${m.name}") }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MeterSlider(
                label = "Langkah Jitter",
                value = state.step,
                range = 0.1f..20f,
                steps = 199,
                format = { String.format(Locale.US, "%.1f m", it) },
                onChange = onStep
            )
            MeterSlider(
                label = "Radius Batas Langkah",
                value = state.radius,
                range = 0.1f..20f,
                steps = 199,
                format = { String.format(Locale.US, "%.1f m", it) },
                onChange = onRadius
            )
            MeterSlider(
                label = "Interval Jitter",
                value = state.interval.toFloat(),
                range = 1f..10f,
                steps = 9,
                format = { String.format(Locale.US, "%d detik", it.toInt()) },
                onChange = { onInterval(it.toInt()) }
            )
        }
    }
}

@Composable
private fun MeterSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    format: (Float) -> String,
    onChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(
                format(value),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            steps = steps
        )
        Spacer(Modifier.height(8.dp))
    }
}
