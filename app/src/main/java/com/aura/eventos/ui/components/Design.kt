package com.aura.eventos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aura.eventos.R
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import com.aura.eventos.ui.theme.*

// ============================================================
//  Marca y Logo Oficial Aura Eventos
// ============================================================
@Composable
fun AuraLogoGraphic(
    modifier: Modifier = Modifier,
    navyColor: Color = AuraNavy,
    tealColor: Color = AuraGreen
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeWidth = w * 0.045f

        // 1. Estrella de 4 puntos superior
        val starCx = cx
        val starCy = h * 0.10f
        val outerR = w * 0.07f
        val innerR = w * 0.022f
        val starPath = Path().apply {
            moveTo(starCx, starCy - outerR)
            lineTo(starCx + innerR, starCy - innerR)
            lineTo(starCx + outerR, starCy)
            lineTo(starCx + innerR, starCy + innerR)
            lineTo(starCx, starCy + outerR)
            lineTo(starCx - innerR, starCy + innerR)
            lineTo(starCx - outerR, starCy)
            lineTo(starCx - innerR, starCy - innerR)
            close()
        }
        drawPath(starPath, color = navyColor)

        // 2. Arcos y red esférica exterior
        val arcRadius = w * 0.38f
        val globeCenterY = h * 0.42f

        drawArc(
            color = navyColor,
            startAngle = 140f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx - arcRadius, globeCenterY - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color = tealColor,
            startAngle = 240f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(cx - arcRadius, globeCenterY - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = strokeWidth)
        )

        drawLine(
            color = navyColor,
            start = Offset(cx - arcRadius * 0.8f, globeCenterY + arcRadius * 0.2f),
            end = Offset(cx - arcRadius * 0.2f, globeCenterY - arcRadius * 0.7f),
            strokeWidth = strokeWidth * 0.8f
        )
        drawLine(
            color = tealColor,
            start = Offset(cx + arcRadius * 0.8f, globeCenterY + arcRadius * 0.2f),
            end = Offset(cx + arcRadius * 0.2f, globeCenterY - arcRadius * 0.7f),
            strokeWidth = strokeWidth * 0.8f
        )

        val nodes = listOf(
            Offset(cx - arcRadius * 0.85f, globeCenterY + arcRadius * 0.2f) to navyColor,
            Offset(cx - arcRadius * 0.6f, globeCenterY - arcRadius * 0.6f) to navyColor,
            Offset(cx + arcRadius * 0.85f, globeCenterY + arcRadius * 0.2f) to tealColor,
            Offset(cx + arcRadius * 0.6f, globeCenterY - arcRadius * 0.6f) to tealColor,
            Offset(cx, globeCenterY - arcRadius * 0.85f) to tealColor
        )
        for ((pos, col) in nodes) {
            drawCircle(color = col, radius = strokeWidth * 1.1f, center = pos)
        }

        // 3. Estructura "A" con flecha verde esmeralda ascendente
        val aBaseY = h * 0.84f
        val aTopY = h * 0.38f

        drawArc(
            color = navyColor,
            startAngle = 30f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.35f, aBaseY - h * 0.15f),
            size = Size(w * 0.70f, h * 0.25f),
            style = Stroke(width = strokeWidth)
        )

        drawLine(
            color = navyColor,
            start = Offset(cx - w * 0.28f, aBaseY),
            end = Offset(cx, aTopY),
            strokeWidth = strokeWidth * 1.1f
        )

        drawLine(
            color = navyColor,
            start = Offset(cx + w * 0.28f, aBaseY),
            end = Offset(cx, aTopY),
            strokeWidth = strokeWidth * 1.1f
        )

        val arrowStart = Offset(cx - w * 0.18f, aBaseY - h * 0.08f)
        val arrowMid = Offset(cx - w * 0.05f, aBaseY - h * 0.18f)
        val arrowTip = Offset(cx + w * 0.22f, aBaseY - h * 0.38f)

        val arrowPath = Path().apply {
            moveTo(arrowStart.x, arrowStart.y)
            lineTo(arrowMid.x, arrowMid.y)
            lineTo(arrowTip.x, arrowTip.y)
        }
        drawPath(arrowPath, color = tealColor, style = Stroke(width = strokeWidth * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val headSize = w * 0.09f
        val arrowHeadPath = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(arrowTip.x - headSize * 0.8f, arrowTip.y + headSize * 0.2f)
            lineTo(arrowTip.x - headSize * 0.2f, arrowTip.y + headSize * 0.8f)
            close()
        }
        drawPath(arrowHeadPath, color = tealColor)

        drawCircle(color = navyColor, radius = strokeWidth * 1.1f, center = Offset(cx - w * 0.28f, aBaseY))
        drawCircle(color = navyColor, radius = strokeWidth * 1.1f, center = Offset(cx, aTopY))
        drawCircle(color = tealColor, radius = strokeWidth * 1.1f, center = Offset(cx + w * 0.28f, aBaseY))
    }
}

