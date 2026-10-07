package it.studyflow.app;
public class TimerState {
    public static final long STUDY=25*60_000L, BREAK=5*60_000L;
    public long remaining=STUDY, elapsedDeadline, wallDeadline, sessionId;
    public boolean running, breakPhase;
    public int boot=-1;
    public long remainingAt(long elapsed,long wall,int currentBoot) {
        if (!running) return remaining;
        return Math.max(0,boot==currentBoot && elapsedDeadline>0 ? elapsedDeadline-elapsed : wallDeadline-wall);
    }
    public void start(long elapsed,long wall,int currentBoot) {
        elapsedDeadline=elapsed+remaining; wallDeadline=wall+remaining; boot=currentBoot; running=true;
        if (sessionId==0) sessionId=wall;
    }
    public void pause(long elapsed,long wall,int currentBoot) { remaining=remainingAt(elapsed,wall,currentBoot); running=false; }
    public void nextPhase() { breakPhase=!breakPhase; running=false; remaining=breakPhase ? BREAK : STUDY; sessionId=0; elapsedDeadline=0; wallDeadline=0; }
    public void reset() { breakPhase=false; running=false; remaining=STUDY; sessionId=0; elapsedDeadline=0; wallDeadline=0; }
}
