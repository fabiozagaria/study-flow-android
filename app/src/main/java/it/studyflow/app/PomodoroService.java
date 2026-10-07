package it.studyflow.app;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import androidx.core.app.*;
import androidx.core.content.ContextCompat;
import java.util.concurrent.*;
public class PomodoroService extends Service {
    public static final String START="start", PAUSE="pause", RESET="reset";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private PowerManager.WakeLock lock;
    private final Runnable tick=new Runnable() { @Override public void run() { if (finish()) return; handler.postDelayed(this,500); } };
    public static void command(Context context,String action) {
        Intent intent=new Intent(context,PomodoroService.class).setAction(action);
        ContextCompat.startForegroundService(context,intent);
    }
    @Override public void onCreate() {
        super.onCreate(); Notifications.channels(this);
        lock=getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"StudyFlow:Pomodoro"); lock.setReferenceCounted(false);
    }
    private Notification ongoing(TimerState state) {
        PendingIntent pause=PendingIntent.getService(this,3,new Intent(this,PomodoroService.class).setAction(PAUSE),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent reset=PendingIntent.getService(this,4,new Intent(this,PomodoroService.class).setAction(RESET),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return Notifications.builder(this,Notifications.TIMER).setContentTitle(state.breakPhase ? "Pausa in corso" : "Pomodoro in corso")
            .setContentText("Il timer continua anche con lo schermo spento")
            .setContentIntent(Notifications.open(this,true)).setOngoing(true).setAutoCancel(false).setOnlyAlertOnce(true)
            .setWhen(System.currentTimeMillis()+TimerStore.remaining(this,state)).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
            .addAction(0,"Pausa",pause).addAction(0,"Reimposta",reset).build();
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        TimerState state=TimerStore.read(this);
        ServiceCompat.startForeground(this,Notifications.RUNNING_ID,ongoing(state),Build.VERSION.SDK_INT>=34 ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0);
        handler.removeCallbacks(tick);
        String action=intent==null ? "restore" : intent.getAction();
        // Settle an expired phase before handling pause/reset; start never skips a phase.
        if (state.running && TimerStore.remaining(this,state)==0) { finish(); return START_NOT_STICKY; }
        if (RESET.equals(action)) { state.reset(); TimerStore.write(this,state); stopTimer(); return START_NOT_STICKY; }
        if (PAUSE.equals(action)) { state.pause(SystemClock.elapsedRealtime(),System.currentTimeMillis(),TimerStore.boot(this)); TimerStore.write(this,state); stopTimer(); return START_NOT_STICKY; }
        if (START.equals(action) && !state.running) { state.start(SystemClock.elapsedRealtime(),System.currentTimeMillis(),TimerStore.boot(this)); TimerStore.write(this,state); }
        if (!state.running) { stopTimer(); return START_NOT_STICKY; }
        if (state.elapsedDeadline==0 || state.boot!=TimerStore.boot(this)) {
            state.remaining=TimerStore.remaining(this,state);state.start(SystemClock.elapsedRealtime(),System.currentTimeMillis(),TimerStore.boot(this));TimerStore.write(this,state);
        }
        ServiceCompat.startForeground(this,Notifications.RUNNING_ID,ongoing(state),Build.VERSION.SDK_INT>=34 ? ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE : 0);
        if (lock.isHeld()) lock.release(); lock.acquire(TimerStore.remaining(this,state)+30_000L);
        TimerAlarmReceiver.schedule(this,state);
        handler.post(tick); return START_STICKY;
    }
    private boolean finish() {
        Boolean study=TimerStore.finishIfDue(this);
        if (study!=null) { worker.execute(() -> TimerStore.recordPending(getApplicationContext())); Notifications.timerDone(this,study); stopTimer(); return true; }
        if (!TimerStore.read(this).running) { stopTimer(); return true; } return false;
    }
    private void stopTimer() { TimerAlarmReceiver.cancel(this); handler.removeCallbacks(tick); if(lock!=null && lock.isHeld()) lock.release(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); }
    @Override public void onDestroy() { handler.removeCallbacks(tick); if(lock!=null && lock.isHeld()) lock.release(); worker.shutdown(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
