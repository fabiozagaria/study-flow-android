package it.studyflow.app;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
public class TaskReminderWorker extends Worker {
    public TaskReminderWorker(@NonNull Context context,@NonNull WorkerParameters params) { super(context,params); }
    public static void schedule(Context context,Task task) {
        WorkManager work=WorkManager.getInstance(context); String name="deadline_"+task.id;
        if (task.done || task.reminderSentFor.equals(DeadlineRules.key(task.due,task.dueTime))) { work.cancelUniqueWork(name); return; }
        long delay=DeadlineRules.delay(task.due,task.dueTime,ZoneId.systemDefault(),System.currentTimeMillis());
        Data data=new Data.Builder().putLong("taskId",task.id).putString("due",DeadlineRules.key(task.due,task.dueTime)).build();
        work.enqueueUniqueWork(name,ExistingWorkPolicy.REPLACE,new OneTimeWorkRequest.Builder(TaskReminderWorker.class).setInputData(data).setInitialDelay(delay,TimeUnit.MILLISECONDS).build());
    }
    public static void cancel(Context c,long id) { WorkManager.getInstance(c).cancelUniqueWork("deadline_"+id); c.getSystemService(android.app.NotificationManager.class).cancel("task_due_"+id,11); }
    @NonNull @Override public Result doWork() {
        StudyDao dao=StudyDatabase.get(getApplicationContext()).dao();
        long id=getInputData().getLong("taskId",0); Task task=dao.task(id);
        if (task==null || task.done) return Result.success();
        String due=DeadlineRules.key(task.due,task.dueTime);
        if (!due.equals(getInputData().getString("due")) || due.equals(task.reminderSentFor)) return Result.success();
        if (DeadlineRules.delay(task.due,task.dueTime,ZoneId.systemDefault(),System.currentTimeMillis())>0) { schedule(getApplicationContext(),task); return Result.success(); }
        if (!Notifications.allowed(getApplicationContext())) return Result.success();
        Notifications.post(getApplicationContext(),"task_due_"+task.id,11,Notifications.builder(getApplicationContext(),Notifications.TASKS).setContentTitle("Attività in scadenza").setContentText(task.title+" · "+task.subject).setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(task.title+"\n"+task.subject+" · Scadenza "+task.due+" alle "+task.dueTime)).build());
        dao.markReminder(task.id,due); return Result.success();
    }
}
