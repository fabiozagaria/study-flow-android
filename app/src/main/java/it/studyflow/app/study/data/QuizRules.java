package it.studyflow.app.study.data;

/** Pure evaluation: a skipped question is distinct from an incorrect answer. */
public final class QuizRules {
  private QuizRules() {}

  public static String evaluate(Integer selected, int correct, int count) {
    if (count < 2 || correct < 0 || correct >= count)
      throw new IllegalArgumentException("Invalid question");
    if (selected == null) return "SKIPPED";
    if (selected < 0 || selected >= count) throw new IllegalArgumentException("Invalid option");
    return selected == correct ? "CORRECT" : "WRONG";
  }

  public static boolean recallOutcome(String outcome) {
    return "CORRECT".equals(outcome)
        || "PARTIAL".equals(outcome)
        || "WRONG".equals(outcome)
        || "SKIPPED".equals(outcome);
  }
}
