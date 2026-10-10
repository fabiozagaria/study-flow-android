package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(tableName = "study_attempts", indices = @Index("topicId"))
public class StudyAttempt {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String topicId = "";
  @NonNull public String title = "";
  @NonNull public String kind = "QUIZ";
  @NonNull public String status = "IN_PROGRESS";
  public long startedAt;
  public Long finishedAt;
}
