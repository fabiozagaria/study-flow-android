package it.studyflow.app;
import java.time.*;
public final class DeadlineRules {
    public static long delay(String date,String time,ZoneId zone,long now) {
        long when=LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli();
        return Math.max(0,when-now);
    }
    public static String key(String date,String time) { return date+"T"+time; }
    private DeadlineRules() {}
}
