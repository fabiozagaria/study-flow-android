package it.studyflow.app;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class CalendarActivity extends AppCompatActivity {
    CalendarViewModel model;
    LocalDate selected = LocalDate.now();
    List<CalendarEvent> all = new ArrayList<>();
    private int theme;
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> signIn = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
        try {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(com.google.android.gms.common.api.ApiException.class);
            if (account != null && account.getAccount() != null) model.importGmail(account.getAccount());
        } catch (com.google.android.gms.common.api.ApiException e) {
            model.status.setValue(e.getStatusCode() == 12501 ? "Accesso annullato." : "Accesso Google non riuscito (codice " + e.getStatusCode() + "). Verifica OAuth Android: package e SHA-1 dell’APK. Vedi le istruzioni Gmail nel progetto.");
        }
    });
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> consent = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) connect(); else model.status.setValue("Autorizzazione Gmail annullata.");
    });
    private com.google.android.gms.auth.api.signin.GoogleSignInClient googleClient() {
        com.google.android.gms.auth.api.signin.GoogleSignInOptions options = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestScopes(new com.google.android.gms.common.api.Scope(GmailReader.SCOPE)).build();
        return com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, options);
    }
    private void connect() {
        com.google.android.gms.auth.api.signin.GoogleSignInAccount account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getAccount() != null && com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(account, new com.google.android.gms.common.api.Scope(GmailReader.SCOPE))) model.importGmail(account.getAccount());
        else signIn.launch(googleClient().getSignInIntent());
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density); }
    @Override protected void onCreate(Bundle state) {
        Appearance.apply(this); theme = Appearance.theme(this); setTheme(theme); super.onCreate(state); setContentView(R.layout.activity_calendar);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(getResources().getBoolean(R.bool.light_bars));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(bars.left,bars.top,bars.right,bars.bottom); return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar); toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material); toolbar.setNavigationOnClickListener(v -> finish());
        if (state != null) selected = LocalDate.parse(state.getString("date", LocalDate.now().toString()));
        CalendarView month = findViewById(R.id.month); month.setFirstDayOfWeek(Calendar.MONDAY);
        month.setDate(selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        month.setOnDateChangeListener((v,y,m,d) -> { selected = LocalDate.of(y,m+1,d); render(); });
        model = new ViewModelProvider(this).get(CalendarViewModel.class);
        model.events.observe(this, events -> { all = events; render(); });
        model.status.observe(this, message -> ((TextView)findViewById(R.id.importStatus)).setText(message));
        findViewById(R.id.addEvent).setOnClickListener(v -> edit(null));
        findViewById(R.id.importMail).setOnClickListener(v -> new MaterialAlertDialogBuilder(this).setTitle("Leggi appuntamenti da Gmail")
            .setMessage("StudyFlow legge fino a 40 email recenti della posta in arrivo, negli ultimi 30 giorni. Cerca date e appuntamenti e propone gli eventi: sei tu a confermarli. Gmail richiede accesso in sola lettura. Il contenuto viene analizzato sul dispositivo; non vengono scaricati allegati.")
            .setNegativeButton("Annulla", null).setPositiveButton("Continua", (d,w) -> connect()).show());
        findViewById(R.id.disconnectMail).setOnClickListener(v -> new MaterialAlertDialogBuilder(this).setTitle("Scollegare Gmail?")
            .setMessage("Revoca l’accesso Google a StudyFlow. Gli eventi già salvati restano nel calendario.")
            .setNegativeButton("Annulla", null).setPositiveButton("Scollega", (d,w) -> googleClient().revokeAccess().addOnCompleteListener(task -> {
                if (task.isSuccessful()) { model.clearProposals(); model.status.setValue("Gmail scollegato."); }
                else model.status.setValue("Revoca non riuscita. Riprova con una connessione Internet oppure rimuovi l’accesso dalle impostazioni del tuo account Google.");
            })).show());
        model.busy.observe(this, loading -> {
            findViewById(R.id.importProgress).setVisibility(loading ? View.VISIBLE : View.GONE);
            findViewById(R.id.importMail).setEnabled(!loading); findViewById(R.id.disconnectMail).setEnabled(!loading);
        });
        model.proposals.observe(this, this::renderProposals);
        model.consent.observe(this, intent -> { if (intent != null) { model.consent.setValue(null); consent.launch(intent); } });
    }
    private void render() {
        ((TextView)findViewById(R.id.eventDay)).setText(selected.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)));
        LinearLayout list = findViewById(R.id.events); list.removeAllViews();
        for (CalendarEvent event : all) if (event.date.equals(selected.toString())) {
            MaterialCardView card = new MaterialCardView(this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin = dp(12); card.setLayoutParams(lp);
            TextView text = new TextView(this); text.setText((event.time.isEmpty() ? "Tutto il giorno" : event.time) + "\n" + event.title + (event.notes.isEmpty() ? "" : "\n" + event.notes)); text.setTextSize(18); text.setPadding(dp(20),dp(20),dp(20),dp(20)); text.setLineSpacing(dp(5),1); card.addView(text); card.setOnClickListener(v -> edit(event)); list.addView(card);
        }
        if (list.getChildCount() == 0) { TextView empty = new TextView(this); empty.setText("Nessun evento. Aggiungi un appuntamento per questo giorno."); empty.setTextSize(17); empty.setPadding(dp(16),dp(16),dp(16),dp(16)); list.addView(empty); }
    }
    void edit(CalendarEvent original) {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(24),0,dp(24),0);
        EditText title = new EditText(this); title.setHint("Titolo evento"); title.setSingleLine(true); title.setText(original == null ? "" : original.title);
        EditText notes = new EditText(this); notes.setHint("Luogo o note"); notes.setText(original == null ? "" : original.notes);
        final LocalDate[] date = {original == null || original.date.isEmpty() ? selected : LocalDate.parse(original.date)};
        final boolean[] dateChosen = {original == null || !original.date.isEmpty()};
        final String[] time = {original == null ? "" : original.time};
        MaterialButton day = new MaterialButton(this); day.setText(dateChosen[0] ? date[0].toString() : "Seleziona la data dell’evento");
        day.setOnClickListener(v -> new DatePickerDialog(this,(picker,y,m,d) -> { dateChosen[0] = true; date[0] = LocalDate.of(y,m+1,d); day.setText(date[0].toString()); },date[0].getYear(),date[0].getMonthValue()-1,date[0].getDayOfMonth()).show());
        MaterialButton hour = new MaterialButton(this); hour.setText(time[0].isEmpty() ? "Scegli ora" : time[0]);
        CheckBox allDay = new CheckBox(this); allDay.setText("Tutto il giorno"); allDay.setChecked(time[0].isEmpty()); hour.setEnabled(!allDay.isChecked());
        allDay.setOnCheckedChangeListener((button, checked) -> { hour.setEnabled(!checked); if (!checked && time[0].isEmpty()) { time[0]="09:00"; hour.setText(time[0]); } });
        hour.setOnClickListener(v -> { LocalTime t = time[0].isEmpty() ? LocalTime.of(9,0) : LocalTime.parse(time[0]); new TimePickerDialog(this,(picker,h,m) -> { time[0] = String.format(Locale.ROOT,"%02d:%02d",h,m); hour.setText(time[0]); },t.getHour(),t.getMinute(),true).show(); });
        form.addView(title); form.addView(day); form.addView(allDay); form.addView(hour); form.addView(notes);
        ScrollView scroll = new ScrollView(this); scroll.addView(form);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setTitle(original == null ? "Nuovo evento" : original.id == 0 ? "Conferma evento da Gmail" : "Modifica evento").setView(scroll).setNegativeButton("Annulla",null).setPositiveButton("Salva",null);
        if (original != null && original.id != 0) builder.setNeutralButton("Elimina",(dialog,w) -> new MaterialAlertDialogBuilder(this).setTitle("Eliminare l’evento?").setMessage(original.title).setNegativeButton("Annulla",null).setPositiveButton("Elimina",(d,i) -> model.delete(original)).show());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(-1).setOnClickListener(v -> {
            if (!dateChosen[0]) { Toast.makeText(this,"Seleziona la data dell’appuntamento",Toast.LENGTH_SHORT).show(); return; }
            if (title.getText().toString().trim().isEmpty()) { title.setError("Inserisci un titolo"); return; }
            CalendarEvent event = new CalendarEvent(); if (original != null) { event.id=original.id; event.sourceKey=original.sourceKey; }
            if (event.sourceKey.isEmpty()) event.sourceKey="manual:"+UUID.randomUUID();
            event.title=title.getText().toString().trim(); event.date=date[0].toString(); event.time=allDay.isChecked() ? "" : time[0]; event.notes=notes.getText().toString().trim();
            model.save(event); selected=date[0]; ((CalendarView)findViewById(R.id.month)).setDate(selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()); render(); dialog.dismiss();
        })); dialog.show();
    }
    private void renderProposals(List<MailProposal> proposals) {
        LinearLayout list = findViewById(R.id.proposals); list.removeAllViews();
        for (MailProposal proposal : proposals) {
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(0,dp(12),0,dp(12));
            TextView text = new TextView(this); text.setText(proposal.event.title + "\n" + (proposal.event.date.isEmpty() ? "Data da scegliere" : proposal.event.date) + (proposal.event.time.isEmpty() ? " · Ora da verificare" : " · " + proposal.event.time) + "\n" + proposal.sender + "\n" + proposal.warning); text.setTextSize(17); box.addView(text);
            MaterialButton review = new MaterialButton(this); review.setText("Controlla proposta"); box.addView(review);
            review.setOnClickListener(v -> new MaterialAlertDialogBuilder(this).setTitle(proposal.event.title)
                .setMessage(proposal.sender + "\n\n" + proposal.excerpt + "\n\n" + proposal.warning)
                .setNegativeButton("Chiudi",null).setNeutralButton("Ignora",(d,w) -> model.dismiss(proposal.event.sourceKey))
                .setPositiveButton("Modifica e salva",(d,w) -> edit(proposal.event)).show());
            list.addView(box);
        }
    }
    @Override protected void onResume() { super.onResume(); if (theme != Appearance.theme(this)) recreate(); }
    @Override protected void onSaveInstanceState(Bundle state) { state.putString("date",selected.toString()); super.onSaveInstanceState(state); }
}
