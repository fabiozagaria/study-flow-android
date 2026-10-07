package it.studyflow.app;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
public final class Appearance {
    private Appearance() {}
    public static void apply(Context context) {
        int mode = context.getSharedPreferences("appearance", Context.MODE_PRIVATE).getInt("mode", -1);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    public static int theme(Context context) {
        return context.getSharedPreferences("appearance", Context.MODE_PRIVATE).getBoolean("black", false) ? R.style.Theme_StudyFlow_Black : R.style.Theme_StudyFlow;
    }
}