@Composable
fun AuraLogo(modifier: Modifier = Modifier, onDark: Boolean = false) {
    val navyCol = if (onDark) Color.White else AuraNavy
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        AuraLogoGraphic(
            modifier = Modifier.size(34.dp),
            navyColor = navyCol,
            tealColor = AuraGreen
        )
        Spacer(Modifier.width(10.dp))
        Text("AURA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = navyCol)
        Text(" EVENTOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AuraGreen)
    }
}

@Composable
fun AuraFullLogo(
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    val navyCol = if (onDark) Color.White else AuraNavy
    val tealCol = AuraGreen

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AuraLogoGraphic(
            modifier = Modifier.size(76.dp),
            navyColor = navyCol,
            tealColor = tealCol
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "AURA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                ),
                color = navyCol
            )
            Text(
                text = "E V E N T O S",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                ),
                color = if (onDark) Color.White.copy(alpha = 0.9f) else AuraNavy
            )
        }
    }
}

// ============================================================
//  Barra superior (siempre con botón para regresar en las pantallas internas)
// ============================================================
@Composable
fun AuraTopBar(
    title: String, subtitle: String? = null, onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp).clip(CircleShape).background(AuraBg)) {
                    Icon(Icons.Filled.ArrowBack, "Regresar", tint = AuraNavy)
                }
                Spacer(Modifier.width(12.dp))
            } else Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = AuraNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AuraMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            actions()
        }
    }
}

/** Botón redondo para la barra superior (carrito, actualizar...). */
@Composable
fun BarIcon(icon: ImageVector, descripcion: String, onClick: () -> Unit, badge: Int = 0) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        BadgedBox(badge = { if (badge > 0) Badge(containerColor = AuraGreen, contentColor = Color.White) { Text("$badge") } }) {
            Icon(icon, descripcion, tint = AuraNavy)
        }
    }
}

