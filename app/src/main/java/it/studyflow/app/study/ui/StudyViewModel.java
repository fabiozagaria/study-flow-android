package it.studyflow.app.study.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import it.studyflow.app.study.data.*;
import java.util.List;

public class StudyViewModel extends AndroidViewModel {
  private final StudyRepository repository;
  public final LiveData<List<TopicRow>> catalog;
  public final LiveData<List<AttemptSummary>> history;
  public final MutableLiveData<TheoryMaterial> material = new MutableLiveData<>();
  public final MutableLiveData<StudyRepository.Session> session = new MutableLiveData<>();
  public final MutableLiveData<String> notice = new MutableLiveData<>();
  public final MutableLiveData<String> error = new MutableLiveData<>();
  public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
  public final MutableLiveData<String> createdAttempt = new MutableLiveData<>();

  public StudyViewModel(@NonNull Application app) {
    super(app);
    repository = new StudyRepository(app);
    catalog = repository.catalog();
    history = repository.history();
    seed();
  }

  private void failed(String message) {
    busy.postValue(false);
    error.postValue(message);
  }

  public void seed() {
    busy.setValue(true);
    repository.seed(getApplication(), () -> busy.postValue(false), this::failed);
  }

  public void material(String topic) {
    material.setValue(null);
    repository.material(topic, material::postValue, this::failed);
  }

  public void read(String topic) {
    repository.read(
        topic,
        () ->
            notice.postValue("Lettura registrata. La comprensione si verifica con quiz e ripasso."),
        this::failed);
  }

  public void start(String topic, String kind, boolean errors, int limit) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.create(
        topic,
        kind,
        errors,
        limit,
        id -> {
          createdAttempt.postValue(id);
          busy.postValue(false);
        },
        this::failed);
  }

  public void load(String id) {
    repository.load(id, session::postValue, this::failed);
  }

  public void answer(AttemptItem item, Integer selected, String text, String note, String rating) {
    if (Boolean.TRUE.equals(busy.getValue())) return;
    busy.setValue(true);
    repository.answer(
        item.id,
        selected,
        text,
        note,
        rating,
        () -> {
          repository.load(
              item.attemptId,
              s -> {
                session.postValue(s);
                busy.postValue(false);
              },
              this::failed);
        },
        this::failed);
  }

  public void draft(AttemptItem item, String text, String note, Integer selected) {
    repository.draft(item.id, text, note, selected, this::failed);
  }

  @Override
  protected void onCleared() {
    repository.close();
  }
}
