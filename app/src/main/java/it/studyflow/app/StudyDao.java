package it.studyflow.app;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface StudyDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, due ASC, priority DESC") LiveData<List<Task>> tasks();
    @Insert long insert(Task task);
    @Query("SELECT * FROM tasks WHERE id = :id") Task task(long id);
    @Query("SELECT * FROM tasks WHERE done = 0") List<Task> pendingTasks();
    @Query("UPDATE tasks SET reminderSentFor = :due WHERE id = :id AND due || 'T' || dueTime = :due") void markReminder(long id, String due);
    @Update void update(Task task);
    @Delete void delete(Task task);
    @Insert(onConflict = OnConflictStrategy.IGNORE) void session(StudySession session);
    @Query("SELECT COALESCE(SUM(minutes), 0) FROM sessions") LiveData<Integer> minutes();
    @Query("SELECT * FROM calendar_events ORDER BY date, time, title") LiveData<List<CalendarEvent>> events();
    @Insert(onConflict = OnConflictStrategy.IGNORE) long insertEvent(CalendarEvent event);
    @Update void updateEvent(CalendarEvent event);
    @Delete void deleteEvent(CalendarEvent event);
    @Query("SELECT sourceKey FROM calendar_events") List<String> eventSources();
}