// ============================================================
//  Botones (píldora, como en la web)
// ============================================================
@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    loading: Boolean = false, icon: ImageVector? = null, color: Color = AuraGreen
) {
    Button(
        onClick = onClick, enabled = enabled && !loading, modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.35f), disabledContentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        else {
            if (icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OutlineButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    icon: ImageVector? = null, color: Color = AuraNavy
) {
    OutlinedButton(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AuraBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = color)
    ) {
        if (icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ============================================================
//  Campos con etiqueta arriba
// ============================================================
@Composable
fun LabeledField(
    label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier,
    placeholder: String = "", keyboardType: KeyboardType = KeyboardType.Text, password: Boolean = false,
    singleLine: Boolean = true, minLines: Int = 1, error: String? = null, leading: ImageVector? = null,
    enabled: Boolean = true, trailingText: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AuraNavy, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange, enabled = enabled, singleLine = singleLine, minLines = minLines,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = error != null,
            placeholder = { Text(placeholder, color = AuraFaint, style = MaterialTheme.typography.bodyMedium) },
            textStyle = MaterialTheme.typography.bodyMedium,
            leadingIcon = leading?.let { { Icon(it, null, tint = AuraMuted, modifier = Modifier.size(20.dp)) } },
            trailingIcon = when {
                password -> ({ IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Mostrar contraseña", tint = AuraMuted) } })
                trailingText != null -> ({ Text(trailingText, color = AuraMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 12.dp)) })
                else -> null
            },
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuraGreen, unfocusedBorderColor = AuraBorder, errorBorderColor = AuraRed,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF8FAFC), disabledContainerColor = AuraBg,
                cursorColor = AuraGreen
            )
        )
        if (error != null) Text(error, color = AuraRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange, singleLine = true, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50), textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = { Text(placeholder, color = AuraFaint, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = AuraMuted, modifier = Modifier.size(20.dp)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuraGreen, unfocusedBorderColor = AuraBorder,
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, cursorColor = AuraGreen
        )
    )
}

// ============================================================
//  Tarjetas, etiquetas y pills
// ============================================================
@Composable
fun AuraCard(
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, color: Color = Color.White,
    borderColor: Color = AuraBorder, content: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    val base = modifier.fillMaxWidth().clip(forma).background(color).border(1.dp, borderColor, forma)
    Column(if (onClick != null) base.clickable(onClick = onClick) else base, content = content)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = AuraGold) {
    Text(text.uppercase(), modifier = modifier, style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
fun StatusPill(estado: String) {
    val (fg, bg, texto) = when (estado) {
        "APROBADO" -> Triple(AuraGreenDark, AuraGreenSoft, "APROBADO")
        "RECHAZADO" -> Triple(AuraRed, AuraRedSoft, "RECHAZADO")
        else -> Triple(AuraAmber, AuraAmberSoft, "PENDIENTE")
    }
    SoftPill(texto, fg, bg, dot = true)
}

@Composable
fun SoftPill(text: String, fg: Color, bg: Color, dot: Boolean = false, icon: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (dot) { Box(Modifier.size(6.dp).clip(CircleShape).background(fg)); Spacer(Modifier.width(6.dp)) }
            if (icon != null) { Icon(icon, null, tint = fg, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)) }
            Text(text, color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterPill(selected: Boolean, text: String, onClick: () -> Unit, accent: Color = AuraGreen, icon: ImageVector? = null) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(50),
        color = if (selected) accent else Color.White,
        border = if (selected) null else BorderStroke(1.dp, AuraBorder)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, Modifier.size(16.dp), tint = if (selected) Color.White else AuraMuted); Spacer(Modifier.width(6.dp)) }
            Text(text, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else AuraNavy, maxLines = 1)
        }
    }
}

/** Interruptor de dos opciones, como "Mobiliario y Decoración | Mesas de Dulces y Candy Bar". */
@Composable
fun SegmentedSwitch(opciones: List<Pair<String, ImageVector>>, seleccion: Int, onSelect: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = Color.White, border = BorderStroke(1.dp, AuraBorder), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(4.dp)) {
            opciones.forEachIndexed { i, (texto, icono) ->
                val activo = i == seleccion
                Surface(
                    onClick = { onSelect(i) }, shape = RoundedCornerShape(50), modifier = Modifier.weight(1f),
                    color = if (activo) AuraNavy else Color.Transparent
                ) {
                    Row(Modifier.padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(icono, null, Modifier.size(16.dp), tint = if (activo) Color.White else AuraMuted)
                        Spacer(Modifier.width(6.dp))
                        Text(texto, style = MaterialTheme.typography.labelMedium, color = if (activo) Color.White else AuraMuted,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector, tint: Color, tintBg: Color, value: String, label: String, modifier: Modifier = Modifier,
    badge: String? = null, badgeFg: Color = AuraGreenDark, badgeBg: Color = AuraGreenSoft
) {
    AuraCard(modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(tintBg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, color = AuraNavy, maxLines = 1)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AuraMuted, maxLines = 2)
            if (badge != null) SoftPill(badge, badgeFg, badgeBg)
        }
    }
}

@Composable
fun Avatar(nombre: String, size: Dp = 40.dp, bg: Color = AuraGreenSoft, fg: Color = AuraGreenDark) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Text(nombre.iniciales(), color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = AuraNavy, bold: Boolean = false, onDark: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = if (onDark) Color(0xFFB8C4D6) else AuraMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor, textAlign = TextAlign.End, modifier = Modifier.weight(1.4f))
    }
}

