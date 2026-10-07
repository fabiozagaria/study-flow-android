package it.studyflow.app;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "sessions")
public class StudySession {
    @PrimaryKey public long id;
    public int minutes = 25;
}
