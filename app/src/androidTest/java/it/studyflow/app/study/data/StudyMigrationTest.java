package it.studyflow.app.study.data;

import static org.junit.Assert.*;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import it.studyflow.app.StudyDatabase;
import java.io.IOException;
import org.junit.*;

public class StudyMigrationTest {
  @Rule
  public MigrationTestHelper helper =
      new MigrationTestHelper(
          InstrumentationRegistry.getInstrumentation(),
          StudyDatabase.class.getCanonicalName(),
          new FrameworkSQLiteOpenHelperFactory());

  @Test
  public void upgradesVersionThreeWithoutLosingPlannerData() throws IOException {
    SupportSQLiteDatabase db = helper.createDatabase("migration-study", 3);
    db.execSQL(
        "INSERT INTO tasks(id,title,subject,due,priority,done,dueTime,reminderSentFor)"
            + " VALUES(1,'Existing','Java','2026-10-10',1,0,'09:00','')");
    db.execSQL("INSERT INTO sessions(id,minutes) VALUES(123,25)");
    db.execSQL(
        "INSERT INTO calendar_events(id,title,date,time,notes,sourceKey)"
            + " VALUES(1,'Meeting','2026-10-10','10:00','','manual-1')");
    db.close();
    db = helper.runMigrationsAndValidate("migration-study", 4, true, StudyMigrations.FROM_3_TO_4);
    try (android.database.Cursor c = db.query("SELECT title FROM tasks WHERE id=1")) {
      assertTrue(c.moveToFirst());
      assertEquals("Existing", c.getString(0));
    }
    try (android.database.Cursor c = db.query("SELECT minutes FROM sessions WHERE id=123")) {
      assertTrue(c.moveToFirst());
      assertEquals(25, c.getInt(0));
    }
    try (android.database.Cursor c = db.query("SELECT title FROM calendar_events WHERE id=1")) {
      assertTrue(c.moveToFirst());
      assertEquals("Meeting", c.getString(0));
    }
    db.close();
  }
}
