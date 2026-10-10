package it.studyflow.app.study.ui;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import it.studyflow.app.R;
import it.studyflow.app.StudyActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Exercise the real catalog/lesson/quiz views and export screenshots for visual review. */
@RunWith(AndroidJUnit4.class)
public class StudyVisualTest {
  @Test
  public void catalogLessonAndQuizRenderInBothThemes() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    int original =
        context.getSharedPreferences("appearance", Context.MODE_PRIVATE).getInt("mode", -1);
    try {
      for (boolean dark : new boolean[] {false, true}) {
        context
            .getSharedPreferences("appearance", Context.MODE_PRIVATE)
            .edit()
            .putInt(
                "mode", dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO)
            .commit();
        String prefix = dark ? "dark" : "light";
        try (ActivityScenario<StudyActivity> scenario =
            ActivityScenario.launch(StudyActivity.class)) {
          await(
              scenario,
              a ->
                  ((RecyclerView) a.findViewById(R.id.studyTopics)).getAdapter().getItemCount()
                      == 67);
          capture(context, prefix + "-catalog");
          scenario.onActivity(
              a -> {
                a.findViewById(R.id.filterSpring).performClick();
                assertTrue(a.findViewById(R.id.filterSpring).isSelected());
                assertEquals(
                    36,
                    ((RecyclerView) a.findViewById(R.id.studyTopics)).getAdapter().getItemCount());
                a.openTopic("java-runtime", "JDK, bytecode e JVM");
              });
          await(
              scenario,
              a -> {
                LinearLayout content = a.findViewById(R.id.studyContent);
                return content != null && content.getChildCount() >= 7;
              });
          capture(context, prefix + "-lesson");
          scenario.onActivity(
              a -> {
                LinearLayout content = a.findViewById(R.id.studyContent);
                ((androidx.core.widget.NestedScrollView) a.findViewById(R.id.studyDetail))
                    .scrollTo(0, content.getChildAt(2).getTop());
              });
          capture(context, prefix + "-example");
          scenario.onActivity(
              a ->
                  new ViewModelProvider(a)
                      .get(StudyViewModel.class)
                      .start("java-types", "QUIZ", false, 0));
          await(
              scenario,
              a -> {
                RadioGroup options = a.findViewById(R.id.sessionOptions);
                return options != null && options.getChildCount() == 3;
              });
          scenario.onActivity(
              a -> {
                assertEquals(View.GONE, a.findViewById(R.id.navigation).getVisibility());
                RadioGroup options = a.findViewById(R.id.sessionOptions);
                options.check(options.getChildAt(0).getId());
              });
          capture(context, prefix + "-quiz");
          scenario.onActivity(a -> a.findViewById(R.id.sessionConfirm).performClick());
          await(
              scenario, a -> a.findViewById(R.id.sessionFeedback).getVisibility() == View.VISIBLE);
          scenario.onActivity(
              a ->
                  ((androidx.core.widget.NestedScrollView) a.findViewById(R.id.sessionScroll))
                      .scrollTo(0, a.findViewById(R.id.sessionFeedback).getTop()));
          capture(context, prefix + "-feedback");
        }
      }
    } finally {
      context
          .getSharedPreferences("appearance", Context.MODE_PRIVATE)
          .edit()
          .putInt("mode", original)
          .commit();
    }
  }

  private void await(ActivityScenario<StudyActivity> scenario, Predicate<StudyActivity> condition) {
    long deadline = SystemClock.uptimeMillis() + 15000;
    AtomicBoolean ready = new AtomicBoolean(false);
    while (SystemClock.uptimeMillis() < deadline) {
      scenario.onActivity(a -> ready.set(condition.test(a)));
      if (ready.get()) return;
      SystemClock.sleep(100);
    }
    fail("Study screen did not reach the expected state");
  }

  private void capture(Context context, String name) throws Exception {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    SystemClock.sleep(350);
    Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
    assertNotNull(bitmap);
    File folder = new File(context.getExternalFilesDir(null), "study-visual");
    assertTrue(folder.isDirectory() || folder.mkdirs());
    try (FileOutputStream out = new FileOutputStream(new File(folder, name + ".png"))) {
      assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out));
    } finally {
      bitmap.recycle();
    }
    shell("mkdir -p /data/local/tmp/study-visual");
    shell(
        "cp "
            + new File(folder, name + ".png").getAbsolutePath()
            + " /data/local/tmp/study-visual/"
            + name
            + ".png");
  }

  private void shell(String command) throws Exception {
    try (InputStream input =
        new android.os.ParcelFileDescriptor.AutoCloseInputStream(
            InstrumentationRegistry.getInstrumentation()
                .getUiAutomation()
                .executeShellCommand(command))) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int count;
      while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
      assertEquals("", output.toString("UTF-8").trim());
    }
  }
}
