package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_options",
    foreignKeys =
        @ForeignKey(
            entity = Question.class,
            parentColumns = "id",
            childColumns = "questionId",
            onDelete = ForeignKey.CASCADE),
    indices = @Index("questionId"))
public class AnswerOption {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String questionId = "";
  @NonNull public String text = "";
  public int position;
  public boolean correct;
}
