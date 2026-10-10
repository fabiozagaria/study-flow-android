package it.studyflow.app.study.ui;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import it.studyflow.app.*;
import it.studyflow.app.study.data.*;
import java.util.*;

public class StudyBrowserFragment extends Fragment {
  private StudyViewModel model;
  private TopicAdapter adapter;
  private List<TopicRow> topics = Collections.emptyList();
  private List<AttemptSummary> history = Collections.emptyList();
  private String filter = "", query = "";
  private boolean showHistory;
  private View root;
  private String topicId;

  public StudyBrowserFragment() {
    super(R.layout.fragment_study_browser);
  }

  public static StudyBrowserFragment topic(String id, String title) {
    StudyBrowserFragment f = new StudyBrowserFragment();
    Bundle b = new Bundle();
    b.putString("topic", id);
    b.putString("title", title);
    f.setArguments(b);
    return f;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    root = view;
    model = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);
    topicId = getArguments() == null ? null : getArguments().getString("topic");
    if (state != null) {
      filter = state.getString("filter", "");
      query = state.getString("query", "");
      showHistory = state.getBoolean("history");
    }
    ((StudyActivity) requireActivity())
        .setStudyTitle(topicId == null ? "Studio" : getArguments().getString("title"), false);
    RecyclerView list = view.findViewById(R.id.studyTopics);
    list.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter = new TopicAdapter(t -> ((StudyActivity) requireActivity()).openTopic(t.id, t.title));
    list.setAdapter(adapter);
    EditText search = view.findViewById(R.id.studySearch);
    search.setText(query);
    search.addTextChangedListener(
        new TextWatcher() {
          public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

          public void onTextChanged(CharSequence s, int st, int before, int count) {
            query = s.toString();
            renderCatalog();
          }

          public void afterTextChanged(Editable e) {}
        });
    view.findViewById(R.id.filterAll).setOnClickListener(v -> filter(""));
    view.findViewById(R.id.filterJava).setOnClickListener(v -> filter("java"));
    view.findViewById(R.id.filterSpring).setOnClickListener(v -> filter("spring"));
    view.findViewById(R.id.filterHistory)
        .setOnClickListener(
            v -> {
              showHistory = true;
              renderCatalog();
            });
    view.findViewById(R.id.mixedJava).setOnClickListener(v -> mixed("java"));
    view.findViewById(R.id.mixedSpring).setOnClickListener(v -> mixed("spring"));
    model.catalog.observe(
        getViewLifecycleOwner(),
        rows -> {
          topics = rows;
          renderCatalog();
        });
    model.history.observe(
        getViewLifecycleOwner(),
        rows -> {
          history = rows;
          renderCatalog();
        });
    model.material.observe(
        getViewLifecycleOwner(),
        material -> {
          if (topicId != null && material != null && topicId.equals(material.topicId))
            renderMaterial(material);
        });
    if (topicId != null) {
      for (int id :
          new int[] {
            R.id.studySearch,
            R.id.studyHero,
            R.id.studyFilters,
            R.id.studyCount,
            R.id.mixedActions,
            R.id.studyTopics
          }) view.findViewById(id).setVisibility(View.GONE);
      view.findViewById(R.id.studyDetail).setVisibility(View.VISIBLE);
      reload();
    }
  }

  private void filter(String value) {
    filter = value;
    showHistory = false;
    renderCatalog();
  }

  private void mixed(String subject) {
    new MaterialAlertDialogBuilder(requireContext())
        .setTitle("Sessione mista")
        .setItems(
            new String[] {
              "Quiz · 10 domande",
              "Quiz · 20 domande",
              "Quiz · 50 domande",
              "Ripasso attivo · 10 domande",
              "Ripassa gli errori dei quiz"
            },
            (d, which) ->
                model.start(
                    subject,
                    which == 3 ? "RECALL" : "QUIZ",
                    which == 4,
                    which == 2 ? 50 : which == 1 ? 20 : 10))
        .show();
  }

  private void renderCatalog() {
    if (root == null || topicId != null) return;
    root.findViewById(R.id.studyHero)
        .setVisibility(
            showHistory
                    || !query.trim().isEmpty()
                    || getResources().getConfiguration().fontScale >= 1.3f
                ? View.GONE
                : View.VISIBLE);
    int selected =
        showHistory
            ? R.id.filterHistory
            : filter.equals("java")
                ? R.id.filterJava
                : filter.equals("spring") ? R.id.filterSpring : R.id.filterAll;
    for (int id :
        new int[] {R.id.filterAll, R.id.filterJava, R.id.filterSpring, R.id.filterHistory}) {
      com.google.android.material.button.MaterialButton button = root.findViewById(id);
      boolean active = id == selected;
      button.setSelected(active);
      button.setBackgroundTintList(
          android.content.res.ColorStateList.valueOf(
              StudyViews.color(
                  requireContext(), active ? R.color.study_quiz_soft : R.color.study_surface)));
      button.setTextColor(
          StudyViews.color(requireContext(), active ? R.color.study_quiz : R.color.study_muted));
      button.setStrokeColor(
          android.content.res.ColorStateList.valueOf(
              StudyViews.color(
                  requireContext(), active ? R.color.study_quiz : R.color.study_border)));
    }
    root.findViewById(R.id.studyTopics).setVisibility(showHistory ? View.GONE : View.VISIBLE);
    root.findViewById(R.id.studyDetail).setVisibility(showHistory ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.mixedActions).setVisibility(showHistory ? View.GONE : View.VISIBLE);
    if (showHistory) {
      LinearLayout content = root.findViewById(R.id.studyContent);
      content.removeAllViews();
      StudyViews.text(content, "Tentativi e risultati", 23);
      if (history.isEmpty())
        StudyViews.text(
            content, "Non hai ancora svolto quiz o ripassi. Torna alle materie per iniziare.", 17);
      for (AttemptSummary a : history) {
        String label =
            a.title
                + "\n"
                + (a.kind.equals("QUIZ") ? "Quiz" : "Autovalutazione")
                + " · "
                + a.answered
                + "/"
                + a.total
                + " risposte";
        label +=
            a.status.equals("COMPLETED")
                ? "\n"
                    + a.correct
                    + " corrette · "
                    + a.partial
                    + " parziali · "
                    + a.errors
                    + " errori · "
                    + a.skipped
                    + " saltate"
                : "\nRiprendi";
        StudyViews.button(
            content, label, () -> ((StudyActivity) requireActivity()).resumeAttempt(a.id));
      }
      ((TextView) root.findViewById(R.id.studyCount))
          .setText(history.size() + " tentativi salvati");
      return;
    }
    List<TopicRow> shown = new ArrayList<>();
    String normalized = query.toLowerCase(Locale.ROOT).trim();
    int quiz = 0, recall = 0;
    for (TopicRow t : topics)
      if ((filter.isEmpty() || filter.equals(t.subjectId))
          && (t.title + " " + t.category + " " + t.subjectTitle)
              .toLowerCase(Locale.ROOT)
              .contains(normalized)) {
        shown.add(t);
        quiz += t.quizCount;
        recall += t.recallCount;
      }
    adapter.submit(shown);
    int pending = 0;
    for (AttemptSummary a : history) if (!a.status.equals("COMPLETED")) pending++;
    ((TextView) root.findViewById(R.id.studyCount))
        .setText(
            shown.size()
                + " argomenti · "
                + quiz
                + " quiz · "
                + recall
                + " ripassi\n"
                + (pending > 0
                    ? pending + " tentativi da riprendere in Tentativi"
                    : "Teoria letta e comprensione sono misure distinte.")
                + (shown.isEmpty() ? "\nNessun argomento corrisponde alla ricerca." : ""));
  }

  private void renderMaterial(TheoryMaterial material) {
    LinearLayout content = root.findViewById(R.id.studyContent);
    content.removeAllViews();
    StudyViews.banner(content, getArguments().getString("title"), material.versionLabel);
    StudyViews.section(content, "Spiegazione", material.explanation, false);
    StudyViews.section(content, "Esempio", material.example, true);
    StudyViews.section(content, "Caso d’uso", material.useCase, false);
    StudyViews.section(content, "Errori comuni", material.commonErrors, false);
    LinearLayout actions = StudyViews.card(content, R.color.study_quiz);
    StudyViews.text(actions, "Metti alla prova ciò che hai letto", 21);
    StudyViews.button(actions, "Quiz dell’argomento", () -> model.start(topicId, "QUIZ", false, 0));
    StudyViews.button(actions, "Ripasso attivo", () -> model.start(topicId, "RECALL", false, 0));
    StudyViews.button(actions, "Ripassa errori quiz", () -> model.start(topicId, "QUIZ", true, 0));
    StudyViews.button(
        actions,
        "Ripassa autovalutazioni insufficienti",
        () -> model.start(topicId, "RECALL", true, 0));
    StudyViews.sources(content, material.sources);
    StudyViews.button(
        content,
        "Segna teoria come letta",
        () -> {
          model.read(topicId);
        });
  }

  public void reload() {
    if (topicId != null) model.material(topicId);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    out.putString("filter", filter);
    out.putString("query", query);
    out.putBoolean("history", showHistory);
  }

  @Override
  public void onDestroyView() {
    root = null;
    super.onDestroyView();
  }
}
