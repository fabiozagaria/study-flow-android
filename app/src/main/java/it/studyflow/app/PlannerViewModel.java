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
        super(application); dao = StudyDatabase.get(application).dao(); tasks = dao.tasks(); minutes = dao.minutes(); executor.execute(() -> TimerStore.recordPending(application));
    }
    public void save(Task task) { executor.execute(() -> {
        Task old=task.id==0 ? null : dao.task(task.id);
        if (old!=null) task.reminderSentFor=old.reminderSentFor;
        if (task.id==0) task.id=dao.insert(task); else dao.update(task);
        TaskReminderWorker.schedule(getApplication(),task);
        if (task.done && (old==null || !old.done)) { TaskReminderWorker.cancel(getApplication(),task.id); Notifications.completed(getApplication(),task); }
    }); }
    public void restoreReminders() { executor.execute(() -> { for(Task task:dao.pendingTasks()) TaskReminderWorker.schedule(getApplication(),task); }); }
    public void delete(Task task) { executor.execute(() -> { dao.delete(task); TaskReminderWorker.cancel(getApplication(),task.id); }); }
    public void record(long id) { executor.execute(() -> { StudySession s = new StudySession(); s.id = id; dao.session(s); }); }
    @Override protected void onCleared() { executor.shutdown(); }
}
