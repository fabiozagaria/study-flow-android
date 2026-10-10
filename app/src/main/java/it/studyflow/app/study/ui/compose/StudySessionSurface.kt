package it.studyflow.app.study.ui.compose

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import it.studyflow.app.R
import it.studyflow.app.study.data.AttemptItem
import it.studyflow.app.study.data.StudyRepository
import it.studyflow.app.study.ui.StudyViewModel
import org.json.JSONArray

/** Presentation state only. Evaluation, snapshots and durable writes stay in Java/Room. */
class StudySessionSurface(
    private val model: StudyViewModel,
    saved: Bundle?,
    private val onBack: Runnable,
    private val onTheory: Runnable,
) {
    var session by mutableStateOf<StudyRepository.Session?>(null)
        private set

    var position by mutableIntStateOf(saved?.getInt("position", -1) ?: -1)
        private set

    var revealed by mutableStateOf(saved?.getBoolean("revealed", false) ?: false)
        private set

    var selected by
        mutableStateOf<Int?>(
            if (saved?.containsKey("selected") == true) saved.getInt("selected") else null
        )
        private set

    var answer by mutableStateOf(saved?.getString("answer", "") ?: "")
        private set

    var note by mutableStateOf(saved?.getString("note", "") ?: "")
        private set

    private var displayedId: String? = saved?.getString("item")
    val current: AttemptItem?
        get() = session?.items?.getOrNull(position)

    fun update(value: StudyRepository.Session) {
        session = value
        if (value.attempt.status == "COMPLETED") return
        if (position !in value.items.indices)
            position = value.items.indexOfFirst { it.outcome == "PENDING" }.coerceAtLeast(0)
        current?.let { item ->
            if (displayedId != item.id || item.outcome != "PENDING") {
                selected = item.selectedIndex
                answer = item.answer
                note = item.note
                if (displayedId != item.id) revealed = false
            }
            displayedId = item.id
        }
    }

    fun saveDraft() {
        current?.takeIf { it.outcome == "PENDING" }?.let { model.draft(it, answer, note, selected) }
    }

    fun save(out: Bundle) {
        out.putInt("position", position)
        out.putBoolean("revealed", revealed)
        out.putString("item", displayedId)
        out.putString("answer", answer)
        out.putString("note", note)
        selected?.let { out.putInt("selected", it) }
    }

    fun createView(context: Context): ComposeView =
        ComposeView(context).apply {
            id = R.id.studyCompose
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { StudyTheme { SessionScreen() } }
        }

    private fun confirm(rating: String) {
        val item = current ?: return
        if (item.outcome != "PENDING" || model.busy.value == true) return
        if (session?.attempt?.kind == "QUIZ" && rating != "SKIPPED" && selected == null) return
        model.answer(item, if (rating == "SKIPPED") null else selected, answer, note, rating)
    }

    private fun next() {
        if (model.busy.value == true || current?.outcome == "PENDING") return
        session?.let { value ->
            if (position + 1 < value.items.size) {
                position++
                update(value)
            }
        }
    }

    @Composable
    private fun SessionScreen() {
        AnimatedContent(
            targetState = session,
            contentKey = { it?.attempt?.status },
            transitionSpec = { fadeIn(tween(StudySpace.motion)) togetherWith fadeOut(tween(150)) },
            label = "Session stage",
        ) { value ->
            SessionBody(value)
        }
    }

    @Composable
    private fun SessionBody(value: StudyRepository.Session?) {
        val busy by model.busy.observeAsState(false)
        if (value == null || value.items.isEmpty()) {
            Box(
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(StudySpace.page)
            ) {
                Text(
                    if (value == null) "Caricamento della sessione…"
                    else "Nessuna domanda disponibile."
                )
            }
            return
        }
        if (value.attempt.status == "COMPLETED") {
            Result(value, busy == true)
            return
        }
        val item = current ?: return
        val quiz = value.attempt.kind == "QUIZ"
        val pending = item.outcome == "PENDING"
        val progress by
            animateFloatAsState(
                (position + 1f) / value.items.size,
                tween(StudySpace.motion),
                label = "Question progress",
            )
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = StudySpace.page, vertical = StudySpace.small)
                    ) {
                        if (pending) {
                            if (quiz)
                                Button(
                                    onClick = { confirm("CORRECT") },
                                    enabled = busy != true && selected != null,
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .heightIn(min = 52.dp)
                                            .testTagCompat("quizConfirm"),
                                ) {
                                    Text("Conferma risposta")
                                }
                            else if (!revealed)
                                Button(
                                    onClick = { revealed = true },
                                    enabled = busy != true,
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .heightIn(min = 52.dp)
                                            .testTagCompat("recallReveal"),
                                ) {
                                    Text("Confronta con la soluzione")
                                }
                            TextButton(
                                onClick = { confirm("SKIPPED") },
                                enabled = busy != true,
                                modifier = Modifier.fillMaxWidth().testTagCompat("quizSkip"),
                            ) {
                                Text("Salta questa domanda")
                            }
                        } else
                            Button(
                                onClick = ::next,
                                enabled = busy != true,
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTagCompat("quizNext"),
                            ) {
                                Text("Domanda successiva")
                            }
                    }
                }
            },
        ) { insets ->
            Column(Modifier.padding(insets)) {
                Column(
                    Modifier.padding(horizontal = StudySpace.page, vertical = StudySpace.medium),
                    verticalArrangement = Arrangement.spacedBy(StudySpace.small),
                ) {
                    StudyCaption(value.attempt.title)
                    Text(
                        "Domanda ${position + 1} di ${value.items.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                            Modifier.testTagCompat("quizPosition").semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                AnimatedContent(
                    targetState = item,
                    contentKey = { it.id },
                    label = "Question",
                    transitionSpec = {
                        fadeIn(tween(StudySpace.motion)) togetherWith fadeOut(tween(150))
                    },
                    modifier = Modifier.weight(1f),
                ) { displayed ->
                    Question(displayed, quiz, busy == true || displayed.id != item.id)
                }
            }
        }
    }

    @Composable
    private fun Question(item: AttemptItem, quiz: Boolean, busy: Boolean) {
        val pending = item.outcome == "PENDING"
        val labels = remember(item.options) { parseOptions(item.options) }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StudySpace.page),
            verticalArrangement = Arrangement.spacedBy(StudySpace.regular),
        ) {
            StudyPanel {
                StudyCaption(if (quiz) "SCEGLI UNA RISPOSTA" else "RICOSTRUISCI IL RAGIONAMENTO")
                StudyHeading(item.prompt, true)
            }
            if (quiz)
                Column(
                    Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(StudySpace.medium),
                ) {
                    labels.forEachIndexed { index, text ->
                        val choice = if (pending) selected else item.selectedIndex
                        AnswerCard(
                            text,
                            index,
                            choice == index,
                            pending && !busy,
                            when {
                                pending -> "PENDING"
                                index == item.correctIndex -> "CORRECT"
                                choice == index -> "WRONG"
                                else -> "PENDING"
                            },
                        ) {
                            selected = index
                        }
                    }
                }
            else {
                StudyCaption(
                    "Prima prova a spiegare il concetto, anche con un esempio. La valutazione finale resta una tua autovalutazione."
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    enabled = pending && !busy,
                    label = { Text("La tua spiegazione") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth().testTagCompat("recallAnswer"),
                )
            }
            AnimatedVisibility(
                !pending || revealed,
                enter = fadeIn(tween(StudySpace.motion)),
                exit = fadeOut(tween(150)),
            ) {
                StudyPanel(
                    Modifier.testTagCompat("quizFeedback").semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                ) {
                    Text(
                        outcomeLabel(item.outcome),
                        style = MaterialTheme.typography.titleMedium,
                        color =
                            if (pending) MaterialTheme.colorScheme.primary
                            else outcomeColor(item.outcome),
                    )
                    if (quiz)
                        Text(
                            "Risposta corretta: ${labels.getOrNull(item.correctIndex).orEmpty()}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    StudyProse(item.explanation)
                    if (!quiz) {
                        StudyHeading("Criteri di confronto")
                        StudyProse(item.rubric)
                        if (pending)
                            listOf(
                                    "WRONG" to "Da ripassare",
                                    "PARTIAL" to "Parzialmente corretta",
                                    "CORRECT" to "Corretta",
                                )
                                .forEach { (rating, title) ->
                                    OutlinedButton(
                                        onClick = { confirm(rating) },
                                        enabled = !busy,
                                        modifier =
                                            Modifier.fillMaxWidth().testTagCompat("rating$rating"),
                                    ) {
                                        Text(title)
                                    }
                                }
                    }
                    if (session?.attempt?.topicId?.let { it != "java" && it != "spring" } == true) {
                        TextButton(
                            onClick = {
                                saveDraft()
                                onTheory.run()
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().testTagCompat("quizTheory"),
                        ) {
                            Text("Rileggi la teoria dell’argomento")
                        }
                    }
                }
            }
            Notes(pending && !busy)
        }
    }

    @Composable
    private fun Notes(editable: Boolean) {
        var expanded by rememberSaveable(displayedId) { mutableStateOf(note.isNotBlank()) }
        StudyPanel {
            TextButton(
                onClick = { expanded = !expanded },
                modifier =
                    Modifier.fillMaxWidth().semantics {
                        stateDescription = if (expanded) "Espanso" else "Chiuso"
                    },
            ) {
                Text(
                    if (expanded) "Nascondi nota personale"
                    else "Aggiungi o leggi una nota personale"
                )
            }
            AnimatedVisibility(
                expanded,
                enter = fadeIn(tween(StudySpace.motion)),
                exit = fadeOut(tween(150)),
            ) {
                OutlinedTextField(
                    note,
                    { note = it },
                    enabled = editable,
                    label = { Text("Nota personale") },
                    modifier = Modifier.fillMaxWidth().testTagCompat("quizNote"),
                    minLines = 2,
                )
            }
        }
    }

    @Composable
    private fun Result(value: StudyRepository.Session, busy: Boolean) {
        val correct = value.items.count { it.outcome == "CORRECT" }
        val partial = value.items.count { it.outcome == "PARTIAL" }
        val wrong = value.items.count { it.outcome == "WRONG" }
        val skipped = value.items.count { it.outcome == "SKIPPED" }
        val quiz = value.attempt.kind == "QUIZ"
        LazyColumn(
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTagCompat("quizResult"),
            contentPadding = PaddingValues(StudySpace.page),
            verticalArrangement = Arrangement.spacedBy(StudySpace.regular),
        ) {
            item(key = "summary") {
                Column(verticalArrangement = Arrangement.spacedBy(StudySpace.regular)) {
                    StudyCaption("SESSIONE COMPLETATA · TENTATIVO SALVATO")
                    StudyHeading(value.attempt.title)
                    StudyPanel {
                        Text(
                            if (quiz) "$correct / ${value.items.size}"
                            else "Autovalutazione completata",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        StudyCaption(
                            if (quiz) "${correct * 100 / value.items.size}% di risposte corrette"
                            else
                                "Gli esiti sono autovalutazioni, non una correzione automatica del testo."
                        )
                        Text(
                            "$correct corrette · $partial parziali\n$wrong da ripassare · $skipped saltate",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        StudyProse(
                            if (wrong + partial + skipped > 0)
                                "Riparti dalle spiegazioni delle risposte da ripassare, parziali o saltate. Poi usa il ripasso degli errori per ricostruire il ragionamento senza leggere la soluzione."
                            else
                                "Hai completato il tentativo senza errori. Puoi rileggere le spiegazioni e provare a ricostruirle con parole tue prima di affrontare un altro argomento."
                        )
                        Button(
                            onClick = {
                                model.start(
                                    value.attempt.topicId,
                                    value.attempt.kind,
                                    true,
                                    value.items.size,
                                )
                            },
                            enabled = !busy,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTagCompat("resultReview"),
                        ) {
                            Text("Ripassa gli errori")
                        }
                        OutlinedButton(
                            onClick = {
                                model.start(
                                    value.attempt.topicId,
                                    value.attempt.kind,
                                    false,
                                    value.items.size,
                                )
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Nuovo tentativo")
                        }
                        TextButton(
                            onClick = { onBack.run() },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Torna allo studio")
                        }
                    }
                    StudyHeading("Rivedi le risposte")
                }
            }
            items(value.items, key = { it.id }) { item ->
                val choices = remember(item.options) { parseOptions(item.options) }
                val detail = buildString {
                    if (quiz)
                        append(
                            "La tua scelta: ${item.selectedIndex?.let { choices.getOrNull(it) } ?: "saltata"}\nRisposta corretta: ${choices.getOrNull(item.correctIndex).orEmpty()}\n\n"
                        )
                    else if (item.answer.isNotBlank()) append("La tua risposta\n${item.answer}\n\n")
                    append(item.explanation)
                    if (!quiz) append("\n\nCriteri\n${item.rubric}")
                    if (item.note.isNotBlank()) append("\n\nNota personale\n${item.note}")
                }
                ExpandablePanel(
                    "${outcomeLabel(item.outcome)} · ${item.prompt}",
                    detail,
                    "result-${item.id}",
                )
            }
        }
    }
}

@Composable
private fun AnswerCard(
    text: String,
    index: Int,
    selected: Boolean,
    enabled: Boolean,
    outcome: String,
    onSelect: () -> Unit,
) {
    val semantic =
        if (outcome != "PENDING") outcomeColor(outcome) else MaterialTheme.colorScheme.primary
    val target =
        when {
            outcome != "PENDING" ->
                semantic
                    .copy(alpha = if (isSystemInDarkTheme()) 0.12f else 0.06f)
                    .compositeOver(MaterialTheme.colorScheme.surface)
            selected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        }
    val fill by animateColorAsState(target, tween(150), label = "Answer background")
    val border by
        animateColorAsState(
            if (selected || outcome != "PENDING") semantic
            else MaterialTheme.colorScheme.outlineVariant,
            tween(150),
            label = "Answer border",
        )
    Surface(
        color = fill,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, border),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = 64.dp)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onSelect,
                )
                .testTagCompat("answer$index")
                .semantics(mergeDescendants = true) {
                    if (outcome != "PENDING") stateDescription = outcomeLabel(outcome)
                },
    ) {
        Row(
            Modifier.padding(StudySpace.regular),
            horizontalArrangement = Arrangement.spacedBy(StudySpace.medium),
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StudySpace.xs)) {
                Text(text, style = MaterialTheme.typography.bodyLarge)
                if (outcome != "PENDING")
                    Text(
                        outcomeLabel(outcome),
                        style = MaterialTheme.typography.labelLarge,
                        color = semantic,
                    )
                else if (selected)
                    Text(
                        "Selezionata",
                        style = MaterialTheme.typography.labelLarge,
                        color = semantic,
                    )
            }
        }
    }
}

private fun parseOptions(json: String): List<String> =
    runCatching { JSONArray(json).let { array -> List(array.length()) { array.getString(it) } } }
        .getOrDefault(emptyList())
