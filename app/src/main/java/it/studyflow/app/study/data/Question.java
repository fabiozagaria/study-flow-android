package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_questions",
    foreignKeys =
        @ForeignKey(
            entity = Topic.class,
            parentColumns = "id",
            childColumns = "topicId",
            onDelete = ForeignKey.RESTRICT),
    indices = @Index("topicId"))
public class Question {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String topicId = "";
  @NonNull public String kind = "QUIZ";
  @NonNull public String prompt = "";
  @NonNull public String explanation = "";
  @NonNull public String rubric = "";
  public int position;
}
