package it.studyflow.app;
import android.content.*;
import android.os.SystemClock;
import android.provider.Settings;
public final class TimerStore {
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("pomodoro",Context.MODE_PRIVATE); }
    public static int boot(Context c) { return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1); }
    public static synchronized TimerState read(Context c) {
        SharedPreferences p=prefs(c); TimerState s=new TimerState(); s.remaining=p.getLong("remaining",TimerState.STUDY); s.running=p.getBoolean("running",false); s.breakPhase=p.getBoolean("pause",false);
        s.wallDeadline=p.getLong("deadline",0); s.elapsedDeadline=p.getLong("elapsedDeadline",0); s.boot=p.getInt("boot",-2); s.sessionId=p.getLong("sessionId",s.running ? s.wallDeadline : 0);
        return s;
    }
    public static synchronized void write(Context c,TimerState s) {
        prefs(c).edit().putLong("remaining",s.remaining).putLong("deadline",s.wallDeadline).putLong("elapsedDeadline",s.elapsedDeadline).putInt("boot",s.boot).putLong("sessionId",s.sessionId).putBoolean("running",s.running).putBoolean("pause",s.breakPhase).commit();
    }
    public static long remaining(Context c,TimerState s) { return s.remainingAt(SystemClock.elapsedRealtime(),System.currentTimeMillis(),boot(c)); }
    public static synchronized Boolean finishIfDue(Context c) {
        TimerState state=read(c); if (!state.running || remaining(c,state)>0) return null;
        boolean study=!state.breakPhase;
        if (study) prefs(c).edit().putLong("pendingSession",state.sessionId).commit();
        state.nextPhase(); write(c,state); return study;
    }
    public static synchronized void recordPending(Context c) {
        long id=prefs(c).getLong("pendingSession",0); if (id==0) return;
        StudySession session=new StudySession();session.id=id; StudyDatabase.get(c).dao().session(session);
        prefs(c).edit().remove("pendingSession").commit();
    }
    private TimerStore() {}
}
