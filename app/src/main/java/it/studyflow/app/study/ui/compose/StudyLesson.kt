package it.studyflow.app.study.ui.compose

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.studyflow.app.R
import it.studyflow.app.study.data.TheoryMaterial
import it.studyflow.app.study.ui.StudyViewModel
import org.json.JSONArray

/** Java/Compose boundary. Room and the Java ViewModel remain the source of truth. */
object StudyCompose {
    @JvmStatic
    fun lesson(
        context: Context,
        topic: String,
        title: String,
        model: StudyViewModel,
        returnToAttempt: Runnable?,
    ): ComposeView =
        ComposeView(context).apply {
            id = R.id.studyCompose
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                StudyTheme {
                    val material by model.material.observeAsState()
                    val busy by model.busy.observeAsState(false)
                    val lesson = material?.takeIf { it.topicId == topic }
                    if (lesson == null) {
                        Box(
                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                        ) {
                            Text("Caricamento della lezione…", Modifier.padding(StudySpace.page))
                        }
                    } else
                        Lesson(
                            lesson,
                            title,
                            busy == true,
                            onQuiz = {
                                if (returnToAttempt != null) returnToAttempt.run()
                                else model.start(topic, "QUIZ", false, 0)
                            },
                            quizLabel =
                                if (returnToAttempt == null) "Metti alla prova la comprensione"
                                else "Riprendi il tentativo",
                            onRecall = { model.start(topic, "RECALL", false, 0) },
                            onQuizErrors = { model.start(topic, "QUIZ", true, 0) },
                            onRecallErrors = { model.start(topic, "RECALL", true, 0) },
                            onRead = { model.read(topic) },
                        )
                }
            }
        }
}

@Composable
private fun Lesson(
    material: TheoryMaterial,
    title: String,
    busy: Boolean,
    onQuiz: () -> Unit,
    quizLabel: String,
    onRecall: () -> Unit,
    onQuizErrors: () -> Unit,
    onRecallErrors: () -> Unit,
    onRead: () -> Unit,
) {
    val scroll = rememberScrollState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The existing Activity already consumes system bars and keyboard insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = StudySpace.page, vertical = StudySpace.medium)
                ) {
                    Button(
                        onClick = onQuiz,
                        enabled = !busy,
                        modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTagCompat("lessonQuiz"),
                    ) {
                        Text(quizLabel)
                    }
                }
            }
        },
    ) { insets ->
        Column(Modifier.padding(insets)) {
            ReadingProgress(scroll)
            Column(
                Modifier.fillMaxSize().verticalScroll(scroll).padding(StudySpace.page),
                verticalArrangement = Arrangement.spacedBy(StudySpace.large),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(StudySpace.small)) {
                    StudyCaption("LETTURA · COMPRENSIONE · PRATICA")
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    StudyCaption(material.versionLabel)
                }
                SelectionContainer { StudyPanel { StudyProse(material.explanation) } }
                StudyPanel {
                    StudyHeading("Esempio da seguire")
                    StudyCode(material.example)
                    StudyCaption(
                        "Leggi il codice insieme alla spiegazione. Puoi selezionare e copiare il testo."
                    )
                }
                ExpandablePanel("Quando lo useresti", material.useCase, "lessonUseCase")
                ExpandablePanel(
                    "Errori comuni e come evitarli",
                    material.commonErrors,
                    "lessonErrors",
                )
                StudyPanel {
                    StudyHeading("Documentazione ufficiale")
                    StudyCaption("Usala per approfondire dopo aver compreso il meccanismo.")
                    SourceLinks(material.sources)
                }
                StudyPanel {
                    StudyHeading("Scegli il prossimo passo")
                    StudyCaption(
                        "La lettura e la comprensione sono misure distinte. Il ripasso attivo ti chiede di ricostruire la spiegazione con parole tue."
                    )
                    OutlinedButton(
                        onClick = onRecall,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text("Ripasso attivo")
                    }
                    TextButton(
                        onClick = onQuizErrors,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ripassa gli errori dei quiz")
                    }
                    TextButton(
                        onClick = onRecallErrors,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ripassa le autovalutazioni insufficienti")
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    TextButton(
                        onClick = onRead,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Segna teoria come letta")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingProgress(scroll: ScrollState) {
    val progress by
        remember(scroll) {
            derivedStateOf {
                if (scroll.maxValue == Int.MAX_VALUE || scroll.maxValue <= 0) 0f
                else (scroll.value.toFloat() / scroll.maxValue).coerceIn(0f, 1f)
            }
        }
    val animated by
        animateFloatAsState(progress, tween(StudySpace.motion), label = "Reading position")
    LinearProgressIndicator(
        progress = { animated },
        modifier =
            Modifier.fillMaxWidth().height(3.dp).semantics {
                contentDescription = "Posizione nella lettura"
            },
        trackColor = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
fun ExpandablePanel(title: String, text: String, tag: String = title) {
    var expanded by rememberSaveable(tag) { mutableStateOf(false) }
    StudyPanel {
        TextButton(
            onClick = { expanded = !expanded },
            modifier =
                Modifier.fillMaxWidth().testTagCompat(tag).semantics {
                    stateDescription = if (expanded) "Espanso" else "Chiuso"
                },
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (expanded) "Nascondi approfondimento" else "Mostra approfondimento",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        AnimatedVisibility(
            expanded,
            enter = fadeIn(tween(StudySpace.motion)),
            exit = fadeOut(tween(StudySpace.motion)),
        ) {
            SelectionContainer { StudyProse(text) }
        }
    }
}

/** Conservative lexical emphasis; it preserves the exact example and doesn't execute code. */
@Composable
fun StudyCode(code: String) {
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val styled =
        remember(code, accent, muted) {
            buildAnnotatedString {
                append(code)
                val tokens =
                    Regex(
                        "//[^\\n]*|\"(?:\\\\.|[^\"\\\\])*\"|\\b(?:public|private|protected|class|interface|record|enum|static|final|void|int|long|boolean|new|return|if|else|try|catch|throw|throws|extends|implements|synchronized|volatile)\\b"
                    )
                tokens.findAll(code).forEach { match ->
                    val color = if (match.value.startsWith("//")) muted else accent
                    addStyle(SpanStyle(color = color), match.range.first, match.range.last + 1)
                }
            }
        }
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
        SelectionContainer {
            Text(
                styled,
                modifier =
                    Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(StudySpace.regular),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun SourceLinks(json: String) {
    val links =
        remember(json) {
            runCatching {
                    JSONArray(json).let { array -> List(array.length()) { array.getString(it) } }
                }
                .getOrDefault(emptyList())
        }
    val uriHandler = LocalUriHandler.current
    links.forEach { url ->
        TextButton(
            onClick = { runCatching { uriHandler.openUri(url) } },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(url, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Tags expose stable UI contracts to instrumentation without leaking implementation into labels.
fun Modifier.testTagCompat(tag: String): Modifier = this.then(Modifier.semantics { testTag = tag })
