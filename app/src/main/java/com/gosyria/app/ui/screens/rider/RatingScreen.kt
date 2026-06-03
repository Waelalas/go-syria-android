package com.gosyria.app.ui.screens.rider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    rideId: String,
    onDone: () -> Unit,
    viewModel: RatingViewModel = hiltViewModel(),
) {
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    var selectedRating by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اكتملت رحلتك", fontWeight = FontWeight.Bold) },
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "وصلت بسلامة!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "كيف كانت تجربتك مع السائق؟",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (star in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "$star نجوم",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedRating = star },
                        tint = if (star <= selectedRating) Color(0xFFFFC107)
                               else MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }

            if (selectedRating > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when (selectedRating) {
                        1 -> "سيء جداً"
                        2 -> "سيء"
                        3 -> "مقبول"
                        4 -> "جيد"
                        else -> "ممتاز!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { viewModel.submitRating(rideId, selectedRating, onDone) },
                enabled = selectedRating > 0 && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("إرسال التقييم", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("تخطي", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
