package it.studyflow.app;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density); }
    @Override protected void onCreate(Bundle state) {
        Appearance.apply(this); setTheme(Appearance.theme(this)); super.onCreate(state);
        new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(getResources().getBoolean(R.bool.light_bars));
        SharedPreferences prefs = getSharedPreferences("appearance", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        MaterialToolbar toolbar = new MaterialToolbar(this); toolbar.setTitle("Impostazioni");
        toolbar.setTitleTextAppearance(this, R.style.StudyTitle); toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish()); root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(72)));
        ScrollView scroll = new ScrollView(this); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24),dp(24),dp(24),dp(24)); scroll.addView(content);
        TextView label = new TextView(this); label.setText("Aspetto"); label.setTextSize(16); label.setTextColor(getColor(R.color.sf_primary)); content.addView(label);
        int mode = prefs.getInt("mode", -1);
        String[] names = {"Uguale al sistema", "Chiaro", "Scuro"}; int[] modes = {-1, 1, 2};
        LinearLayout themeRow = new LinearLayout(this); themeRow.setOrientation(LinearLayout.VERTICAL); themeRow.setPadding(0,dp(28),0,dp(28));
        TextView title = new TextView(this); title.setText("Tema scuro"); title.setTextSize(21); themeRow.addView(title);
        TextView subtitle = new TextView(this); subtitle.setText(names[mode == 1 ? 1 : mode == 2 ? 2 : 0]); subtitle.setTextSize(17); subtitle.setTextColor(getColor(R.color.sf_muted)); themeRow.addView(subtitle);
        themeRow.setBackgroundResource(android.R.drawable.list_selector_background); content.addView(themeRow);
        themeRow.setOnClickListener(v -> new MaterialAlertDialogBuilder(this).setTitle("Tema")
            .setSingleChoiceItems(names, mode == 1 ? 1 : mode == 2 ? 2 : 0, (dialog, which) -> {
                prefs.edit().putInt("mode", modes[which]).apply(); dialog.dismiss();
                AppCompatDelegate.setDefaultNightMode(modes[which]);
                recreate();
            }).setNegativeButton("Annulla", null).show());
        MaterialSwitch black = new MaterialSwitch(this); black.setText("Tema nero notte\nNero puro in modalità scura"); black.setTextSize(18); black.setPadding(0,dp(12),0,dp(24)); black.setChecked(prefs.getBoolean("black", false)); content.addView(black);
        black.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean("black", checked).apply(); recreate(); });
        com.google.android.material.button.MaterialButton notifications = new com.google.android.material.button.MaterialButton(this);
        notifications.setText("Gestisci notifiche"); notifications.setOnClickListener(v -> startActivity(new android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName()))); content.addView(notifications);
        TextView aboutLabel = new TextView(this); aboutLabel.setText("Informazioni"); aboutLabel.setTextSize(16); aboutLabel.setTextColor(getColor(R.color.sf_primary)); aboutLabel.setPadding(0,dp(32),0,dp(24)); content.addView(aboutLabel);
        TextView about = new TextView(this); about.setText("StudyFlow\nVersione 1.7\n\nIl tuo planner di studio, sul tuo dispositivo."); about.setTextSize(18); about.setLineSpacing(dp(4),1); content.addView(about);
        TextView signature = new TextView(this); signature.setText("Sviluppato da fabiozagariadev"); signature.setTextSize(18); signature.setPadding(0, dp(24), 0, dp(12)); content.addView(signature);
        addProfileLink(content, "GitHub · fabiozagaria", "https://github.com/fabiozagaria");
        if (!getString(R.string.author_linkedin).isEmpty()) addProfileLink(content, "LinkedIn", getString(R.string.author_linkedin));
    }
    private void addProfileLink(LinearLayout parent, String label, String url) {
        com.google.android.material.button.MaterialButton link = new com.google.android.material.button.MaterialButton(this);
        link.setText(label);
        link.setOnClickListener(v -> {
            try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))); }
            catch (android.content.ActivityNotFoundException e) { Toast.makeText(this, "Nessun browser disponibile", Toast.LENGTH_SHORT).show(); }
        });
        parent.addView(link, new LinearLayout.LayoutParams(-2, -2));
    }
}
