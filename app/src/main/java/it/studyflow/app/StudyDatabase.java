package it.studyflow.app;
import android.content.Context;
import androidx.room.*;
@Database(entities = {Task.class, StudySession.class}, version = 1, exportSchema = false)
public abstract class StudyDatabase extends RoomDatabase {
    public abstract StudyDao dao();
    private static volatile StudyDatabase instance;
    public static StudyDatabase get(Context context) {
        if (instance == null) synchronized (StudyDatabase.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), StudyDatabase.class, "studyflow.db").build();
        }
        return instance;
    }
}
