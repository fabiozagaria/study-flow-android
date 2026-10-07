package it.studyflow.app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
public class EventExtractorTest {
    private final LocalDate received=LocalDate.of(2026,10,7);
    @Test public void italianDateAndTime() { EventExtractor.Result r=EventExtractor.extract("Esame di matematica","Il 12 ottobre 2026 alle 09:30",received); assertEquals("2026-10-12",r.date); assertEquals("09:30",r.time); }
    @Test public void explicitNumericDate() { assertEquals("2027-02-01",EventExtractor.extract("Riunione","01/02/2027 ore 15",received).date); }
    @Test public void isoDate() { assertEquals("2027-01-20",EventExtractor.extract("Meeting","2027-01-20 at 10:00",received).date); }
    @Test public void invalidDateDoesNotBecomeEvent() { assertNull(EventExtractor.extract("Esame","31/02/2026",received)); }
    @Test public void missingYearRequiresReview() { EventExtractor.Result r=EventExtractor.extract("Lezione","12 novembre",received); assertEquals("2026-11-12",r.date); assertFalse(r.warning.isEmpty()); }
    @Test public void multipleDatesRequireSelection() { EventExtractor.Result r=EventExtractor.extract("Appuntamento","12/10/2026 oppure 13/10/2026",received); assertEquals("",r.date); assertFalse(r.warning.isEmpty()); }
    @Test public void noAppointmentNoProposal() { assertNull(EventExtractor.extract("Codice di accesso","Valido fino al 12/10/2026",received)); }
    @Test public void relativeDateUsesReceivedDay() { assertEquals("2026-10-08",EventExtractor.extract("Colloquio","domani alle 14",received).date); }
    @Test public void impossibleTimeIgnored() { assertEquals("",EventExtractor.extract("Lezione","12/10/2026 ore 29:99",received).time); }
    @Test public void multipleTimesAreAmbiguous() { EventExtractor.Result r=EventExtractor.extract("Visita","12/10/2026 dalle 09:00 alle 10:00",received); assertEquals("",r.time); assertFalse(r.warning.isEmpty()); }
}
