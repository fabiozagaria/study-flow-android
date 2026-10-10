package it.studyflow.app.study.ui

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.studyflow.app.StudyActivity
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real ViewModel/Room journeys, including reconstruction of presentation state. */
@RunWith(AndroidJUnit4::class)
class StudyComposeTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun readingQuizResultsAndErrorReviewInBothThemes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        val original = prefs.getInt("mode", -1)
        try {
            for (dark in listOf(false, true)) {
                prefs
                    .edit()
                    .putInt(
                        "mode",
                        if (dark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO,
                    )
                    .commit()
                val prefix = if (dark) "dark" else "light"
                ActivityScenario.launch(StudyActivity::class.java).use { scenario ->
                    awaitSeed(scenario)
                    scenario.onActivity {
                        it.openTopic("java-threads", "Thread, Runnable e ciclo di vita")
                    }
                    awaitNode("lessonQuiz")
                    compose.onNodeWithText("Costruiamo il concetto").assertExists()
                    capture(context, "$prefix-lesson")
                    compose.onNodeWithText("Esempio da seguire").performScrollTo()
                    capture(context, "$prefix-example")
                    compose.onNodeWithTag("lessonErrors").performScrollTo().performClick()
                    compose
                        .onNodeWithTag("lessonErrors")
                        .assert(
                            SemanticsMatcher.expectValue(
                                androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
                                "Espanso",
                            )
                        )
                    compose.onNodeWithTag("lessonQuiz").performClick()
                    awaitNode("quizPosition")
                    for (question in 0..3) {
                        val correct = AtomicInteger()
                        scenario.onActivity { activity ->
                            correct.set(
                                ViewModelProvider(activity)[StudyViewModel::class.java]
                                    .session
                                    .value!!
                                    .items[question]
                                    .correctIndex
                            )
                        }
                        val choice = if (question == 0) (correct.get() + 1) % 3 else correct.get()
                        compose.onNodeWithTag("answer$choice").performScrollTo().performClick()
                        if (question == 0) {
                            scenario.recreate()
                            awaitNode("answer$choice")
                            compose.onNodeWithTag("answer$choice").assertIsSelected()
                            capture(context, "$prefix-quiz")
                        }
                        compose.onNodeWithTag("quizConfirm").performClick()
                        if (question == 3) awaitNode("quizResult")
                        else {
                            awaitNode("quizFeedback")
                            compose.onNodeWithTag("quizFeedback").performScrollTo()
                            if (question == 0) {
                                capture(context, "$prefix-feedback")
                                compose.onNodeWithTag("quizTheory").performScrollTo().performClick()
                                awaitNode("lessonQuiz")
                                compose.onNodeWithTag("lessonQuiz").performClick()
                                awaitNode("quizFeedback")
                                compose
                                    .onNodeWithTag("quizPosition")
                                    .assertTextEquals("Domanda 1 di 4")
                            }
                            compose.onNodeWithTag("quizNext").performClick()
                            compose
                                .onNodeWithTag("quizPosition")
                                .assertTextEquals("Domanda ${question + 2} di 4")
                        }
                    }
                    compose.onNodeWithText("3 / 4").assertExists()
                    capture(context, "$prefix-result")
                    compose.onNodeWithTag("resultReview").performScrollTo().performClick()
                    awaitNode("quizPosition")
                    scenario.onActivity { activity ->
                        val value =
                            ViewModelProvider(activity)[StudyViewModel::class.java].session.value!!
                        assertEquals("QUIZ", value.attempt.kind)
                        assertTrue(value.items.isNotEmpty())
                        assertTrue(value.items.size <= 4)
                    }
                }
            }
        } finally {
            prefs.edit().putInt("mode", original).commit()
        }
    }

    @Test
    fun recallDraftAndManualRatingSurviveRecreation() {
        ActivityScenario.launch(StudyActivity::class.java).use { scenario ->
            awaitSeed(scenario)
            scenario.onActivity {
                ViewModelProvider(it)[StudyViewModel::class.java].start(
                    "java-threads",
                    "RECALL",
                    false,
                    1,
                )
            }
            awaitNode("recallAnswer")
            compose
                .onNodeWithTag("recallAnswer")
                .performTextInput("Runnable descrive il lavoro; start avvia il thread.")
            scenario.recreate()
            awaitNode("recallAnswer")
            compose
                .onNodeWithTag("recallAnswer")
                .assertTextContains("Runnable descrive il lavoro; start avvia il thread.")
            compose.onNodeWithTag("recallReveal").performClick()
            compose.onNodeWithTag("ratingPARTIAL").performScrollTo().performClick()
            awaitNode("quizResult")
            scenario.onActivity { activity ->
                val value = ViewModelProvider(activity)[StudyViewModel::class.java].session.value!!
                assertEquals("PARTIAL", value.items.single().outcome)
                assertEquals(
                    "Runnable descrive il lavoro; start avvia il thread.",
                    value.items.single().answer,
                )
            }
        }
    }

    @Test
    fun largeTextAndDisabledAnimationsKeepQuizUsable() {
        val scale = shellOutput("settings get system font_scale").trim()
        val animation = shellOutput("settings get global animator_duration_scale").trim()
        try {
            shell("settings put system font_scale 1.5")
            shell("settings put global animator_duration_scale 0")
            ActivityScenario.launch(StudyActivity::class.java).use { scenario ->
                awaitSeed(scenario)
                scenario.onActivity {
                    assertTrue(it.resources.configuration.fontScale >= 1.5f)
                    ViewModelProvider(it)[StudyViewModel::class.java].start(
                        "java-locks",
                        "QUIZ",
                        false,
                        1,
                    )
                }
                awaitNode("answer0")
                compose.onNodeWithTag("answer0").performScrollTo().performClick().assertIsSelected()
                compose
                    .onNodeWithTag("quizConfirm")
                    .assertIsDisplayed()
                    .assertIsEnabled()
                    .performClick()
                awaitNode("quizResult")
            }
        } finally {
            shell(
                if (scale == "null") "settings delete system font_scale"
                else "settings put system font_scale $scale"
            )
            shell(
                if (animation == "null") "settings delete global animator_duration_scale"
                else "settings put global animator_duration_scale $animation"
            )
        }
    }

    private fun awaitNode(tag: String) {
        compose.waitUntil(20000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
    }

    private fun awaitSeed(scenario: ActivityScenario<StudyActivity>) {
        compose.waitUntil(20000) {
            var ready = false
            scenario.onActivity {
                ready = ViewModelProvider(it)[StudyViewModel::class.java].catalog.value?.size == 67
            }
            ready
        }
    }

    private fun capture(context: Context, name: String) {
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        assertNotNull(bitmap)
        val folder = File(context.getExternalFilesDir(null), "study-visual")
        assertTrue(folder.isDirectory || folder.mkdirs())
        val file = File(folder, "$name.png")
        file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
        shell("mkdir -p /data/local/tmp/study-visual")
        shell("cp ${file.absolutePath} /data/local/tmp/study-visual/$name.png")
    }

    private fun shell(command: String) {
        assertEquals("", shellOutput(command).trim())
    }

    private fun shellOutput(command: String): String {
        ParcelFileDescriptor.AutoCloseInputStream(
                InstrumentationRegistry.getInstrumentation()
                    .uiAutomation
                    .executeShellCommand(command)
            )
            .use {
                return it.readBytes().toString(Charsets.UTF_8)
            }
    }
}
