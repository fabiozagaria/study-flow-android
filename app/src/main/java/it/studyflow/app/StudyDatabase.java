package it.studyflow.app;
import android.content.Context;
import androidx.room.*;
import it.studyflow.app.study.data.*;
@Database(entities = {Task.class, StudySession.class, CalendarEvent.class, Subject.class, Topic.class, TheoryMaterial.class, Question.class, AnswerOption.class, StudyAttempt.class, AttemptItem.class, MaterialProgress.class}, version = 4, exportSchema = true)
public abstract class StudyDatabase extends RoomDatabase {
    public abstract StudyDao dao();
    public abstract LearningDao learning();
    private static volatile StudyDatabase instance;
    public static StudyDatabase get(Context context) {
        if (instance == null) synchronized (StudyDatabase.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(), StudyDatabase.class, "studyflow.db").addMigrations(new androidx.room.migration.Migration(1, 2) {
                @Override public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS calendar_events (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, notes TEXT NOT NULL, sourceKey TEXT NOT NULL)");
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_calendar_events_sourceKey ON calendar_events (sourceKey)");
                }
            }, new androidx.room.migration.Migration(2, 3) {
                @Override public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN dueTime TEXT NOT NULL DEFAULT '09:00'");
                    db.execSQL("ALTER TABLE tasks ADD COLUMN reminderSentFor TEXT NOT NULL DEFAULT ''");
                }
            }, StudyMigrations.FROM_3_TO_4).build();
        }
        return instance;
    }
}
