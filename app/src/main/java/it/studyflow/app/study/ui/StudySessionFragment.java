package it.studyflow.app.study.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import it.studyflow.app.*;
import it.studyflow.app.study.data.*;
import java.util.*;
import org.json.*;

public class StudySessionFragment extends Fragment {
  private StudyViewModel model;
  private View root;
  private String attemptId;
  private int position = -1;
  private boolean revealed;
  private StudyRepository.Session session;
  private AttemptItem current;
  private String restoredAnswer, restoredNote;
  private Integer restoredSelection;
  private boolean restoredDraft;

  public StudySessionFragment() {
    super(R.layout.fragment_study_session);
  }

  public static StudySessionFragment create(String id) {
    StudySessionFragment f = new StudySessionFragment();
    Bundle args = new Bundle();
    args.putString("attempt", id);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    root = view;
    attemptId = requireArguments().getString("attempt");
    model = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);
    if (state != null) {
      position = state.getInt("position", -1);
      revealed = state.getBoolean("revealed");
      restoredDraft = state.getBoolean("draft");
      restoredAnswer = state.getString("answer", "");
      restoredNote = state.getString("note", "");
      restoredSelection = state.containsKey("selected") ? state.getInt("selected") : null;
    }
    model.session.observe(
        getViewLifecycleOwner(),
        value -> {
          if (value != null && attemptId.equals(value.attempt.id)) {
            session = value;
            render();
          }
        });
    model.busy.observe(
        getViewLifecycleOwner(), busy -> setActionsEnabled(!Boolean.TRUE.equals(busy)));
    view.findViewById(R.id.sessionConfirm)
        .setOnClickListener(
            v -> {
              Integer selected = selected();
              if (selected == null) {
                Toast.makeText(
                        requireContext(),
                        "Seleziona una risposta oppure usa Salta",
                        Toast.LENGTH_LONG)
                    .show();
                return;
              }
              confirm(selected, "CORRECT");
            });
    view.findViewById(R.id.sessionReveal)
        .setOnClickListener(
            v -> {
              revealed = true;
              renderFeedback();
            });
    view.findViewById(R.id.ratingWrong).setOnClickListener(v -> confirm(null, "WRONG"));
    view.findViewById(R.id.ratingPartial).setOnClickListener(v -> confirm(null, "PARTIAL"));
    view.findViewById(R.id.ratingCorrect).setOnClickListener(v -> confirm(null, "CORRECT"));
    view.findViewById(R.id.sessionSkip).setOnClickListener(v -> confirm(null, "SKIPPED"));
    view.findViewById(R.id.sessionNext)
        .setOnClickListener(
            v -> {
              position++;
              revealed = false;
              restoredDraft = false;
              render();
            });
    reload();
  }

  public void reload() {
    if (model != null) model.load(attemptId);
  }

  private void render() {
    if (root == null || session == null) return;
    ((StudyActivity) requireActivity())
        .setStudyTitle(session.attempt.kind.equals("QUIZ") ? "Quiz" : "Ripasso attivo", true);
    if (session.attempt.status.equals("COMPLETED")) {
      current = null;
      renderResult();
      return;
    }
    if (position < 0 || position >= session.items.size()) {
      position = 0;
      while (position < session.items.size()
          && !session.items.get(position).outcome.equals("PENDING")) position++;
      if (position >= session.items.size()) position = 0;
    }
    current = session.items.get(position);
    boolean pending = current.outcome.equals("PENDING"), quiz = session.attempt.kind.equals("QUIZ");
    ((com.google.android.material.progressindicator.LinearProgressIndicator)
            root.findViewById(R.id.sessionProgress))
        .setProgressCompat((position + 1) * 100 / session.items.size(), false);
    ((TextView) root.findViewById(R.id.sessionPosition))
        .setText(
            session.attempt.title
                + "\nDomanda "
                + (position + 1)
                + " di "
                + session.items.size()
                + (quiz ? " · Correzione automatica" : " · Autovalutazione"));
    ((TextView) root.findViewById(R.id.sessionPrompt)).setText(current.prompt);
    RadioGroup options = root.findViewById(R.id.sessionOptions);
    options.removeAllViews();
    try {
      JSONArray labels = new JSONArray(current.options);
      for (int i = 0; i < labels.length(); i++) {
        RadioButton button = new RadioButton(requireContext());
        button.setId(View.generateViewId());
        button.setTag(i);
        button.setText(labels.getString(i));
        button.setTextSize(17);
        int padding = StudyViews.dp(requireContext(), 14);
        button.setPadding(padding, padding, padding, padding);
        button.setTextColor(StudyViews.color(requireContext(), R.color.study_text));
        button.setButtonTintList(
            android.content.res.ColorStateList.valueOf(
                StudyViews.color(requireContext(), R.color.study_quiz)));
        button.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
        button.setEnabled(pending);
        Integer choice = restoredDraft ? restoredSelection : current.selectedIndex;
        int fill = R.color.study_surface, border = R.color.study_border;
        if (!pending && i == current.correctIndex) {
          fill = R.color.study_spring_soft;
          border = R.color.study_spring;
        } else if (!pending && choice != null && choice == i) {
          fill = R.color.study_error_soft;
          border = R.color.study_error;
        }
        android.graphics.drawable.StateListDrawable background =
            new android.graphics.drawable.StateListDrawable();
        if (pending)
          background.addState(
              new int[] {android.R.attr.state_checked},
              StudyViews.rounded(requireContext(), R.color.study_selected, R.color.study_quiz));
        background.addState(new int[] {}, StudyViews.rounded(requireContext(), fill, border));
        button.setBackground(background);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(-1, -2);
        params.bottomMargin = StudyViews.dp(requireContext(), 10);
        options.addView(button, params);
        if (choice != null && choice == i) options.check(button.getId());
      }
    } catch (JSONException e) {
      model.error.setValue("Opzioni non valide: torna al catalogo.");
    }
    options.setVisibility(quiz ? View.VISIBLE : View.GONE);
    EditText answer = root.findViewById(R.id.sessionAnswer);
    answer.setVisibility(quiz ? View.GONE : View.VISIBLE);
    answer.setText(restoredDraft ? restoredAnswer : current.answer);
    answer.setEnabled(pending);
    EditText note = root.findViewById(R.id.sessionNote);
    note.setText(restoredDraft ? restoredNote : current.note);
    note.setEnabled(pending);
    restoredDraft = false;
    root.findViewById(R.id.sessionConfirm)
        .setVisibility(quiz && pending ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.sessionReveal)
        .setVisibility(!quiz && pending && !revealed ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.sessionSkip).setVisibility(pending ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.sessionNext).setVisibility(pending ? View.GONE : View.VISIBLE);
    root.findViewById(R.id.sessionResult).setVisibility(View.GONE);
    renderFeedback();
    setActionsEnabled(!Boolean.TRUE.equals(model.busy.getValue()));
    root.findViewById(R.id.sessionScroll)
        .post(
            () -> {
              if (root != null) root.findViewById(R.id.sessionScroll).scrollTo(0, 0);
            });
  }

  private void renderFeedback() {
    if (root == null || current == null) return;
    boolean pending = current.outcome.equals("PENDING"), quiz = session.attempt.kind.equals("QUIZ");
    TextView feedback = root.findViewById(R.id.sessionFeedback);
    boolean visible = !pending || revealed;
    feedback.setVisibility(visible ? View.VISIBLE : View.GONE);
    if (visible) {
      int fill =
          pending
              ? R.color.study_recall_soft
              : current.outcome.equals("CORRECT")
                  ? R.color.study_spring_soft
                  : current.outcome.equals("WRONG")
                      ? R.color.study_error_soft
                      : R.color.study_warning_soft;
      int border =
          pending
              ? R.color.study_recall
              : current.outcome.equals("CORRECT")
                  ? R.color.study_spring
                  : current.outcome.equals("WRONG") ? R.color.study_error : R.color.study_warning;
      feedback.setBackground(StudyViews.rounded(requireContext(), fill, border));
      String text = pending ? "Soluzione guidata" : StudyViews.outcome(current.outcome);
      if (quiz) {
        try {
          text +=
              "\nRisposta corretta: "
                  + new JSONArray(current.options).getString(current.correctIndex);
        } catch (JSONException ignored) {
          text += "\nOpzione non disponibile";
        }
      }
      text += "\n\n" + current.explanation;
      if (!quiz) text += "\n\nCriteri\n" + current.rubric;
      feedback.setText(text);
    }
    root.findViewById(R.id.sessionRatings)
        .setVisibility(!quiz && pending && revealed ? View.VISIBLE : View.GONE);
    root.findViewById(R.id.sessionReveal)
        .setVisibility(!quiz && pending && !revealed ? View.VISIBLE : View.GONE);
  }

  private Integer selected() {
    RadioGroup group = root.findViewById(R.id.sessionOptions);
    View view = group.findViewById(group.getCheckedRadioButtonId());
    return view == null ? null : (Integer) view.getTag();
  }

  private String text(int id) {
    return ((EditText) root.findViewById(id)).getText().toString();
  }

  private void confirm(Integer selected, String rating) {
    if (current == null) return;
    model.answer(current, selected, text(R.id.sessionAnswer), text(R.id.sessionNote), rating);
  }

  private void setActionsEnabled(boolean enabled) {
    if (root == null) return;
    for (int id :
        new int[] {
          R.id.sessionConfirm,
          R.id.sessionReveal,
          R.id.sessionSkip,
          R.id.sessionNext,
          R.id.ratingWrong,
          R.id.ratingPartial,
          R.id.ratingCorrect
        }) root.findViewById(id).setEnabled(enabled);
    boolean editable = enabled && current != null && current.outcome.equals("PENDING");
    root.findViewById(R.id.sessionAnswer).setEnabled(editable);
    root.findViewById(R.id.sessionNote).setEnabled(editable);
    RadioGroup group = root.findViewById(R.id.sessionOptions);
    for (int i = 0; i < group.getChildCount(); i++) group.getChildAt(i).setEnabled(editable);
  }

  private void saveDraft() {
    if (root != null && current != null && current.outcome.equals("PENDING"))
      model.draft(current, text(R.id.sessionAnswer), text(R.id.sessionNote), selected());
  }

  private void renderResult() {
    ((com.google.android.material.progressindicator.LinearProgressIndicator)
            root.findViewById(R.id.sessionProgress))
        .setProgressCompat(100, false);
    for (int id :
        new int[] {
          R.id.sessionOptions,
          R.id.sessionAnswer,
          R.id.sessionNote,
          R.id.sessionConfirm,
          R.id.sessionReveal,
          R.id.sessionFeedback,
          R.id.sessionRatings,
          R.id.sessionSkip,
          R.id.sessionNext
        }) root.findViewById(id).setVisibility(View.GONE);
    int correct = 0, partial = 0, wrong = 0, skipped = 0;
    for (AttemptItem i : session.items) {
      if (i.outcome.equals("CORRECT")) correct++;
      else if (i.outcome.equals("PARTIAL")) partial++;
      else if (i.outcome.equals("WRONG")) wrong++;
      else if (i.outcome.equals("SKIPPED")) skipped++;
    }
    ((TextView) root.findViewById(R.id.sessionPosition))
        .setText(session.attempt.title + " · Tentativo salvato");
    boolean quiz = session.attempt.kind.equals("QUIZ");
    ((TextView) root.findViewById(R.id.sessionPrompt))
        .setText(
            quiz
                ? correct
                    + " / "
                    + session.items.size()
                    + " corrette ("
                    + (correct * 100 / session.items.size())
                    + "%)"
                : "Autovalutazione completata");
    LinearLayout result = root.findViewById(R.id.sessionResult);
    result.setVisibility(View.VISIBLE);
    result.removeAllViews();
    StudyViews.text(
        result,
        correct
            + " corrette · "
            + partial
            + " parziali · "
            + wrong
            + " da ripassare · "
            + skipped
            + " saltate",
        17);
    if (!quiz)
      StudyViews.text(
          result,
          "Questi esiti sono autovalutazioni, non una correzione automatica della risposta libera.",
          16);
    StudyViews.button(result, "Nuovo tentativo", () -> replaceWithNew(false));
    StudyViews.button(result, "Ripassa gli errori", () -> replaceWithNew(true));
    StudyViews.button(
        result,
        "Torna allo studio",
        () -> requireActivity().getSupportFragmentManager().popBackStack());
    for (AttemptItem item : session.items) {
      StudyViews.section(
          result, StudyViews.outcome(item.outcome) + " · " + item.prompt, item.explanation, false);
      if (!item.answer.trim().isEmpty())
        StudyViews.section(result, "La tua risposta", item.answer, false);
      if (quiz) {
        try {
          JSONArray options = new JSONArray(item.options);
          StudyViews.text(
              result,
              "Scelta: "
                  + (item.selectedIndex == null ? "saltata" : options.getString(item.selectedIndex))
                  + "\nCorretta: "
                  + options.getString(item.correctIndex),
              16);
        } catch (JSONException ignored) {
        }
      } else StudyViews.section(result, "Criteri", item.rubric, false);
      if (!item.note.trim().isEmpty()) StudyViews.section(result, "Nota", item.note, false);
    }
  }

  private void replaceWithNew(boolean errors) {
    model.start(session.attempt.topicId, session.attempt.kind, errors, session.items.size());
  }

  @Override
  public void onPause() {
    saveDraft();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    out.putInt("position", position);
    out.putBoolean("revealed", revealed);
    if (root != null && current != null && current.outcome.equals("PENDING")) {
      out.putBoolean("draft", true);
      out.putString("answer", text(R.id.sessionAnswer));
      out.putString("note", text(R.id.sessionNote));
      Integer choice = selected();
      if (choice != null) out.putInt("selected", choice);
    }
  }

  @Override
  public void onDestroyView() {
    root = null;
    super.onDestroyView();
  }
}
