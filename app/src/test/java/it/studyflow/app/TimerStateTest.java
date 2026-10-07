package it.studyflow.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class TimerStateTest {
    @Test public void countdownContinuesWithoutActivity() { TimerState s=new TimerState();s.start(100,1000,1);assertEquals(TimerState.STUDY-60000,s.remainingAt(60100,61000,1)); }
    @Test public void wallClockChangesDoNotChangeRunningTimer() { TimerState s=new TimerState();s.start(100,1000,1);assertEquals(TimerState.STUDY-100,s.remainingAt(200,999999999,1)); }
    @Test public void pauseFreezesAndResumeKeepsStudyIdentity() { TimerState s=new TimerState();s.start(100,1000,1);long id=s.sessionId;s.pause(60100,61000,1);assertEquals(TimerState.STUDY-60000,s.remainingAt(900000,900000,1));s.start(900000,900000,1);assertEquals(id,s.sessionId);assertEquals(TimerState.STUDY-61000,s.remainingAt(901000,901000,1)); }
    @Test public void expirationNeverNegative() { TimerState s=new TimerState();s.start(100,1000,1);assertEquals(0,s.remainingAt(99999999,99999999,1)); }
    @Test public void rebootUsesWallDeadline() { TimerState s=new TimerState();s.start(100,1000,1);assertEquals(TimerState.STUDY-30000,s.remainingAt(3,31000,2)); }
    @Test public void completingStudyPreparesManualBreak() { TimerState s=new TimerState();s.start(100,1000,1);s.nextPhase();assertFalse(s.running);assertTrue(s.breakPhase);assertEquals(TimerState.BREAK,s.remaining);assertEquals(0,s.sessionId); }
    @Test public void completingBreakPreparesStudy() { TimerState s=new TimerState();s.nextPhase();s.nextPhase();assertFalse(s.breakPhase);assertEquals(TimerState.STUDY,s.remaining); }
    @Test public void resetStopsAndReturnsToStudy() { TimerState s=new TimerState();s.nextPhase();s.start(100,1000,1);s.reset();assertFalse(s.running);assertFalse(s.breakPhase);assertEquals(TimerState.STUDY,s.remaining);assertEquals(0,s.sessionId); }
}
