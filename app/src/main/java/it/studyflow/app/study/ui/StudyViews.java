package it.studyflow.app.study.ui;

import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import org.json.*;

final class StudyViews {
  private StudyViews() {}

  static TextView text(LinearLayout parent, String value, int size) {
    TextView t = new TextView(parent.getContext());
    t.setText(value);
    t.setTextSize(size);
    t.setTextIsSelectable(true);
    t.setLineSpacing(8, 1);
    t.setPadding(0, 12, 0, 16);
    parent.addView(t, new LinearLayout.LayoutParams(-1, -2));
    return t;
  }

  static void section(LinearLayout parent, String title, String body, boolean code) {
    text(parent, title, 21);
    TextView t = text(parent, body, 17);
    if (code) t.setTypeface(Typeface.MONOSPACE);
  }

  static MaterialButton button(LinearLayout parent, String label, Runnable action) {
    MaterialButton b = new MaterialButton(parent.getContext());
    b.setText(label);
    b.setOnClickListener(v -> action.run());
    parent.addView(b, new LinearLayout.LayoutParams(-1, -2));
    return b;
  }

  static void sources(LinearLayout parent, String json) {
    try {
      JSONArray links = new JSONArray(json);
      for (int i = 0; i < links.length(); i++) {
        String url = links.getString(i);
        Uri uri = Uri.parse(url);
        if (!"https".equals(uri.getScheme())) continue;
        button(
            parent,
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
      text(parent, "Fonte non disponibile", 16);
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
