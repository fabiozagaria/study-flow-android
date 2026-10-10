package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "study_material_progress",
    foreignKeys =
        @ForeignKey(
            entity = TheoryMaterial.class,
            parentColumns = "id",
            childColumns = "materialId",
            onDelete = ForeignKey.CASCADE))
public class MaterialProgress {
  @PrimaryKey @NonNull public String materialId = "";
  public long readAt;
}
