package com.example.batterymonitor_compose
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

const val ACTION_ACTUALIZAR_BATERIA = "com.unsa.batterymonitor.ACTUALIZAR_BATERIA"
const val REQUEST_CODE_BATTERY = 1001

@Composable
fun BatteryMonitorAdvancedScreen() {
    val context = LocalContext.current

    var porcentaje by remember { mutableStateOf(0) }
    var estaCargando by remember { mutableStateOf(false) }
    var estadoBateriaTexto by remember { mutableStateOf("Desconocido") }
    var origenActualizacion by remember { mutableStateOf("Inicializando...") }

    // Función utilitaria para procesar el Intent de batería
    fun procesarIntentBateria(intent: Intent?, origen: String) {
        if (intent == null) return
        val nivel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val escala = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        if (nivel != -1 && escala != -1) {
            porcentaje = (nivel * 100) / escala
        }

        estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        estadoBateriaTexto = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
            BatteryManager.BATTERY_STATUS_FULL -> "Carga Completa"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descargando"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Conectado, sin cargar"
            else -> "No disponible"
        }

        origenActualizacion = origen
        Log.d("BatteryAdvanced", "[$origen] Nivel: $porcentaje%, Estado: $estadoBateriaTexto")
    }

    // Efecto 1: BroadcastReceiver automático del sistema
    DisposableEffect(context) {
        val systemReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                procesarIntentBateria(intent, "Evento Automático del Sistema")
            }
        }
        context.registerReceiver(systemReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d("BatteryAdvanced", "System Receiver registrado")

        onDispose {
            context.unregisterReceiver(systemReceiver)
            Log.d("BatteryAdvanced", "System Receiver desregistrado")
        }
    }

    // Efecto 2: BroadcastReceiver personalizado para PendingIntent
    DisposableEffect(context) {
        val customReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                // Consulta del último estado sticky de batería
                val batteryStatusIntent = context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                procesarIntentBateria(batteryStatusIntent, "Disparo Manual (PendingIntent)")
            }
        }

        val filter = IntentFilter(ACTION_ACTUALIZAR_BATERIA)
        ContextCompat.registerReceiver(
            context,
            customReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        Log.d("BatteryAdvanced", "Custom Receiver registrado (NOT_EXPORTED)")

        onDispose {
            context.unregisterReceiver(customReceiver)
            Log.d("BatteryAdvanced", "Custom Receiver desregistrado")
        }
    }

    // Función para emitir el broadcast manual con PendingIntent
    fun dispararBroadcastManual() {
        val intent = Intent(ACTION_ACTUALIZAR_BATERIA).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BATTERY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            pendingIntent.send()
            Log.d("BatteryAdvanced", "PendingIntent enviado exitosamente")
        } catch (e: PendingIntent.CanceledException) {
            Log.e("BatteryAdvanced", "Error al enviar PendingIntent", e)
        }
    }

    // Estructura de Interfaz de Usuario
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MONITOR DE BATERÍA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$porcentaje%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (porcentaje <= 15) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { porcentaje / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = if (porcentaje <= 15) Color.Red else MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Estado de carga:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = estadoBateriaTexto,
                            color = if (estaCargando) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Última fuente:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = origenActualizacion,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { dispararBroadcastManual() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Actualizar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Actualizar manualmente", fontSize = 16.sp)
            }
        }
    }
}