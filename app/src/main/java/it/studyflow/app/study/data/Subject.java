package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(tableName = "study_subjects")
public class Subject {
  @PrimaryKey @NonNull public String id = "";
  @NonNull public String title = "";
  public int position;
}
