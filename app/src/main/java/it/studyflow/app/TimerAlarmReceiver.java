package it.studyflow.app;
import android.app.*;
import android.content.*;
import android.os.SystemClock;
import java.util.concurrent.Executors;
public class TimerAlarmReceiver extends BroadcastReceiver {
    private static PendingIntent intent(Context c) { return PendingIntent.getBroadcast(c,5,new Intent(c,TimerAlarmReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); }
    public static void schedule(Context c,TimerState state) { c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+TimerStore.remaining(c,state),intent(c)); }
    public static void cancel(Context c) { c.getSystemService(AlarmManager.class).cancel(intent(c)); }
    @Override public void onReceive(Context c,Intent intent) {
        PendingResult pending=goAsync(); java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();
        executor.execute(() -> { try { Boolean study=TimerStore.finishIfDue(c); if(study!=null) { TimerStore.recordPending(c); Notifications.timerDone(c,study); } } finally { pending.finish(); executor.shutdown(); } });
    }
}
