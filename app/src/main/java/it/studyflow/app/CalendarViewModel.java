package it.studyflow.app;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import java.util.List;
import java.util.concurrent.*;
public class CalendarViewModel extends AndroidViewModel {
    final StudyDao dao;
    final ExecutorService worker = Executors.newSingleThreadExecutor();
    final Handler main = new Handler(Looper.getMainLooper());
    public final LiveData<List<CalendarEvent>> events;
    public final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    public final MutableLiveData<List<MailProposal>> proposals = new MutableLiveData<>(new java.util.ArrayList<>());
    public final MutableLiveData<android.content.Intent> consent = new MutableLiveData<>();
    private boolean cleared;
    public void importGmail(android.accounts.Account account) {
        if (Boolean.TRUE.equals(busy.getValue())) return;
        busy.setValue(true); status.setValue("Analisi delle email recenti…");
        worker.execute(() -> {
            String token = null;
            try {
                token = com.google.android.gms.auth.GoogleAuthUtil.getToken(getApplication(), account, "oauth2:" + GmailReader.SCOPE);
                List<MailProposal> found = GmailReader.recent(token, new java.util.HashSet<>(dao.eventSources()));
                main.post(() -> { if (cleared) return; proposals.setValue(found); status.setValue(found.isEmpty() ? "Nessun nuovo appuntamento riconosciuto nelle ultime 40 email della posta in arrivo degli ultimi 30 giorni." : found.size()+" proposte. Controlla data e ora prima di confermare."); });
            } catch (com.google.android.gms.auth.UserRecoverableAuthException e) {
                consent.postValue(e.getIntent()); status.postValue("Conferma l’autorizzazione Google per proseguire.");
            } catch (Exception e) {
                status.postValue(e instanceof java.io.IOException ? e.getMessage() : "Impossibile collegare Gmail. Verifica configurazione OAuth, rete e Google Play Services.");
            } finally {
                if (token != null) { try { com.google.android.gms.auth.GoogleAuthUtil.clearToken(getApplication(), token); } catch (Exception ignored) {} }
                busy.postValue(false);
            }
        });
    }
    public void clearProposals() { proposals.setValue(new java.util.ArrayList<>()); }
    public void dismiss(String key) {
        List<MailProposal> list = new java.util.ArrayList<>(proposals.getValue() == null ? java.util.Collections.emptyList() : proposals.getValue());
        list.removeIf(proposal -> proposal.event.sourceKey.equals(key)); proposals.setValue(list);
    }
    public final MutableLiveData<String> status = new MutableLiveData<>("Gli eventi vengono salvati sul dispositivo.");
    public CalendarViewModel(@NonNull Application app) { super(app); dao = StudyDatabase.get(app).dao(); events = dao.events(); }
    public void save(CalendarEvent event) {
        worker.execute(() -> {
            try {
                if (event.id != 0) { dao.updateEvent(event); status.postValue("Evento aggiornato."); }
                else { long id = dao.insertEvent(event); status.postValue(id == -1 ? "Questo evento è già stato importato." : "Evento salvato."); main.post(() -> dismiss(event.sourceKey)); }
            } catch (RuntimeException e) { status.postValue("Impossibile salvare l’evento. Riprova."); }
        });
    }
    public void delete(CalendarEvent event) { worker.execute(() -> dao.deleteEvent(event)); }
    @Override protected void onCleared() { cleared=true; worker.shutdownNow(); }
}
