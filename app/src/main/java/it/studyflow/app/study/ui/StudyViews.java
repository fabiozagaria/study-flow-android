package it.studyflow.app.study.ui;

import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import it.studyflow.app.R;
import java.util.Locale;
import org.json.*;

final class StudyViews {
  private StudyViews() {}

  static int dp(Context c, int value) {
    return Math.round(value * c.getResources().getDisplayMetrics().density);
  }

  static int color(Context c, int resource) {
    return ContextCompat.getColor(c, resource);
  }

  static GradientDrawable rounded(Context c, int fill, int border) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color(c, fill));
    d.setCornerRadius(dp(c, 14));
    if (border != 0) d.setStroke(dp(c, 1), color(c, border));
    return d;
  }

  static TextView text(LinearLayout parent, String value, int size) {
    TextView t = new TextView(parent.getContext());
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(
        color(parent.getContext(), size < 17 ? R.color.study_muted : R.color.study_text));
    t.setTextIsSelectable(true);
    t.setLineSpacing(dp(parent.getContext(), 5), 1.1f);
    t.setPadding(0, dp(parent.getContext(), 8), 0, dp(parent.getContext(), 12));
    if (size >= 21) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    parent.addView(t, new LinearLayout.LayoutParams(-1, -2));
    return t;
  }

  static LinearLayout card(LinearLayout parent, int accent) {
    Context c = parent.getContext();
    MaterialCardView card = new MaterialCardView(c);
    card.setRadius(dp(c, 20));
    card.setCardElevation(dp(c, 1));
    card.setCardBackgroundColor(color(c, R.color.study_surface));
    card.setStrokeWidth(dp(c, 1));
    card.setStrokeColor(color(c, accent));
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.bottomMargin = dp(c, 16);
    parent.addView(card, params);
    LinearLayout inside = new LinearLayout(c);
    inside.setOrientation(LinearLayout.VERTICAL);
    inside.setPadding(dp(c, 18), dp(c, 12), dp(c, 18), dp(c, 12));
    card.addView(inside, new android.widget.FrameLayout.LayoutParams(-1, -2));
    return inside;
  }

  static void banner(LinearLayout parent, String title, String subtitle) {
    LinearLayout box = new LinearLayout(parent.getContext());
    box.setOrientation(LinearLayout.VERTICAL);
    box.setBackgroundResource(R.drawable.study_hero);
    int p = dp(parent.getContext(), 20);
    box.setPadding(p, p, p, p);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.bottomMargin = dp(parent.getContext(), 18);
    parent.addView(box, params);
    TextView heading = text(box, title, 23);
    heading.setTextColor(android.graphics.Color.WHITE);
    TextView detail = text(box, subtitle, 13);
    detail.setTextColor(android.graphics.Color.rgb(215, 235, 231));
  }

  static void section(LinearLayout parent, String title, String body, boolean code) {
    int accent =
        code
            ? R.color.study_quiz
            : title.equals("Errori comuni")
                ? R.color.study_warning
                : title.equals("Caso d’uso")
                    ? R.color.study_spring
                    : title.startsWith("Da ripassare")
                        ? R.color.study_error
                        : title.startsWith("Corretta")
                            ? R.color.study_spring
                            : R.color.study_recall;
    LinearLayout inside = card(parent, accent);
    TextView heading = text(inside, title, 20);
    heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    heading.setTextColor(color(parent.getContext(), accent));
    TextView t = text(inside, body, code ? 14 : 17);
    if (code) {
      t.setTypeface(Typeface.MONOSPACE);
      t.setTextColor(color(parent.getContext(), R.color.study_code_text));
      t.setBackground(rounded(parent.getContext(), R.color.study_code, 0));
      int padding = dp(parent.getContext(), 14);
      t.setPadding(padding, padding, padding, padding);
      t.setLineSpacing(dp(parent.getContext(), 4), 1.05f);
    }
  }

  static MaterialButton button(LinearLayout parent, String label, Runnable action) {
    Context c = parent.getContext();
    MaterialButton b = new MaterialButton(c);
    b.setText(label);
    b.setTextSize(15);
    b.setAllCaps(false);
    b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    b.setCornerRadius(dp(c, 14));
    b.setMinHeight(dp(c, 54));
    b.setPadding(dp(c, 18), dp(c, 8), dp(c, 18), dp(c, 8));
    String lower = label.toLowerCase(Locale.ROOT);
    int accent = R.color.study_quiz, fill = R.color.study_quiz, foreground = R.color.study_surface;
    if (lower.contains("ripasso attivo")) accent = fill = R.color.study_recall;
    if (lower.contains("error") || lower.contains("insufficient")) {
      accent = foreground = R.color.study_warning;
      fill = R.color.study_warning_soft;
    } else if (lower.contains("documentazione")) {
      foreground = accent;
      fill = R.color.study_surface;
    } else if (lower.contains("letta")) {
      accent = foreground = R.color.study_spring;
      fill = R.color.study_spring_soft;
    }
    b.setBackgroundTintList(ColorStateList.valueOf(color(c, fill)));
    b.setTextColor(color(c, foreground));
    b.setStrokeWidth(dp(c, 1));
    b.setStrokeColor(ColorStateList.valueOf(color(c, accent)));
    b.setOnClickListener(v -> action.run());
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.bottomMargin = dp(c, 6);
    parent.addView(b, params);
    return b;
  }

  static void sources(LinearLayout parent, String json) {
    LinearLayout box = card(parent, R.color.study_quiz);
    TextView title = text(box, "Documentazione ufficiale", 20);
    title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    title.setTextColor(color(parent.getContext(), R.color.study_quiz));
    try {
      JSONArray links = new JSONArray(json);
      for (int i = 0; i < links.length(); i++) {
        String url = links.getString(i);
        Uri uri = Uri.parse(url);
        if (!"https".equals(uri.getScheme())) continue;
        button(
            box,
            "Documentazione · " + uri.getHost(),
            () -> {
              try {
                parent.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
              } catch (ActivityNotFoundException ex) {
                Toast.makeText(parent.getContext(), "Nessun browser disponibile", Toast.LENGTH_LONG)
                    .show();
              }
            });
      }
    } catch (JSONException ex) {
      text(box, "Fonte non disponibile", 16);
    }
  }

  static String outcome(String value) {
    switch (value) {
      case "CORRECT":
        return "Corretta";
      case "PARTIAL":
        return "Parziale";
      case "WRONG":
        return "Da ripassare";
      case "SKIPPED":
        return "Saltata";
      default:
        return "In corso";
    }
  }
}
