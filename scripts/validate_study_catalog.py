#!/usr/bin/env python3
"""Validate content contracts and SQLite migration without Android dependencies."""
import json,re,sqlite3,collections
from pathlib import Path
root=Path(__file__).resolve().parents[1]
c=json.loads((root/'app/src/main/assets/study_catalog.json').read_text())
ids=set();prompts=set();totals=collections.Counter();subjects={s['id'] for s in c['subjects']}
def unique(value):
    assert value not in ids, f'Duplicate id: {value}'
    ids.add(value)
for t in c['topics']:
    unique(t['id']); assert t['subjectId'] in subjects
    for field in ('title','category','explanation','example','useCase','commonErrors','versionLabel'):
        assert t[field].strip(), (t['id'],field)
    assert len(t['explanation'])>600,(t['id'],'short theory')
    assert all(u.startswith('https://') for u in t['sources'])
    quiz=[q for q in t['questions'] if q['kind']=='QUIZ']
    recall=[q for q in t['questions'] if q['kind']=='RECALL']
    assert len(quiz)>=4 and len(recall)>=2
    for q in t['questions']:
        unique(q['id']); totals[(t['subjectId'],q['kind'])]+=1
        assert q['prompt'].strip() and q['explanation'].strip()
        if q['kind']=='QUIZ':
            assert q['prompt'] not in prompts, q['prompt']
            prompts.add(q['prompt'])
            assert len(q['options'])>=3 and len(set(q['options']))==len(q['options'])
            assert 0<=q['correctIndex']<len(q['options'])
        else: assert q['rubric'].strip()
# Exercise the exact SQL executed by the Room Migration.
sql=(root/'app/src/main/java/it/studyflow/app/study/data/StudyMigrations.java').read_text()
db=sqlite3.connect(':memory:');db.execute('PRAGMA foreign_keys=ON')
db.execute('CREATE TABLE tasks(id INTEGER PRIMARY KEY,title TEXT)');db.execute("INSERT INTO tasks VALUES(1,'preserved')")
for expression in re.findall(r'db\.execSQL\((.*?)\);',sql,re.S):
    statement=''.join(json.loads(literal) for literal in re.findall(r'"(?:[^"\\]|\\.)*"',expression))
    db.execute(statement)
assert db.execute('SELECT title FROM tasks WHERE id=1').fetchone()[0]=='preserved'
db.execute("INSERT INTO study_subjects VALUES('java','Java',0)")
db.execute("INSERT INTO study_topics VALUES('one','java','One','Basics',0)")
try: db.execute("INSERT INTO study_topics VALUES('bad','absent','Bad','Basics',1)")
except sqlite3.IntegrityError: pass
else: raise AssertionError('Missing foreign key enforcement')
for table in ('study_subjects','study_topics','study_materials','study_questions','study_options','study_attempts','study_attempt_items','study_material_progress'):
    assert db.execute('SELECT name FROM sqlite_master WHERE type="table" AND name=?',(table,)).fetchone()
print('Catalog and migration SQL validated:',len(c['topics']),'topics',dict(totals))
# Compare the exact migration result with Room's exported target schema.
schema_dir=root/'app/schemas/it.studyflow.app.StudyDatabase'
if (schema_dir/'3.json').exists() and (schema_dir/'4.json').exists():
    before=json.loads((schema_dir/'3.json').read_text())['database']
    after=json.loads((schema_dir/'4.json').read_text())['database']
    old={e['tableName']:e for e in before['entities']}
    new={e['tableName']:e for e in after['entities']}
    for name,entity in old.items():
        assert entity==new[name],f'Planner schema changed: {name}'
    for name,entity in new.items():
        if name in old: continue
        columns={row[1]:row for row in db.execute(f'PRAGMA table_info("{name}")')}
        for field in entity['fields']:
            actual=columns[field['columnName']]
            assert actual[2]==field['affinity'],(name,field['columnName'],'affinity')
            assert bool(actual[3])==field['notNull'],(name,field['columnName'],'nullability')
        for index in entity['indices']:
            actual=db.execute('SELECT name FROM sqlite_master WHERE type="index" AND name=?',(index['name'],)).fetchone()
            assert actual,(name,index['name'])
    print('Room schema 3/4: existing planner entities unchanged; migrated columns and indices match')
