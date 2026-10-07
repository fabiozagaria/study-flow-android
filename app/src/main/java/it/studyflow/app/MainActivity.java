package it.studyflow.app;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.LocalDate;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private int appliedTheme;
    private int getThemeResIdForAppearance() { return appliedTheme; }
    private PlannerViewModel model;
    private TaskAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    private int section = R.id.today, minutes;
    private TextView summary, clock, timerMode;
    private MaterialButton start;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final androidx.activity.result.ActivityResultLauncher<String> notificationPermission = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), granted -> {
        if (granted && model!=null) model.restoreReminders();
        if (!granted) Toast.makeText(this,"Puoi attivare gli avvisi da Impostazioni → Gestisci notifiche",Toast.LENGTH_LONG).show();
    });
    private final Runnable tick = new Runnable() {
        @Override public void run() { updateTimer(); handler.postDelayed(this, 500); }
    };

    @Override public void onCreate(Bundle state) {
        Appearance.apply(this); appliedTheme = Appearance.theme(this); setTheme(appliedTheme);
        super.onCreate(state);
        new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(getResources().getBoolean(R.bool.light_bars)); setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        MaterialToolbar top = findViewById(R.id.toolbar);
        top.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.settings) { startActivity(new android.content.Intent(this, SettingsActivity.class)); return true; }
            if (item.getItemId() == R.id.appearance) {
                android.content.SharedPreferences appearance = getSharedPreferences("appearance", MODE_PRIVATE);
                int mode = appearance.getInt("mode", -1);
                int[] modes = {-1, 1, 2};
                new MaterialAlertDialogBuilder(this).setTitle("Tema")
                    .setSingleChoiceItems(new String[]{"Uguale al sistema", "Chiaro", "Scuro"}, mode == 1 ? 1 : mode == 2 ? 2 : 0, (dialog, which) -> {
                        appearance.edit().putInt("mode", modes[which]).apply(); dialog.dismiss();
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(modes[which]); recreate();
                    }).setNegativeButton("Annulla", null).show();
                return true;
            }
            return false;
        });
        findViewById(R.id.focus).setOnClickListener(v -> ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(R.id.timer));
        model = new ViewModelProvider(this).get(PlannerViewModel.class);
        Notifications.channels(this);
        android.content.SharedPreferences notices = getSharedPreferences("notifications", MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT>=33 && !Notifications.allowed(this) && !notices.getBoolean("asked",false)) {
            notices.edit().putBoolean("asked",true).apply(); notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
        model.restoreReminders();
        summary = findViewById(R.id.summary); clock = findViewById(R.id.clock);
        timerMode = findViewById(R.id.timerMode); start = findViewById(R.id.start);
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new TaskAdapter.Actions() {
            @Override public void toggle(Task t, boolean done) { t.done = done; model.save(t); }
            @Override public void edit(Task t) { editTask(t); }
        });
        list.setAdapter(adapter);
        model.tasks.observe(this, value -> { tasks = value; render(); });
        model.minutes.observe(this, value -> { minutes = value; render(); });
        findViewById(R.id.add).setOnClickListener(v -> editTask(null));
        start.setOnClickListener(v -> PomodoroService.command(this, TimerStore.read(this).running ? PomodoroService.PAUSE : PomodoroService.START));
        findViewById(R.id.reset).setOnClickListener(v -> PomodoroService.command(this, PomodoroService.RESET));
        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setOnItemSelectedListener(item -> { if (item.getItemId() == R.id.calendar) { startActivity(new android.content.Intent(this, CalendarActivity.class)); return false; } section = item.getItemId(); render(); return true; });
        if (state != null) nav.setSelectedItemId(state.getInt("section", R.id.today));
        if (getIntent().getBooleanExtra("openTimer",false)) nav.setSelectedItemId(R.id.timer);
        render();
    }

    private void render() {
        boolean taskPage = section == R.id.today || section == R.id.tasks;
        findViewById(R.id.list).setVisibility(taskPage ? View.VISIBLE : View.GONE);
        findViewById(R.id.add).setVisibility(taskPage ? View.VISIBLE : View.GONE);
        findViewById(R.id.timerPanel).setVisibility(section == R.id.timer ? View.VISIBLE : View.GONE);
        List<Task> shown = new ArrayList<>(); int completed = 0;
        String today = LocalDate.now().toString();
        for (Task task : tasks) {
            if (task.done) completed++;
            if (section == R.id.tasks || (!task.done && task.due.compareTo(today) <= 0)) shown.add(task);
        }
        adapter.submit(shown);
        findViewById(R.id.focusCard).setVisibility(section == R.id.today ? View.VISIBLE : View.GONE);
        findViewById(R.id.listHeading).setVisibility(taskPage ? View.VISIBLE : View.GONE);
        findViewById(R.id.empty).setVisibility(taskPage && shown.isEmpty() ? View.VISIBLE : View.GONE);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (section == R.id.today) {
            toolbar.setTitle("StudyFlow");
            summary.setText(shown.isEmpty() ? "Tutto in ordine\nNessuna attività in scadenza" : shown.size() + " attività da completare\nIn scadenza oggi o in ritardo");
        } else if (section == R.id.tasks) {
            toolbar.setTitle("Le tue attività"); summary.setText(tasks.isEmpty() ? "Aggiungi la tua prima attività.\nTocca una scheda per modificarla." : tasks.size() + " attività · " + completed + " completate\nTocca una scheda per modificarla.");
        } else if (section == R.id.timer) {
            toolbar.setTitle("Concentrati"); summary.setText("Dedica questo tempo a una sola attività.");
        } else {
            toolbar.setTitle("I tuoi progressi");
            int progress = tasks.isEmpty() ? 0 : completed * 100 / tasks.size();
            summary.setText("Tempo studiato\n" + minutes + " minuti\n\nPomodori completati\n" + minutes / 25 + "\n\nAttività completate\n" + completed + " su " + tasks.size() + " (" + progress + "%)");
        }
    }

    private void editTask(Task original) {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density); form.setPadding(padding, 0, padding, 0);
        EditText title = new EditText(this); title.setHint("Titolo attività"); title.setSingleLine(true);
        EditText subject = new EditText(this); subject.setHint("Materia, es. Matematica"); subject.setSingleLine(true);
        MaterialButton date = new MaterialButton(this);
        final LocalDate[] selected = {original == null ? LocalDate.now() : LocalDate.parse(original.due)};
        date.setText("Scadenza: " + selected[0]);
        date.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, day) -> {
            selected[0] = LocalDate.of(year, month + 1, day); date.setText("Scadenza: " + selected[0]);
        }, selected[0].getYear(), selected[0].getMonthValue() - 1, selected[0].getDayOfMonth()).show());
        final String[] dueTime = {original == null ? "09:00" : original.dueTime};
        MaterialButton dueHour = new MaterialButton(this); dueHour.setText("Scadenza alle " + dueTime[0]);
        dueHour.setOnClickListener(v -> { java.time.LocalTime t=java.time.LocalTime.parse(dueTime[0]); new android.app.TimePickerDialog(this,(picker,h,m) -> { dueTime[0]=String.format(Locale.ROOT,"%02d:%02d",h,m); dueHour.setText("Scadenza alle "+dueTime[0]); },t.getHour(),t.getMinute(),true).show(); });
        TextView priorityLabel = new TextView(this); priorityLabel.setText("Priorità");
        Spinner priority = new Spinner(this);
        priority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Bassa", "Media", "Alta"}));
        priority.setSelection(original == null ? 1 : original.priority);
        if (original != null) { title.setText(original.title); subject.setText(original.subject); }
        form.addView(title); form.addView(subject); form.addView(date); form.addView(dueHour); form.addView(priorityLabel); form.addView(priority);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setTitle(original == null ? "Nuova attività" : "Modifica attività")
                .setView(form).setNegativeButton("Annulla", null).setPositiveButton("Salva", null);
        if (original != null) builder.setNeutralButton("Elimina", (dialog, which) ->
            new MaterialAlertDialogBuilder(this).setTitle("Eliminare l’attività?").setMessage(original.title)
                .setNegativeButton("Annulla", null).setPositiveButton("Elimina", (d, w) -> model.delete(original)).show());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (title.getText().toString().trim().isEmpty()) { title.setError("Inserisci un titolo"); return; }
            if (subject.getText().toString().trim().isEmpty()) { subject.setError("Inserisci una materia"); return; }
            Task task = new Task();
            if (original != null) { task.id = original.id; task.done = original.done; }
            task.title = title.getText().toString().trim(); task.subject = subject.getText().toString().trim();
            task.due = selected[0].toString(); task.dueTime=dueTime[0]; task.priority = priority.getSelectedItemPosition();
            model.save(task); dialog.dismiss();
        }));
        dialog.show();
    }

    private void updateTimer() {
        TimerState state=TimerStore.read(this);
        long seconds=(TimerStore.remaining(this,state)+999)/1000;
        clock.setText(String.format(Locale.ITALIAN,"%02d:%02d",seconds/60,seconds%60));
        timerMode.setText(state.breakPhase ? "Pausa" : "Sessione di studio"); start.setText(state.running ? "Metti in pausa" : "Avvia");
    }
    @Override protected void onResume() {
        super.onResume(); if (getThemeResIdForAppearance()!=Appearance.theme(this)) { recreate(); return; }
        if (TimerStore.read(this).running) PomodoroService.command(this,PomodoroService.START);
        handler.removeCallbacks(tick); tick.run(); render(); model.restoreReminders();
    }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(tick); }
    @Override protected void onNewIntent(android.content.Intent intent) { super.onNewIntent(intent); setIntent(intent); if(intent.getBooleanExtra("openTimer",false)) ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(R.id.timer); }
    @Override protected void onSaveInstanceState(Bundle state) { state.putInt("section",section); super.onSaveInstanceState(state); }
}
