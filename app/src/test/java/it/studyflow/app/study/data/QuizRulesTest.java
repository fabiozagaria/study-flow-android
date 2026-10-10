package it.studyflow.app.study.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class QuizRulesTest {
  @Test
  public void distinguishesCorrectWrongAndSkipped() {
    assertEquals("CORRECT", QuizRules.evaluate(2, 2, 3));
    assertEquals("WRONG", QuizRules.evaluate(0, 2, 3));
    assertEquals("SKIPPED", QuizRules.evaluate(null, 2, 3));
  }

  @Test
  public void rejectsInvalidIndices() {
    assertThrows(IllegalArgumentException.class, () -> QuizRules.evaluate(3, 0, 3));
    assertThrows(IllegalArgumentException.class, () -> QuizRules.evaluate(-1, 0, 3));
    assertThrows(IllegalArgumentException.class, () -> QuizRules.evaluate(null, -1, 3));
  }

  @Test
  public void rejectsInvalidQuestion() {
    assertThrows(IllegalArgumentException.class, () -> QuizRules.evaluate(0, 0, 1));
  }

  @Test
  public void doesNotTreatOpenAnswerAsAutomaticallyGraded() {
    assertTrue(QuizRules.recallOutcome("PARTIAL"));
    assertFalse(QuizRules.recallOutcome("PENDING"));
    assertFalse(QuizRules.recallOutcome("100%"));
  }
}
