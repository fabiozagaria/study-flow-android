package it.studyflow.app;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Keep top-level Activity destinations selected and reuse an existing destination. */
public final class TopLevelNavigation {
  private TopLevelNavigation() {}

  public static void attach(Activity activity, int selected) {
    BottomNavigationView nav = activity.findViewById(R.id.navigation);
    nav.setSelectedItemId(selected);
    nav.setOnItemSelectedListener(
        item -> {
          int id = item.getItemId();
          if (id == selected) return true;
          open(activity, id);
          return false;
        });
  }

  public static void open(Activity activity, int id) {
    Class<?> target =
        id == R.id.study
            ? StudyActivity.class
            : id == R.id.calendar ? CalendarActivity.class : MainActivity.class;
    Intent intent =
        new Intent(activity, target)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("section", id);
    activity.startActivity(intent);
    if (!(activity instanceof MainActivity)) activity.finish();
  }
}
