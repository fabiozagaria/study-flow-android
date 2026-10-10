package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_attempt_items",
    foreignKeys =
        @ForeignKey(
            entity = StudyAttempt.class,
            parentColumns = "id",
            childColumns = "attemptId",
            onDelete = ForeignKey.CASCADE),
    indices = {
      @Index(
          value = {"attemptId", "position"},
          unique = true),
      @Index("questionId")
    })
public class AttemptItem {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String attemptId = "";
  @NonNull public String questionId = "";
  public int position;
  @NonNull public String prompt = "";
  @NonNull public String explanation = "";
  @NonNull public String rubric = "";
  @NonNull public String options = "[]";
  public int correctIndex = -1;
  public Integer selectedIndex;
  @NonNull public String answer = "";
  @NonNull public String note = "";
  @NonNull public String outcome = "PENDING";
  public Long answeredAt;
}
