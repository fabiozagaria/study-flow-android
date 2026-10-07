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
}
