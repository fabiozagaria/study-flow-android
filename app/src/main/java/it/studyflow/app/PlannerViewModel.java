package it.studyflow.app;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
public class PlannerViewModel extends AndroidViewModel {
    private final StudyDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public final LiveData<List<Task>> tasks;
    public final LiveData<Integer> minutes;
    public PlannerViewModel(@NonNull Application application) {
        super(application); dao = StudyDatabase.get(application).dao(); tasks = dao.tasks(); minutes = dao.minutes();
    }
    public void save(Task task) { executor.execute(() -> { if (task.id == 0) dao.insert(task); else dao.update(task); }); }
    public void delete(Task task) { executor.execute(() -> dao.delete(task)); }
    public void record(long id) { executor.execute(() -> { StudySession s = new StudySession(); s.id = id; dao.session(s); }); }
    @Override protected void onCleared() { executor.shutdown(); }
}
