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
import com.aura.eventos.ui.theme.*

// ============================================================
//  Marca
// ============================================================
@Composable
fun AuraLogo(modifier: Modifier = Modifier, onDark: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(AuraGreen), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Home, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text("AURA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = if (onDark) Color.White else AuraNavy)
        Text(" EVENTOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AuraGreen)
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

@Composable
fun ArticuloImagen(sku: String?, url: String?, nombre: String, dulces: Boolean, modifier: Modifier = Modifier, mostrarNombre: Boolean = true) {
    val foto = sku?.let { FOTOS[it] }
    when {
        !url.isNullOrBlank() -> AsyncImage(model = url, contentDescription = nombre, contentScale = ContentScale.Crop, modifier = modifier)
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
