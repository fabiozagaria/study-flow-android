package it.studyflow.app;
import android.content.*;
import java.util.concurrent.*;
public class ReminderRestoreReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent) {
        PendingResult pending=goAsync(); ExecutorService executor=Executors.newSingleThreadExecutor();
        executor.execute(() -> { try {
            for(Task task:StudyDatabase.get(context).dao().pendingTasks()) TaskReminderWorker.schedule(context,task);
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Boolean study=TimerStore.finishIfDue(context);
                if(study!=null) Notifications.timerDone(context,study);
                else {
                    TimerState state=TimerStore.read(context);
                    if (state.running) { state.pause(android.os.SystemClock.elapsedRealtime(),System.currentTimeMillis(),TimerStore.boot(context)); TimerStore.write(context,state);
                        Notifications.post(context,"timer_reboot",3,Notifications.builder(context,Notifications.FINISHED).setContentTitle("Timer sospeso dopo il riavvio").setContentText("Apri StudyFlow per riprendere la sessione.").setContentIntent(Notifications.open(context,true)).build()); }
                }
            }
            TimerStore.recordPending(context);
        } finally { pending.finish(); executor.shutdown(); } });
    }
}
