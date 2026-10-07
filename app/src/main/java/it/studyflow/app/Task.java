package it.studyflow.app;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true) public long id;
    public String title = "";
    public String subject = "";
    public String due = "";
    public int priority;
    public boolean done;
    @androidx.annotation.NonNull @androidx.room.ColumnInfo(defaultValue = "'09:00'") public String dueTime = "09:00";
    @androidx.annotation.NonNull @androidx.room.ColumnInfo(defaultValue = "''") public String reminderSentFor = "";
}
