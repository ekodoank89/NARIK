package com.narik.mania.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.narik.mania.R
import com.narik.mania.data.JitterRun
import com.narik.mania.domain.GeoPoint
import com.narik.mania.domain.JitterMode
import com.narik.mania.presentation.JitterIntent
import com.narik.mania.presentation.JitterViewModel
import com.narik.mania.ui.theme.NarikTheme

private val DEFAULT_CENTER = LatLng(-6.2, 106.8166666)

private val MODE_COLORS = mapOf(
    JitterMode.GRB to Color(0xFF4CAF50),
    JitterMode.GJK to Color(0xFF2196F3)
)

enum class Dest(val label: String) {
    FAV("FAV"), JIT("JIT"), HOME("HOME"), SET("SET"), OPT("OPT")
}

@Composable
fun NarikApp() {
    NarikTheme {
        val vm: JitterViewModel = viewModel()
        val state by vm.state.collectAsState()
        val context = LocalContext.current

        var destName by rememberSaveable { mutableStateOf(Dest.HOME.name) }
        var jitTabName by rememberSaveable { mutableStateOf(JitterMode.GRB.name) }
        val dest = Dest.valueOf(destName)
        val jitTab = JitterMode.valueOf(jitTabName)

        var fineGranted by remember { mutableStateOf(false) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, 15f)
        }

        val basePermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            fineGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        val bgPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        // Izin lokasi + notifikasi
        LaunchedEffect(Unit) {
            fineGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            basePermissionLauncher.launch(perms.toTypedArray())
        }

        // Izin lokasi SEPANJANG WAKTU (background) + kamera ke posisi terakhir
        LaunchedEffect(fineGranted) {
            if (!fineGranted) return@LaunchedEffect
            val bgGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!bgGranted) {
                bgPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { loc ->
                        loc?.let {
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                        }
                    }
            } catch (_: Exception) {
            }
        }

        val handleModeButton: (JitterMode) -> Unit = { mode ->
            val run = state.runs[mode] ?: JitterRun()
            if (!fineGranted) {
                Toast.makeText(context, "Izinkan lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if (run.isRunning) {
                // Mode ini sedang berjalan → STOP mode tsb saja
                vm.onIntent(JitterIntent.Stop(mode))
            } else {
                // PLAY mode tsb (mode lain tetap berjalan)
                val target = cameraPositionState.position.target
                vm.onIntent(JitterIntent.Start(mode, GeoPoint(target.latitude, target.longitude)))
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    Dest.entries.forEach { d ->
                        NavigationBarItem(
                            selected = dest == d,
                            onClick = { destName = d.name },
                            icon = { Icon(d.icon(), contentDescription = d.label) },
                            label = { Text(d.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ===== GOOGLE MAP FULL WIDTH =====
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = fineGranted)
                ) {
                    // ===== MARKER + JITTER PER MODE =====
                    state.runs.forEach { (mode, run) ->
                        if (run.isRunning) {
                            val color = MODE_COLORS[mode] ?: Color(0xFF4CAF50)
                            run.center?.let { c ->
                                val center = LatLng(c.lat, c.lng)
                                Circle(
                                    center = center,
                                    radius = state.radius.toDouble(),
                                    strokeColor = color,
                                    strokeWidth = 4f,
                                    fillColor = color.copy(alpha = 0.2f)
                                )
                                Marker(state = MarkerState(position = center), title = "Pusat $mode")
                            }
                            run.point?.let { p ->
                                Marker(
                                    state = MarkerState(position = LatLng(p.lat, p.lng)),
                                    title = "Titik $mode",
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        if (mode == JitterMode.GRB)
                                            BitmapDescriptorFactory.HUE_GREEN
                                        else
                                            BitmapDescriptorFactory.HUE_BLUE
                                    )
                                )
                            }
                        }
                    }
                }

                // ===== PIN TETAP DI TENGAH LAYAR =====
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_narik),
                        contentDescription = "Pin tengah layar",
                        tint = Color(0xFFE53935),
                        modifier = Modifier
                            .size(56.dp)
                            .offset(y = (-28).dp)
                    )
                }

                // ===== MENU COMING SOON: FAV / SET / OPT =====
                if (dest == Dest.FAV || dest == Dest.SET || dest == Dest.OPT) {
                    ComingSoonCard(dest.label, Modifier.align(Alignment.Center))
                }

                // ===== PANEL JIT =====
                if (dest == Dest.JIT) {
                    JitterPanel(
                        state = state,
                        tab = jitTab,
                        onTabChange = { jitTabName = it.name },
                        onStep = { vm.onIntent(JitterIntent.SetStep(it)) },
                        onRadius = { vm.onIntent(JitterIntent.SetRadius(it)) },
                        onInterval = { vm.onIntent(JitterIntent.SetInterval(it)) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    )
                }

                // ===== TOMBOL PLAY/STOP GRB & GJK — POSISI TETAP KIRI BAWAH =====
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeButton(
                        mode = JitterMode.GRB,
                        isRunning = state.runs[JitterMode.GRB]?.isRunning == true,
                        onClick = { handleModeButton(JitterMode.GRB) }
                    )
                    ModeButton(
                        mode = JitterMode.GJK,
                        isRunning = state.runs[JitterMode.GJK]?.isRunning == true,
                        onClick = { handleModeButton(JitterMode.GJK) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    mode: JitterMode,
    isRunning: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (isRunning) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        }
    ) {
        Text(if (isRunning) "■ STOP $mode" else "▶ PLAY $mode")
    }
}

private fun Dest.icon() = when (this) {
    Dest.FAV -> Icons.Filled.Star
    Dest.JIT -> Icons.Filled.LocationOn
    Dest.HOME -> Icons.Filled.Home
    Dest.SET -> Icons.Filled.Settings
    Dest.OPT -> Icons.Filled.MoreVert
}

@Composable
private fun ComingSoonCard(label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🚧", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("MENU $label", style = MaterialTheme.typography.titleMedium)
            Text("Coming Soon", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}
