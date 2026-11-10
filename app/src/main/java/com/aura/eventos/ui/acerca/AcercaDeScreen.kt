package com.aura.eventos.ui.acerca

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.eventos.BuildConfig
import com.aura.eventos.ui.components.AuraCard
import com.aura.eventos.ui.components.AuraTopBar
import com.aura.eventos.ui.theme.*

@Composable
fun AcercaDeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember { obtenerVersion(context) }

    Scaffold(
        containerColor = AuraBg,
        topBar = { AuraTopBar("Acerca de", "Información de la aplicación", onBack = onBack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AuraNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = AuraGreen,
                    modifier = Modifier.size(50.dp)
                )
            }

            Text("Aura Eventos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AuraNavy)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AuraGreenSoft
            ) {
                Text(
                    text = "V $versionName",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuraGreenDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuraCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Smartphone, null, tint = AuraGreenDark)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Versión instalada", style = MaterialTheme.typography.labelSmall, color = AuraMuted)
                            Text("V $versionName", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = AuraBorder)
                    Text("Plataforma móvil de gestión y renta de mobiliario para eventos.", style = MaterialTheme.typography.bodyMedium)
                    Text("© 2026 Aura Eventos. Todos los derechos reservados.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                }
            }
        }
    }
}

private fun obtenerVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: BuildConfig.VERSION_NAME
    } catch (e: Exception) {
        BuildConfig.VERSION_NAME
    }
}
