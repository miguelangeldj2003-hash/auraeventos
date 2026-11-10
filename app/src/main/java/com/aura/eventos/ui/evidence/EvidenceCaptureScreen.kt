package com.aura.eventos.ui.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aura.eventos.data.local.EvidenceType
import com.aura.eventos.ui.components.*
import com.aura.eventos.ui.theme.*
import java.io.File

private fun permiso(ctx: Context, p: String) = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED

@Composable
fun EvidenceCaptureScreen(onBack: () -> Unit, vm: EvidenceViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var hasCamera by remember { mutableStateOf(permiso(ctx, Manifest.permission.CAMERA)) }
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) vm.fetchLocation() else vm.onPermissionDenied()
    }
    LaunchedEffect(Unit) { if (!hasCamera) camLauncher.launch(Manifest.permission.CAMERA) }
    LaunchedEffect(ui.saved) { if (ui.saved) onBack() }

    // Paso 1: cámara a pantalla completa
    if (ui.photoPath == null) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (hasCamera) CameraView(onPhoto = vm::onPhoto)
            else Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.NoPhotography, null, tint = Color.White, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Necesitamos permiso de cámara para tomar la evidencia.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Dar permiso", { camLauncher.launch(Manifest.permission.CAMERA) })
            }
            // Botón para regresar, siempre visible sobre la cámara
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Evidencia", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(ui.folio.folioVisible(), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        return
    }

    // Paso 2: tipo, comentario, GPS y guardar
    Scaffold(containerColor = AuraBg, topBar = { AuraTopBar("Evidencia", ui.folio.folioVisible(), onBack = onBack) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AsyncImage(model = File(ui.photoPath!!), contentDescription = "Foto", contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)))
            OutlineButton("Volver a tomar la foto", vm::onRetake, icon = Icons.Filled.Replay)

            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Tipo de evidencia")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(EvidenceType.entries.toList()) { t -> FilterPill(ui.type == t, t.label, { vm.onType(t) }) }
                    }
                    LabeledField("Comentario (opcional)", ui.comment, vm::onComment, placeholder = "Ej: entregado sin daños, firmó la clienta…", singleLine = false, minLines = 2)
                }
            }
            AuraCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("Ubicación GPS")
                    when {
                        ui.locating -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = AuraGreen); Spacer(Modifier.width(8.dp)); Text("Obteniendo GPS…")
                        }
                        ui.latitude != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = AuraGreen); Spacer(Modifier.width(8.dp))
                            Text("%.5f, %.5f  (±%d m)".format(ui.latitude, ui.longitude, (ui.accuracy ?: 0f).toInt()), color = AuraGreenDark, fontWeight = FontWeight.SemiBold)
                        }
                        else -> Text("Aún sin ubicación. El GPS funciona aunque no haya internet.", style = MaterialTheme.typography.bodySmall, color = AuraMuted)
                    }
                    ui.locationError?.let { Text(it, color = AuraRed, style = MaterialTheme.typography.bodySmall) }
                    OutlineButton("Usar mi ubicación actual", {
                        if (permiso(ctx, Manifest.permission.ACCESS_FINE_LOCATION)) vm.fetchLocation()
                        else locLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }, icon = Icons.Filled.MyLocation)
                }
            }
            PrimaryButton("Guardar evidencia", vm::save, loading = ui.saving, icon = Icons.Filled.Check)
            Text("Se guarda en el teléfono y se sube automáticamente cuando haya internet.", style = MaterialTheme.typography.bodySmall, color = AuraMuted,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun CameraView(onPhoto: (String) -> Unit) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(ctx) }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(ctx).get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        provider.unbindAll()
        provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView({ previewView }, Modifier.fillMaxSize())
        Box(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).size(80.dp)
                .clip(CircleShape).background(Color.White).border(5.dp, AuraGreen, CircleShape)
                .clickable {
                    val dir = File(ctx.filesDir, "evidencias").apply { mkdirs() }   // almacenamiento interno, no se pierde
                    val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
                    imageCapture.takePicture(
                        ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(ctx),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(r: ImageCapture.OutputFileResults) = onPhoto(file.absolutePath)
                            override fun onError(e: ImageCaptureException) {}
                        })
                }
        )
    }
}
