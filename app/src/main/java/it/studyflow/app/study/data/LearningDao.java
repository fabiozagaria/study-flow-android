package it.studyflow.app.study.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface LearningDao {
  @Query("SELECT COUNT(*) FROM study_materials")
  int materialCount();

  @Insert(onConflict = OnConflictStrategy.ABORT)
  void subjects(List<Subject> values);

  @Insert(onConflict = OnConflictStrategy.ABORT)
  void topics(List<Topic> values);

  @Insert(onConflict = OnConflictStrategy.ABORT)
  void materials(List<TheoryMaterial> values);

  /** Update text in place: REPLACE would delete rows referenced by reading progress. */
  @Update
  void updateTheory(TheoryMaterial value);

  @Insert(onConflict = OnConflictStrategy.ABORT)
  void questions(List<Question> values);

  @Insert(onConflict = OnConflictStrategy.ABORT)
  void options(List<AnswerOption> values);

  @Query(
      "SELECT t.id,t.subjectId,t.title,t.category,s.title AS subjectTitle,p.readAt, (SELECT"
          + " COUNT(*) FROM study_questions q WHERE q.topicId=t.id AND q.kind='QUIZ') AS"
          + " quizCount,(SELECT COUNT(*) FROM study_questions q WHERE q.topicId=t.id AND"
          + " q.kind='RECALL') AS recallCount FROM study_topics t JOIN study_subjects s ON"
          + " s.id=t.subjectId LEFT JOIN study_materials m ON m.topicId=t.id LEFT JOIN"
          + " study_material_progress p ON p.materialId=m.id ORDER BY s.position,t.position")
  LiveData<List<TopicRow>> catalog();

  @Query("SELECT * FROM study_materials WHERE topicId=:topicId")
  TheoryMaterial material(String topicId);

  @Query("SELECT * FROM study_topics WHERE id=:id")
  Topic topic(String id);

  @Query(
      "SELECT * FROM study_questions WHERE (topicId=:topicId OR topicId IN (SELECT id FROM"
          + " study_topics WHERE subjectId=:topicId)) AND kind=:kind ORDER BY position")
  List<Question> questionsFor(String topicId, String kind);

  @Query("SELECT * FROM study_options WHERE questionId=:id ORDER BY position")
  List<AnswerOption> optionsFor(String id);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void markRead(MaterialProgress value);

  @Insert
  void insertAttempt(StudyAttempt value);

  @Insert
  void insertItems(List<AttemptItem> values);

  @Query("SELECT * FROM study_attempts WHERE id=:id")
  StudyAttempt attempt(String id);

  @Query("SELECT * FROM study_attempt_items WHERE attemptId=:id ORDER BY position")
  List<AttemptItem> items(String id);

  @Query("SELECT * FROM study_attempt_items WHERE id=:id")
  AttemptItem item(String id);

  @Query(
      "UPDATE study_attempt_items SET"
          + " selectedIndex=:selected,answer=:answer,note=:note,outcome=:outcome,answeredAt=:at"
          + " WHERE id=:id AND outcome='PENDING'")
  int confirm(String id, Integer selected, String answer, String note, String outcome, long at);

  @Query(
      "UPDATE study_attempt_items SET answer=:answer,note=:note,selectedIndex=:selected WHERE"
          + " id=:id AND outcome='PENDING'")
  void draft(String id, String answer, String note, Integer selected);

  @Query(
      "UPDATE study_attempts SET status='COMPLETED',finishedAt=:at WHERE id=:id AND"
          + " status='IN_PROGRESS' AND NOT EXISTS (SELECT 1 FROM study_attempt_items WHERE"
          + " attemptId=:id AND outcome='PENDING')")
  void finish(String id, long at);

  @Query(
      "SELECT a.id,a.title,a.kind,a.status,a.startedAt,COUNT(i.id) AS total,SUM(CASE WHEN"
          + " i.outcome!='PENDING' THEN 1 ELSE 0 END) AS answered,SUM(CASE WHEN i.outcome='CORRECT'"
          + " THEN 1 ELSE 0 END) AS correct,SUM(CASE WHEN i.outcome='PARTIAL' THEN 1 ELSE 0 END) AS"
          + " partial,SUM(CASE WHEN i.outcome='WRONG' THEN 1 ELSE 0 END) AS errors,SUM(CASE WHEN"
          + " i.outcome='SKIPPED' THEN 1 ELSE 0 END) AS skipped FROM study_attempts a JOIN"
          + " study_attempt_items i ON i.attemptId=a.id GROUP BY a.id ORDER BY a.startedAt DESC")
  LiveData<List<AttemptSummary>> history();

  @Query(
      "SELECT DISTINCT q.* FROM study_questions q JOIN study_attempt_items i ON i.questionId=q.id"
          + " WHERE (q.topicId=:topicId OR q.topicId IN (SELECT id FROM study_topics WHERE"
          + " subjectId=:topicId)) AND q.kind=:kind AND i.outcome IN ('WRONG','PARTIAL','SKIPPED')"
          + " AND NOT EXISTS (SELECT 1 FROM study_attempt_items newer WHERE"
          + " newer.questionId=i.questionId AND (newer.answeredAt>i.answeredAt OR"
          + " (newer.answeredAt=i.answeredAt AND newer.rowid>i.rowid)) AND"
          + " newer.outcome!='PENDING') ORDER BY q.position")
  List<Question> reviewQuestions(String topicId, String kind);
}
