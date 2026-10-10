package it.studyflow.app.study.data;

import static org.junit.Assert.*;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.studyflow.app.StudyDatabase;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StudyPersistenceTest {
  private StudyDatabase db;
  private LearningDao dao;

  @Before
  public void setup() throws Exception {
    Context c = ApplicationProvider.getApplicationContext();
    db = Room.inMemoryDatabaseBuilder(c, StudyDatabase.class).allowMainThreadQueries().build();
    CatalogImporter.seed(c, db);
    dao = db.learning();
  }

  @After
  public void close() {
    db.close();
  }

  @Test
  public void seedIsAtomicAndIdempotent() throws Exception {
    assertEquals(67, dao.materialCount());
    CatalogImporter.seed(ApplicationProvider.getApplicationContext(), db);
    assertEquals(67, dao.materialCount());
    assertEquals(124, dao.questionsFor("java", "QUIZ").size());
    assertEquals(144, dao.questionsFor("spring", "QUIZ").size());
  }

  @Test
  public void theoryEditionPreservesProgressQuestionsAndAttemptSnapshots() throws Exception {
    TheoryMaterial material = dao.material("java-threads");
    material.revision = 1;
    material.explanation = "Old theory";
    dao.updateTheory(material);
    MaterialProgress progress = new MaterialProgress();
    progress.materialId = material.id;
    progress.readAt = 123L;
    dao.markRead(progress);
    StudyAttempt attempt = new StudyAttempt();
    attempt.id = "edition-attempt";
    attempt.topicId = material.topicId;
    dao.insertAttempt(attempt);
    AttemptItem item = new AttemptItem();
    item.id = "edition-item";
    item.attemptId = attempt.id;
    item.questionId = "java-threads-q1";
    item.prompt = "Original snapshot";
    item.explanation = "Saved feedback";
    dao.insertItems(Collections.singletonList(item));
    dao.confirm(item.id, 0, "", "Saved note", "WRONG", 124);
    String questionText = dao.questionsFor(material.topicId, "QUIZ").get(0).explanation;
    CatalogImporter.seed(ApplicationProvider.getApplicationContext(), db);
    assertEquals(2, dao.material(material.topicId).revision);
    assertTrue(dao.material(material.topicId).explanation.contains("Costruiamo il concetto"));
    assertEquals(questionText, dao.questionsFor(material.topicId, "QUIZ").get(0).explanation);
    assertEquals("Original snapshot", dao.item(item.id).prompt);
    assertEquals("Saved feedback", dao.item(item.id).explanation);
    assertEquals("Saved note", dao.item(item.id).note);
    assertEquals("WRONG", dao.item(item.id).outcome);
    try (android.database.Cursor cursor =
        db.getOpenHelper()
            .getReadableDatabase()
            .query(
                "SELECT readAt FROM study_material_progress WHERE"
                    + " materialId='java-threads-theory'")) {
      assertTrue(cursor.moveToFirst());
      assertEquals(123L, cursor.getLong(0));
    }
  }

  @Test
  public void answersAreIdempotentAndCompletionIsConditional() {
    StudyAttempt a = new StudyAttempt();
    a.id = "attempt";
    a.topicId = "java-types";
    dao.insertAttempt(a);
    AttemptItem i = new AttemptItem();
    i.id = "item";
    i.attemptId = a.id;
    i.questionId = "java-types-q1";
    dao.insertItems(Collections.singletonList(i));
    dao.finish(a.id, 10);
    assertEquals("IN_PROGRESS", dao.attempt(a.id).status);
    assertEquals(1, dao.confirm(i.id, 0, "", "error", "WRONG", 20));
    assertEquals(0, dao.confirm(i.id, 1, "", "", "CORRECT", 21));
    dao.finish(a.id, 22);
    assertEquals("COMPLETED", dao.attempt(a.id).status);
    assertEquals("WRONG", dao.item(i.id).outcome);
    assertEquals(1, dao.reviewQuestions("java-types", "QUIZ").size());
  }

  @Test
  public void aLaterCorrectAnswerClearsReviewWithoutDeletingOldError() {
    StudyAttempt a = new StudyAttempt();
    a.id = "a";
    a.topicId = "java-types";
    dao.insertAttempt(a);
    AttemptItem first = new AttemptItem();
    first.id = "first";
    first.attemptId = a.id;
    first.questionId = "java-types-q1";
    dao.insertItems(Collections.singletonList(first));
    dao.confirm(first.id, 0, "", "", "WRONG", 100);
    StudyAttempt b = new StudyAttempt();
    b.id = "b";
    b.topicId = a.topicId;
    dao.insertAttempt(b);
    AttemptItem second = new AttemptItem();
    second.id = "second";
    second.attemptId = b.id;
    second.questionId = first.questionId;
    dao.insertItems(Collections.singletonList(second));
    dao.confirm(second.id, 1, "", "", "CORRECT", 101);
    assertEquals(0, dao.reviewQuestions("java-types", "QUIZ").size());
    assertEquals("WRONG", dao.item(first.id).outcome);
  }
}
