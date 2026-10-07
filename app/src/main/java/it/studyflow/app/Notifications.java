package it.studyflow.app;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.*;
import androidx.core.content.ContextCompat;
public final class Notifications {
    public static final String TASKS="tasks", TIMER="timer_running", FINISHED="timer_finished";
    public static final int RUNNING_ID=1, FINISH_ID=2;
    public static void channels(Context c) {
        NotificationManager manager=c.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(TASKS,"Attività e scadenze",NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(new NotificationChannel(TIMER,"Pomodoro in corso",NotificationManager.IMPORTANCE_LOW));
        manager.createNotificationChannel(new NotificationChannel(FINISHED,"Pomodoro completato",NotificationManager.IMPORTANCE_HIGH));
    }
    public static PendingIntent open(Context c, boolean timer) {
        Intent intent=new Intent(c,MainActivity.class).putExtra("openTimer",timer).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c,timer ? 1 : 0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static NotificationCompat.Builder builder(Context c,String channel) {
        channels(c); return new NotificationCompat.Builder(c,channel).setSmallIcon(R.drawable.ic_notification).setColor(ContextCompat.getColor(c,R.color.sf_primary)).setContentIntent(open(c,false)).setAutoCancel(true);
    }
    public static boolean allowed(Context c) { return (Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(c,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) && NotificationManagerCompat.from(c).areNotificationsEnabled(); }
    public static void post(Context c,String tag,int id,Notification notification) {
        if (allowed(c)) { try { c.getSystemService(NotificationManager.class).notify(tag,id,notification); } catch (SecurityException ignored) {} }
    }
    public static void completed(Context c,Task task) {
        post(c,"task_done_"+task.id,10,builder(c,TASKS).setContentTitle("Attività completata ✓").setContentText(task.title+" · "+task.subject).build());
    }
    public static void timerDone(Context c,boolean study) {
        post(c,"timer_end",FINISH_ID,builder(c,FINISHED).setContentTitle(study ? "Pomodoro completato" : "Pausa terminata").setContentText(study ? "25 minuti di studio registrati. Avvia la pausa quando vuoi." : "Pronto per una nuova sessione di studio?").setContentIntent(open(c,true)).build());
    }
    private Notifications() {}
}