@Composable
fun SmallChip(text: String, icon: ImageVector? = null, fg: Color = AuraMuted, bg: Color = AuraBg) {
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, Modifier.size(12.dp), tint = fg); Spacer(Modifier.width(4.dp)) }
            Text(text, style = MaterialTheme.typography.bodySmall, color = fg, maxLines = 1)
        }
    }
}

// ============================================================
//  Estados vacíos, carga, cantidades
// ============================================================
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AuraGreen) }
}

@Composable
fun EmptyState(
    titulo: String, texto: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Inbox,
    accion: String? = null, onAccion: (() -> Unit)? = null
) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(AuraGreenSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AuraGreen, modifier = Modifier.size(34.dp))
        }
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = AuraNavy, textAlign = TextAlign.Center)
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = AuraMuted, textAlign = TextAlign.Center)
        if (accion != null && onAccion != null) { Spacer(Modifier.height(6.dp)); PrimaryButton(accion, onAccion, Modifier.widthIn(max = 260.dp)) }
    }
}

@Composable
fun QtyStepper(valor: Int, max: Int, onChange: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = Color.White, border = BorderStroke(1.dp, AuraBorder)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(valor - 1) }, modifier = Modifier.size(38.dp)) { Icon(Icons.Filled.Remove, "Menos", Modifier.size(18.dp), tint = AuraNavy) }
            Text("$valor", Modifier.widthIn(min = 26.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { if (valor < max) onChange(valor + 1) }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.Add, "Más", Modifier.size(18.dp), tint = if (valor < max) AuraNavy else AuraFaint)
            }
        }
    }
}

// ============================================================
//  Fotos de artículos (fotos incluidas de la demostración o URL; si no hay, un degradado pastel como en la web)
// ============================================================
private val FOTOS = mapOf(
    "SC-004-BLA" to R.drawable.foto_silla_chiavari, "SL-005-BLA" to R.drawable.foto_set_lounge,
    "LN-003-ROS" to R.drawable.foto_letrero_love, "CB-006-VIN" to R.drawable.foto_carrito_vintage,
    "ST-007-ACR" to R.drawable.foto_torre_cupcakes, "FF-008-CHO" to R.drawable.foto_fuente_chocolate,
    "MC-001-ORO" to R.drawable.foto_mesa_candy
)

val LocalBaseUrl = staticCompositionLocalOf { "" }

@Composable
fun ArticuloImagen(sku: String?, url: String?, nombre: String, dulces: Boolean, modifier: Modifier = Modifier, mostrarNombre: Boolean = true) {
    val foto = sku?.let { FOTOS[it] }
    val baseUrl = LocalBaseUrl.current
    val model: Any? = remember(url, baseUrl) {
        when {
            url.isNullOrBlank() -> null
            url.startsWith("/api/") -> baseUrl.trimEnd('/') + url
            url.startsWith("http://") || url.startsWith("https://") -> url
            java.io.File(url).exists() -> java.io.File(url)
            url.startsWith("/") -> java.io.File(url)
            else -> url
        }
    }
    when {
        model != null -> AsyncImage(model = model, contentDescription = nombre, contentScale = ContentScale.Crop, modifier = modifier)
        foto != null -> Image(painterResource(foto), nombre, modifier, contentScale = ContentScale.Crop)
        else -> Box(
            modifier.background(Brush.linearGradient(
                if (dulces) listOf(Color(0xFFFFF4CC), Color(0xFFFCE4EC)) else listOf(Color(0xFFEDEAF8), Color(0xFFFBE9F1))
            )),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                Icon(if (dulces) Icons.Filled.Cake else Icons.Filled.Celebration, null, tint = AuraNavy.copy(alpha = 0.45f), modifier = Modifier.size(34.dp))
                if (mostrarNombre) Text(nombre, style = MaterialTheme.typography.bodySmall, color = AuraMuted, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

/** Línea de color bajo la foto (verde en mobiliario, dorada en dulces). */
@Composable
fun AcentoLinea(color: Color) = Box(Modifier.fillMaxWidth().height(3.dp).background(color))
