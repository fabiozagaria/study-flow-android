package it.studyflow.app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
public class DeadlineRulesTest {
    @Test public void deadlineHasUserSelectedTime() { long now=Instant.parse("2026-10-07T08:00:00Z").toEpochMilli();assertEquals(3600000,DeadlineRules.delay("2026-10-07","09:00",ZoneId.of("UTC"),now)); }
    @Test public void overdueReminderIsImmediate() { assertEquals(0,DeadlineRules.delay("2026-10-06","09:00",ZoneId.of("UTC"),Instant.parse("2026-10-07T08:00:00Z").toEpochMilli())); }
    @Test public void localTimeUsesSelectedTimezone() { long now=Instant.parse("2026-10-07T06:00:00Z").toEpochMilli();assertEquals(3600000,DeadlineRules.delay("2026-10-07","09:00",ZoneId.of("Europe/Rome"),now)); }
    @Test public void changingTimeChangesDuplicateKey() { assertNotEquals(DeadlineRules.key("2026-10-07","09:00"),DeadlineRules.key("2026-10-07","10:00")); }
}
