# Architettura Studio

## Confini

Java/XML e database `studyflow.db` esistenti. `StudyActivity` ospita `StudyBrowserFragment` e `StudySessionFragment`; `StudyViewModel` espone stato tramite LiveData; `StudyRepository` serializza I/O e scritture su executor e applica transazioni Room. `LearningDao` è separato dal DAO planner. I componenti nuovi vivono sotto `study.data` e `study.ui`; il planner non viene riscritto.

Il catalogo usa Subject, Topic, TheoryMaterial, Question e AnswerOption. MaterialProgress registra solo lettura. StudyAttempt e AttemptItem conservano tentativo, ordine e snapshot di testo/opzioni/soluzione/criteri. Non dipendono dalla permanenza della domanda originale per mostrare lo storico. Gli id sono stabili; la generazione delle opzioni è riproducibile e lo shuffle di sessione conserva l’indice corretto nello snapshot.

Le conferme aggiornano soltanto item PENDING. Il completamento richiede assenza di item pendenti. Saltata, sbagliata, parziale e corretta sono esiti distinti. La coda di ripasso guarda l’ultimo esito confermato e usa rowid per ordinare risposte con timestamp uguale. Nessuna cancellazione automatica dello storico.

Le bozze vengono salvate uscendo e nel Bundle di ricreazione; le risposte confermate sono persistite subito. In un arresto improvviso prima del salvataggio della bozza, il testo non confermato può andare perso. L’ordine e le risposte confermate restano in Room.

La navigazione riusa la MainActivity tramite CLEAR_TOP/SINGLE_TOP, conserva il percorso openTimer dalle notifiche e mostra la destinazione selezionata in Studio e Calendario. Le sessioni nascondono la barra inferiore per concentrazione; Back conserva il tentativo.

## Migrazione e test

La migrazione manuale 3→4 aggiunge otto tabelle con FK e indici, senza modificare le tre tabelle esistenti. Schemi Room esportati e versionati. Test strumentali verificano conservazione di attività, eventi e minuti; catalogo idempotente; conferma duplicata; completamento; ripasso degli errori. Test JVM coprono correzione e casi limite. Lo script Python valida catalogo, SQL migrazione e FK in SQLite.

Verifica manuale: installazione vuota e upgrade; ricerca senza risultati; font grande; temi chiaro/scuro/nero; quiz per argomento e misti; risposta, saltata, ultima domanda; autovalutazione dopo soluzione; Back, rotazione e terminazione processo; storico; coda errori; navigazione ripetuta; apertura Pomodoro da notifica.

## Incrementi futuri

Aggiornamento catalogo per revisione senza perdere letture/storico, editor/import, spaced repetition, collegamento persistente dell’argomento al Pomodoro, statistiche per materia, gestione esplicita di eliminazione dello storico. Nessun grading semantico delle risposte libere è presente.
