package it.studyflow.app;
import androidx.annotation.NonNull;
import androidx.room.*;
@Entity(tableName = "calendar_events", indices = {@Index(value = "sourceKey", unique = true)})
public class CalendarEvent {
    @PrimaryKey(autoGenerate = true) public long id;
    @NonNull public String title = "";
    @NonNull public String date = "";
    @NonNull public String time = "";
    @NonNull public String notes = "";
    @NonNull public String sourceKey = "";
}
