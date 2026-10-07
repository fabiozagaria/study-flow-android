package it.studyflow.app;
import java.time.*;
import java.util.*;
public final class CalendarIndex {
    public static class Day { public int tasks,done,events; }
    public static Map<LocalDate,Day> build(List<Task> tasks,List<CalendarEvent> events) {
        Map<LocalDate,Day> map=new HashMap<>();
        for(Task task:tasks) { Day d=map.computeIfAbsent(LocalDate.parse(task.due),key -> new Day()); d.tasks++; if(task.done)d.done++; }
        for(CalendarEvent event:events) map.computeIfAbsent(LocalDate.parse(event.date),key -> new Day()).events++;
        return map;
    }
    public static List<LocalDate> grid(YearMonth month) {
        LocalDate first=month.atDay(1); first=first.minusDays(first.getDayOfWeek().getValue()-1);
        List<LocalDate> dates=new ArrayList<>(); for(int i=0;i<42;i++)dates.add(first.plusDays(i)); return dates;
    }
    private CalendarIndex() {}
}
