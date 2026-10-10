package it.studyflow.app.study.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared presentation tokens. No dynamic palette: StudyFlow has one consistent accent. */
object StudySpace {
    val xs = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val regular = 16.dp
    val large = 24.dp
    val page = 20.dp
    const val motion = 200
}

private val Light =
    lightColorScheme(
        primary = Color(0xFF245BBB),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEAF1FF),
        onPrimaryContainer = Color(0xFF173C7A),
        secondary = Color(0xFF245BBB),
        secondaryContainer = Color(0xFFEAF1FF),
        background = Color(0xFFF5F6F8),
        onBackground = Color(0xFF202630),
        surface = Color.White,
        onSurface = Color(0xFF202630),
        surfaceVariant = Color(0xFFEEF0F4),
        onSurfaceVariant = Color(0xFF525D6B),
        outline = Color(0xFF77818F),
        outlineVariant = Color(0xFFD9DEE6),
        error = Color(0xFFA72E39),
        errorContainer = Color(0xFFFBECEF),
        onErrorContainer = Color(0xFF7B2430),
    )
private val Dark =
    darkColorScheme(
        primary = Color(0xFFA3C2FF),
        onPrimary = Color(0xFF102E62),
        primaryContainer = Color(0xFF263C5D),
        onPrimaryContainer = Color(0xFFDDE8FF),
        secondary = Color(0xFFA3C2FF),
        secondaryContainer = Color(0xFF263C5D),
        background = Color(0xFF14181E),
        onBackground = Color(0xFFE7EAF0),
        surface = Color(0xFF1D232C),
        onSurface = Color(0xFFE7EAF0),
        surfaceVariant = Color(0xFF292F39),
        onSurfaceVariant = Color(0xFFB7C0CD),
        outline = Color(0xFF939FAF),
        outlineVariant = Color(0xFF3B4655),
        error = Color(0xFFFFB1BB),
        errorContainer = Color(0xFF422A31),
        onErrorContainer = Color(0xFFFFD9DF),
    )
private val StudyTypography =
    Typography(
        headlineMedium =
            TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
        headlineSmall =
            TextStyle(fontSize = 24.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold),
        titleLarge =
            TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
        titleMedium =
            TextStyle(fontSize = 17.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 27.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
        labelLarge =
            TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    )

@Composable
fun StudyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = StudyTypography,
        shapes =
            Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(20.dp),
            ),
        content = content,
    )
}

@Composable
fun StudyPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(tween(StudySpace.motion)),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(StudySpace.regular),
            verticalArrangement = Arrangement.spacedBy(StudySpace.medium),
            content = content,
        )
    }
}

@Composable
fun StudyHeading(text: String, large: Boolean = false) {
    Text(
        text,
        style =
            if (large) MaterialTheme.typography.headlineSmall
            else MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
fun StudyCaption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun StudyProse(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(StudySpace.medium)) {
        text
            .split("\n\n")
            .filter { it.isNotBlank() }
            .forEach { paragraph ->
                if (paragraph.startsWith("## ")) StudyHeading(paragraph.removePrefix("## "))
                else Text(paragraph, style = MaterialTheme.typography.bodyLarge)
            }
    }
}

@Composable
fun outcomeColor(outcome: String): Color =
    when (outcome) {
        "CORRECT" -> if (isSystemInDarkTheme()) Color(0xFF9CD5BA) else Color(0xFF216649)
        "WRONG" -> MaterialTheme.colorScheme.error
        else -> if (isSystemInDarkTheme()) Color(0xFFE9CA97) else Color(0xFF815A20)
    }

fun outcomeLabel(outcome: String): String =
    when (outcome) {
        "CORRECT" -> "Risposta corretta"
        "WRONG" -> "Da ripassare"
        "PARTIAL" -> "Parzialmente corretta"
        "SKIPPED" -> "Domanda saltata"
        else -> "Soluzione guidata"
    }
