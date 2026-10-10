package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_materials",
    foreignKeys =
        @ForeignKey(
            entity = Topic.class,
            parentColumns = "id",
            childColumns = "topicId",
            onDelete = ForeignKey.RESTRICT),
    indices = @Index(value = "topicId", unique = true))
public class TheoryMaterial {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String topicId = "";
  @NonNull public String explanation = "";
  @NonNull public String example = "";
  @NonNull public String useCase = "";
  @NonNull public String commonErrors = "";
  @NonNull public String sources = "[]";
  @NonNull public String versionLabel = "";
  public int revision;
}
