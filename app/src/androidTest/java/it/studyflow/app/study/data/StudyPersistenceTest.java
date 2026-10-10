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
