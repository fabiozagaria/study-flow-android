package it.studyflow.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import it.studyflow.app.study.ui.*;

public class StudyActivity extends AppCompatActivity {
  private int theme;
  private StudyViewModel model;

  @Override
  public void onCreate(Bundle state) {
    Appearance.apply(this);
    theme = Appearance.theme(this);
    setTheme(theme);
    super.onCreate(state);
    setContentView(R.layout.activity_study);
    new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
        .setAppearanceLightNavigationBars(getResources().getBoolean(R.bool.light_bars));
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.root),
        (v, insets) -> {
          androidx.core.graphics.Insets bars =
              insets.getInsets(
                  WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
          v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return insets;
        });
    TopLevelNavigation.attach(this, R.id.study);
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
    toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    model = new ViewModelProvider(this).get(StudyViewModel.class);
    model.notice.observe(
        this,
        message -> {
          if (message != null) {
            model.notice.setValue(null);
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
          }
        });
    model.busy.observe(
        this,
        busy ->
            findViewById(R.id.studyBusy)
                .setVisibility(Boolean.TRUE.equals(busy) ? View.VISIBLE : View.GONE));
    model.error.observe(
        this,
        message -> {
          findViewById(R.id.studyErrorPanel)
              .setVisibility(message == null ? View.GONE : View.VISIBLE);
          ((android.widget.TextView) findViewById(R.id.studyError)).setText(message);
        });
    findViewById(R.id.studyRetry)
        .setOnClickListener(
            v -> {
              model.error.setValue(null);
              model.seed();
              androidx.fragment.app.Fragment current =
                  getSupportFragmentManager().findFragmentById(R.id.studyHost);
              if (current instanceof StudySessionFragment)
                ((StudySessionFragment) current).reload();
              else if (current instanceof StudyBrowserFragment)
                ((StudyBrowserFragment) current).reload();
            });
    model.createdAttempt.observe(this, id -> dispatchCreatedAttempt());
    getSupportFragmentManager().addOnBackStackChangedListener(this::navigationVisibility);
    if (state == null)
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.studyHost, new StudyBrowserFragment())
          .commit();
  }

  private void dispatchCreatedAttempt() {
    String id = model.createdAttempt.getValue();
    if (id == null || getSupportFragmentManager().isStateSaved()) return;
    model.createdAttempt.setValue(null);
    if (getSupportFragmentManager().findFragmentById(R.id.studyHost)
        instanceof StudySessionFragment) {
      getSupportFragmentManager().popBackStackImmediate();
    }
    resumeAttempt(id);
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    dispatchCreatedAttempt();
  }

  public void openTopic(String id, String title) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.studyHost, StudyBrowserFragment.topic(id, title))
        .addToBackStack("topic")
        .commit();
  }

  public void resumeAttempt(String id) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.studyHost, StudySessionFragment.create(id))
        .addToBackStack("session")
        .commit();
  }

  public void setStudyTitle(String title, boolean session) {
    ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle(title);
    findViewById(R.id.navigation).setVisibility(session ? View.GONE : View.VISIBLE);
  }

  private void navigationVisibility() {
    findViewById(R.id.navigation)
        .setVisibility(
            getSupportFragmentManager().findFragmentById(R.id.studyHost)
                    instanceof StudySessionFragment
                ? View.GONE
                : View.VISIBLE);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (theme != Appearance.theme(this)) recreate();
  }
}
