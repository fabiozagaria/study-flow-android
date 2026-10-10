package it.studyflow.app.study.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import it.studyflow.app.StudyDatabase;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import org.json.*;

public final class StudyRepository implements AutoCloseable {
  private final StudyDatabase db;
  private final LearningDao dao;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public StudyRepository(Context context) {
    db = StudyDatabase.get(context);
    dao = db.learning();
  }

  public LiveData<List<TopicRow>> catalog() {
    return dao.catalog();
  }

  public LiveData<List<AttemptSummary>> history() {
    return dao.history();
  }

  public void seed(Context c, Runnable success, Consumer<String> error) {
    execute(
        () -> {
          CatalogImporter.seed(c, db);
          success.run();
        },
        error);
  }

  public void material(String topic, Consumer<TheoryMaterial> success, Consumer<String> error) {
    execute(() -> success.accept(dao.material(topic)), error);
  }

  public void read(String topic, Runnable success, Consumer<String> error) {
    execute(
        () -> {
          TheoryMaterial m = dao.material(topic);
          if (m == null) throw new IllegalStateException();
          MaterialProgress p = new MaterialProgress();
          p.materialId = m.id;
          p.readAt = System.currentTimeMillis();
          dao.markRead(p);
          success.run();
        },
        error);
  }

  public void create(
      String topicId,
      String kind,
      boolean errorsOnly,
      int limit,
      Consumer<String> success,
      Consumer<String> error) {
    execute(
        () -> {
          if (!kind.equals("QUIZ") && !kind.equals("RECALL")) throw new IllegalArgumentException();
          Topic topic = dao.topic(topicId);
          String title =
              topic != null
                  ? topic.title
                  : topicId.equals("java")
                      ? "Java · quiz misto"
                      : topicId.equals("spring") ? "Spring Boot · quiz misto" : null;
          if (title == null) throw new IllegalArgumentException();
          List<Question> questions =
              errorsOnly ? dao.reviewQuestions(topicId, kind) : dao.questionsFor(topicId, kind);
          if (questions.isEmpty()) {
            error.accept("Nessuna domanda disponibile per questa modalità.");
            return;
          }
          Collections.shuffle(questions);
          if (limit > 0 && questions.size() > limit)
            questions = new ArrayList<>(questions.subList(0, limit));
          StudyAttempt a = new StudyAttempt();
          a.id = UUID.randomUUID().toString();
          a.topicId = topicId;
          a.title = title;
          a.kind = kind;
          a.startedAt = System.currentTimeMillis();
          List<AttemptItem> items = new ArrayList<>();
          int position = 0;
          for (Question q : questions) {
            AttemptItem item = new AttemptItem();
            item.id = UUID.randomUUID().toString();
            item.attemptId = a.id;
            item.questionId = q.id;
            item.position = position++;
            item.prompt = q.prompt;
            item.explanation = q.explanation;
            item.rubric = q.rubric;
            if (kind.equals("QUIZ")) {
              List<AnswerOption> options = dao.optionsFor(q.id);
              Collections.shuffle(options);
              JSONArray texts = new JSONArray();
              for (int j = 0; j < options.size(); j++) {
                texts.put(options.get(j).text);
                if (options.get(j).correct) item.correctIndex = j;
              }
              item.options = texts.toString();
              QuizRules.evaluate(null, item.correctIndex, options.size());
            }
            items.add(item);
          }
          db.runInTransaction(
              () -> {
                dao.insertAttempt(a);
                dao.insertItems(items);
              });
          success.accept(a.id);
        },
        error);
  }

  public static final class Session {
    public final StudyAttempt attempt;
    public final List<AttemptItem> items;

    Session(StudyAttempt a, List<AttemptItem> i) {
      attempt = a;
      items = i;
    }
  }

  public void load(String id, Consumer<Session> success, Consumer<String> error) {
    execute(
        () -> {
          StudyAttempt a = dao.attempt(id);
          if (a == null) throw new IllegalArgumentException();
          success.accept(new Session(a, dao.items(id)));
        },
        error);
  }

  public void answer(
      String id,
      Integer selected,
      String answer,
      String note,
      String rating,
      Runnable success,
      Consumer<String> error) {
    execute(
        () -> {
          AttemptItem item = dao.item(id);
          if (item == null) throw new IllegalArgumentException();
          StudyAttempt attempt = dao.attempt(item.attemptId);
          String outcome =
              attempt.kind.equals("QUIZ")
                  ? QuizRules.evaluate(
                      selected, item.correctIndex, new JSONArray(item.options).length())
                  : rating;
          if (attempt.kind.equals("RECALL") && !QuizRules.recallOutcome(outcome))
            throw new IllegalArgumentException();
          db.runInTransaction(
              () -> {
                dao.confirm(id, selected, answer, note, outcome, System.currentTimeMillis());
                dao.finish(item.attemptId, System.currentTimeMillis());
              });
          success.run();
        },
        error);
  }

  public void draft(
      String id, String answer, String note, Integer selected, Consumer<String> error) {
    execute(() -> dao.draft(id, answer, note, selected), error);
  }

  private interface Job {
    void run() throws Exception;
  }

  private void execute(Job job, Consumer<String> error) {
    executor.execute(
        () -> {
          try {
            job.run();
          } catch (Exception e) {
            android.util.Log.e("StudyFlow", "Study operation failed", e);
            error.accept("Operazione non riuscita. I dati confermati restano salvati. Riprova.");
          }
        });
  }

  @Override
  public void close() {
    executor.shutdown();
  }
}
