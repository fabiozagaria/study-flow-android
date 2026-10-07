package it.studyflow.app;
public class MailProposal {
    public final CalendarEvent event;
    public final String sender, excerpt, warning;
    public MailProposal(CalendarEvent event, String sender, String excerpt, String warning) { this.event=event; this.sender=sender; this.excerpt=excerpt; this.warning=warning; }
}
