package it.studyflow.app.study.data;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class StudyMigrations {
  private StudyMigrations() {}

  public static final Migration FROM_3_TO_4 =
      new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_subjects (id TEXT NOT NULL PRIMARY KEY,title TEXT"
                  + " NOT NULL,position INTEGER NOT NULL)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_topics (id TEXT NOT NULL PRIMARY KEY,subjectId TEXT"
                  + " NOT NULL,title TEXT NOT NULL,category TEXT NOT NULL,position INTEGER NOT"
                  + " NULL,FOREIGN KEY(subjectId) REFERENCES study_subjects(id) ON UPDATE NO ACTION"
                  + " ON DELETE RESTRICT)");
          db.execSQL("CREATE INDEX index_study_topics_subjectId ON study_topics(subjectId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_materials (id TEXT NOT NULL PRIMARY KEY,topicId"
                  + " TEXT NOT NULL,explanation TEXT NOT NULL,example TEXT NOT NULL,useCase TEXT"
                  + " NOT NULL,commonErrors TEXT NOT NULL,sources TEXT NOT NULL,versionLabel TEXT"
                  + " NOT NULL,revision INTEGER NOT NULL,FOREIGN KEY(topicId) REFERENCES"
                  + " study_topics(id) ON UPDATE NO ACTION ON DELETE RESTRICT)");
          db.execSQL(
              "CREATE UNIQUE INDEX index_study_materials_topicId ON study_materials(topicId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_questions (id TEXT NOT NULL PRIMARY KEY,topicId"
                  + " TEXT NOT NULL,kind TEXT NOT NULL,prompt TEXT NOT NULL,explanation TEXT NOT"
                  + " NULL,rubric TEXT NOT NULL,position INTEGER NOT NULL,FOREIGN KEY(topicId)"
                  + " REFERENCES study_topics(id) ON UPDATE NO ACTION ON DELETE RESTRICT)");
          db.execSQL("CREATE INDEX index_study_questions_topicId ON study_questions(topicId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_options (id TEXT NOT NULL PRIMARY KEY,questionId"
                  + " TEXT NOT NULL,text TEXT NOT NULL,position INTEGER NOT NULL,correct INTEGER"
                  + " NOT NULL,FOREIGN KEY(questionId) REFERENCES study_questions(id) ON UPDATE NO"
                  + " ACTION ON DELETE CASCADE)");
          db.execSQL("CREATE INDEX index_study_options_questionId ON study_options(questionId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_attempts (id TEXT NOT NULL PRIMARY KEY,topicId TEXT"
                  + " NOT NULL,title TEXT NOT NULL,kind TEXT NOT NULL,status TEXT NOT"
                  + " NULL,startedAt INTEGER NOT NULL,finishedAt INTEGER)");
          db.execSQL("CREATE INDEX index_study_attempts_topicId ON study_attempts(topicId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_attempt_items (id TEXT NOT NULL PRIMARY"
                  + " KEY,attemptId TEXT NOT NULL,questionId TEXT NOT NULL,position INTEGER NOT"
                  + " NULL,prompt TEXT NOT NULL,explanation TEXT NOT NULL,rubric TEXT NOT"
                  + " NULL,options TEXT NOT NULL,correctIndex INTEGER NOT NULL,selectedIndex"
                  + " INTEGER,answer TEXT NOT NULL,note TEXT NOT NULL,outcome TEXT NOT"
                  + " NULL,answeredAt INTEGER,FOREIGN KEY(attemptId) REFERENCES study_attempts(id)"
                  + " ON UPDATE NO ACTION ON DELETE CASCADE)");
          db.execSQL(
              "CREATE UNIQUE INDEX index_study_attempt_items_attemptId_position ON"
                  + " study_attempt_items(attemptId,position)");
          db.execSQL(
              "CREATE INDEX index_study_attempt_items_questionId ON"
                  + " study_attempt_items(questionId)");
          db.execSQL(
              "CREATE TABLE IF NOT EXISTS study_material_progress (materialId TEXT NOT NULL PRIMARY"
                  + " KEY,readAt INTEGER NOT NULL,FOREIGN KEY(materialId) REFERENCES"
                  + " study_materials(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
      };
}
