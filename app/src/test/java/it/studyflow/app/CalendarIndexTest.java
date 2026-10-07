package it.studyflow.app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.*;
import java.util.*;
public class CalendarIndexTest {
    @Test public void gridStartsOnMondayAndHasSixWeeks() { List<LocalDate> days=CalendarIndex.grid(YearMonth.of(2026,10));assertEquals(DayOfWeek.MONDAY,days.get(0).getDayOfWeek());assertEquals(42,days.size());assertEquals(LocalDate.of(2026,9,28),days.get(0)); }
    @Test public void leapDayAppears() { assertTrue(CalendarIndex.grid(YearMonth.of(2028,2)).contains(LocalDate.of(2028,2,29))); }
    @Test public void nonLeapFebruaryHasTwentyEightDays() { assertEquals(28,CalendarIndex.grid(YearMonth.of(2027,2)).stream().filter(d -> d.getMonthValue()==2).count()); }
    @Test public void tasksAndEventsShareDateWithIndependentCounts() { Task a=new Task();a.due="2026-10-07";Task b=new Task();b.due=a.due;b.done=true;CalendarEvent e=new CalendarEvent();e.date=a.due;CalendarIndex.Day day=CalendarIndex.build(Arrays.asList(a,b),Collections.singletonList(e)).get(LocalDate.parse(a.due));assertEquals(2,day.tasks);assertEquals(1,day.done);assertEquals(1,day.events); }
    @Test public void emptyDayHasNoMarker() { assertFalse(CalendarIndex.build(Collections.emptyList(),Collections.emptyList()).containsKey(LocalDate.now())); }
}
