package it.studyflow.app.study.ui;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import it.studyflow.app.StudyActivity;
import it.studyflow.app.study.ui.compose.StudySessionSurface;

/** Existing Fragment navigation with a Compose-only presentation surface. */
public class StudySessionFragment extends Fragment {
  private StudyViewModel model;
  private StudySessionSurface surface;
  private String attemptId;
  private Bundle presentationState;

  public static StudySessionFragment create(String id) {
    StudySessionFragment fragment = new StudySessionFragment();
    Bundle args = new Bundle();
    args.putString("attempt", id);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
    attemptId = requireArguments().getString("attempt");
    model = new ViewModelProvider(requireActivity()).get(StudyViewModel.class);
    surface =
        new StudySessionSurface(
            model,
            state != null ? state : presentationState,
            () -> requireActivity().getSupportFragmentManager().popBackStack(),
            () -> {
              if (surface.getSession() == null) return;
              String topic = surface.getSession().attempt.topicId;
              StudyBrowserFragment theory =
                  StudyBrowserFragment.topic(topic, surface.getSession().attempt.title);
              theory.requireArguments().putBoolean("fromSession", true);
              requireActivity()
                  .getSupportFragmentManager()
                  .beginTransaction()
                  .replace(it.studyflow.app.R.id.studyHost, theory)
                  .addToBackStack("theory")
                  .commit();
            });
    return surface.createView(requireContext());
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    model.session.observe(
        getViewLifecycleOwner(),
        value -> {
          if (value != null && attemptId.equals(value.attempt.id) && surface != null) {
            surface.update(value);
            ((StudyActivity) requireActivity())
                .setStudyTitle(value.attempt.kind.equals("QUIZ") ? "Quiz" : "Ripasso attivo", true);
          }
        });
    reload();
  }

  public void reload() {
    if (model != null) model.load(attemptId);
  }

  @Override
  public void onPause() {
    if (surface != null) surface.saveDraft();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle out) {
    super.onSaveInstanceState(out);
    if (surface != null) surface.save(out);
  }

  @Override
  public void onDestroyView() {
    if (surface != null) {
      presentationState = new Bundle();
      surface.save(presentationState);
    }
    surface = null;
    super.onDestroyView();
  }
}
