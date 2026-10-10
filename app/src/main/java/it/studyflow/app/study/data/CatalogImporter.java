package it.studyflow.app.study.data;

import android.content.Context;
import it.studyflow.app.StudyDatabase;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Bundled, original Italian lessons. Import is atomic and never replaces user progress. */
public final class CatalogImporter {
  private CatalogImporter() {}

  public static void seed(Context context, StudyDatabase db) throws IOException, JSONException {
    if (db.learning().materialCount() > 0) return;
    String json;
    try (InputStream in = context.getAssets().open("study_catalog.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int n;
      while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
      json = out.toString(StandardCharsets.UTF_8.name());
    }
    JSONObject catalog = new JSONObject(json);
    List<Subject> subjects = new ArrayList<>();
    List<Topic> topics = new ArrayList<>();
    List<TheoryMaterial> materials = new ArrayList<>();
    List<Question> questions = new ArrayList<>();
    List<AnswerOption> options = new ArrayList<>();
    JSONArray ss = catalog.getJSONArray("subjects");
    for (int i = 0; i < ss.length(); i++) {
      JSONObject row = ss.getJSONObject(i);
      Subject s = new Subject();
      s.id = row.getString("id");
      s.title = row.getString("title");
      s.position = i;
      subjects.add(s);
    }
    JSONArray ts = catalog.getJSONArray("topics");
    for (int i = 0; i < ts.length(); i++) {
      JSONObject row = ts.getJSONObject(i);
      Topic t = new Topic();
      t.id = row.getString("id");
      t.subjectId = row.getString("subjectId");
      t.title = row.getString("title");
      t.category = row.getString("category");
      t.position = i;
      topics.add(t);
      TheoryMaterial m = new TheoryMaterial();
      m.id = t.id + "-theory";
      m.topicId = t.id;
      m.explanation = row.getString("explanation");
      m.example = row.getString("example");
      m.useCase = row.getString("useCase");
      m.commonErrors = row.getString("commonErrors");
      m.sources = row.getJSONArray("sources").toString();
      m.versionLabel = row.getString("versionLabel");
      m.revision = catalog.getInt("revision");
      materials.add(m);
      JSONArray qs = row.getJSONArray("questions");
      for (int j = 0; j < qs.length(); j++) {
        JSONObject qr = qs.getJSONObject(j);
        Question q = new Question();
        q.id = qr.getString("id");
        q.topicId = t.id;
        q.kind = qr.getString("kind");
        q.prompt = qr.getString("prompt");
        q.explanation = qr.getString("explanation");
        q.rubric = qr.optString("rubric", "");
        q.position = j;
        questions.add(q);
        if (q.kind.equals("QUIZ")) {
          JSONArray os = qr.getJSONArray("options");
          int correct = qr.getInt("correctIndex");
          QuizRules.evaluate(null, correct, os.length());
          for (int k = 0; k < os.length(); k++) {
            AnswerOption o = new AnswerOption();
            o.id = q.id + "-" + k;
            o.questionId = q.id;
            o.text = os.getString(k);
            o.correct = k == correct;
            o.position = k;
            options.add(o);
          }
        }
      }
    }
    db.runInTransaction(
        () -> {
          if (db.learning().materialCount() > 0) return;
          LearningDao dao = db.learning();
          dao.subjects(subjects);
          dao.topics(topics);
          dao.materials(materials);
          dao.questions(questions);
          dao.options(options);
        });
  }
}
