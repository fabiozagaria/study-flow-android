package it.studyflow.app;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
/** Local, deterministic extraction: no email content is sent to an AI service. */
public final class EventExtractor {
    public static class Result {
        public String date = "", time = "", warning = "";
    }
    private static final Pattern EVENT = Pattern.compile("(?iu)\\b(appuntamento|riunione|esame|lezione|colloquio|visita|prenotazione|evento|scadenza|meeting|webinar|workshop|verifica|appointment|exam|lecture|interview)\\b");
    private static final Pattern ISO = Pattern.compile("\\b(20\\d{2})-(\\d{1,2})-(\\d{1,2})\\b");
    private static final Pattern NUM = Pattern.compile("(?<![\\d/.-])(\\d{1,2})[/.](\\d{1,2})(?:[/.](20\\d{2}))?(?![\\d/.-])");
    private static final String MONTHS = "gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre";
    private static final Pattern TEXT = Pattern.compile("(?iu)\\b(\\d{1,2})\\s+("+MONTHS+")(?:\\s+(20\\d{2}))?\\b");
    private static final Pattern TIME = Pattern.compile("(?iu)(?:\\b(?:ore|alle|at)\\s+(\\d{1,2})(?:[:.](\\d{2}))?\\b|\\b(\\d{1,2}):(\\d{2})\\b)");
    public static Result extract(String subject, String body, LocalDate received) {
        String input = subject + "\n" + body;
        if (!EVENT.matcher(input).find()) return null;
        Set<LocalDate> dates = new LinkedHashSet<>(); boolean guessed = false;
        Matcher m = ISO.matcher(input);
        while (m.find()) add(dates,Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
        m = NUM.matcher(input);
        while (m.find()) { boolean inferred = m.group(3)==null; guessed |= inferred; add(dates,inferred ? received.getYear() : Integer.parseInt(m.group(3)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(1))); }
        m = TEXT.matcher(input);
        List<String> months = Arrays.asList(MONTHS.split("\\|"));
        while (m.find()) { boolean inferred=m.group(3)==null; guessed |= inferred; add(dates,inferred ? received.getYear() : Integer.parseInt(m.group(3)),months.indexOf(m.group(2).toLowerCase(Locale.ITALIAN))+1,Integer.parseInt(m.group(1))); }
        if (dates.isEmpty()) {
            if (Pattern.compile("(?iu)\\bdomani\\b").matcher(input).find()) { dates.add(received.plusDays(1)); guessed=true; }
            else if (Pattern.compile("(?iu)\\boggi\\b").matcher(input).find()) { dates.add(received); guessed=true; }
        }
        if (dates.isEmpty()) return null;
        Result result = new Result();
        if (dates.size()==1) result.date=dates.iterator().next().toString();
        else result.warning="Più date nel messaggio: scegli quella dell’appuntamento.";
        if (guessed && dates.size()==1) result.warning="Anno o giorno dedotto dalla data dell’email: verifica la data.";
        Set<String> times = new LinkedHashSet<>(); m=TIME.matcher(input);
        while (m.find()) {
            int h=Integer.parseInt(m.group(1)!=null ? m.group(1) : m.group(3));
            int min=Integer.parseInt(m.group(1)!=null ? (m.group(2)==null ? "0" : m.group(2)) : m.group(4));
            if (h<24 && min<60) times.add(String.format(Locale.ROOT,"%02d:%02d",h,min));
        }
        if (times.size()==1) result.time=times.iterator().next();
        else if (times.size()>1) result.warning += " Più orari: scegli l’ora di inizio.";
        return result;
    }
    private static void add(Set<LocalDate> dates,int y,int m,int d) { try { dates.add(LocalDate.of(y,m,d)); } catch (DateTimeException ignored) {} }
    private EventExtractor() {}
}
