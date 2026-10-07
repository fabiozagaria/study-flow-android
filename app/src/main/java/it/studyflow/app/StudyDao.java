package it.studyflow.app;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface StudyDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, due ASC, priority DESC") LiveData<List<Task>> tasks();
    @Insert void insert(Task task);
    @Update void update(Task task);
    @Delete void delete(Task task);
    @Insert(onConflict = OnConflictStrategy.IGNORE) void session(StudySession session);
    @Query("SELECT COALESCE(SUM(minutes), 0) FROM sessions") LiveData<Integer> minutes();
}
