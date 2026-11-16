package com.aura.eventos.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.eventos.ui.components.AuraLogoGraphic
import com.aura.eventos.ui.theme.AuraGreen
import com.aura.eventos.ui.theme.AuraNavy
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onNavigateNext()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AuraLogoGraphic(
                    modifier = Modifier.size(150.dp),
                    navyColor = AuraNavy,
                    tealColor = AuraGreen
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp
                    ),
                    color = AuraNavy
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "E V E N T O S",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 10.sp
                    ),
                    color = AuraNavy
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Plataforma de Renta y Gestión de Eventos",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AuraNavy.copy(alpha = 0.65f)
                )
            }

            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(24.dp),
                color = AuraGreen,
                strokeWidth = 2.5.dp
            )
        }
    }
}
