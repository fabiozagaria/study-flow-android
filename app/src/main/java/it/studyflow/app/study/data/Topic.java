package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_topics",
    foreignKeys =
        @ForeignKey(
            entity = Subject.class,
            parentColumns = "id",
            childColumns = "subjectId",
            onDelete = ForeignKey.RESTRICT),
    indices = @Index("subjectId"))
public class Topic {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String subjectId = "";
  @NonNull public String title = "";
  @NonNull public String category = "";
  public int position;
}
