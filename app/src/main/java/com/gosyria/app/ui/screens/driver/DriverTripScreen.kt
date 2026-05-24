package com.gosyria.app.ui.screens.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTripScreen(
    onTripCompleted: () -> Unit,
    viewModel: DriverTripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isCompleted) { if (state.isCompleted) onTripCompleted() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الرحلة الجارية", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Route info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("●", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text("الاستلام", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(state.pickupAddress.ifBlank { "..." }, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("▼", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text("الوجهة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(state.destAddress.ifBlank { "..." }, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider()
                    Text(
                        "الأجرة: ${state.fare.toInt()} ل.س",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Open Maps button
            val navAddress = if (state.status == "IN_PROGRESS") state.destAddress else state.pickupAddress
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(navAddress)}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (state.status == "IN_PROGRESS") "ملاحة إلى الوجهة" else "ملاحة إلى نقطة الاستلام")
            }

            // Status indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (state.status) {
                        "IN_PROGRESS" -> MaterialTheme.colorScheme.secondaryContainer
                        "COMPLETED"   -> MaterialTheme.colorScheme.tertiaryContainer
                        else          -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.status == "COMPLETED") {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                    Text(
                        text = when (state.status) {
                            "DRIVER_FOUND", "DRIVER_EN_ROUTE" -> "في الطريق إلى الراكب"
                            "IN_PROGRESS"                     -> "الرحلة جارية"
                            "COMPLETED"                       -> "اكتملت الرحلة!"
                            else                              -> "جاري التحميل..."
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            state.error?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            when (state.status) {
                "DRIVER_FOUND", "DRIVER_EN_ROUTE" -> Button(
                    onClick = { viewModel.arrivedAtPickup() },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onSecondary)
                    else Text("وصلت للراكب — بدء الرحلة", fontWeight = FontWeight.Bold)
                }
                "IN_PROGRESS" -> Button(
                    onClick = { viewModel.completeTrip() },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("إنهاء الرحلة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
