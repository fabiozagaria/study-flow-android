# StudyFlow — Java + XML

<img src="docs/assets/planet.png" width="96" alt="Icona StudyFlow: pianeta minimal verde oliva" />

Planner di studio offline con Material Design 3. Interfaccia ispirata agli screenshot di Shizuku: fondo crema, accenti oliva, schede arrotondate e pulsanti a pillola. Impostazioni per tema automatico, chiaro, scuro e nero notte.

## Aprire da zero in Android Studio

1. Estrai lo ZIP e seleziona **Open**, scegliendo la cartella **StudyFlow** che contiene `settings.gradle`.
2. Installa Android SDK Platform 35 dal menu **Tools → SDK Manager**.
3. Usa JDK 17 o 21 nelle impostazioni Gradle (il JDK integrato di Android Studio va bene).
4. Il progetto include il Gradle Wrapper 8.11.1. Usa la distribuzione **Wrapper** nelle impostazioni Gradle e lascia che Android Studio scarichi le dipendenze.
5. Avvia la sincronizzazione Gradle e premi **Run** su un emulatore o dispositivo con Android 8.0 (API 26) o successivo.

Se Android Studio richiede il percorso SDK, seleziona quello installato sul tuo computer. Non serve creare un nuovo template: questo è già il progetto.

## Funzioni

- Oggi: attività non completate in scadenza oggi o in ritardo.
- Attività: creazione, modifica, eliminazione con conferma, materia, scadenza e priorità. Spunta per completare; tocca la scheda per modificare.
- Pomodoro: 25 minuti di studio, 5 di pausa, avvio, sospensione e reset. Ogni fase successiva viene avviata manualmente.
- Statistiche: minuti e sessioni complete, percentuale di attività completate.
- Calendario interno: eventi manuali e proposte da Gmail, con conferma prima del salvataggio.
- Room: attività, sessioni ed eventi sul dispositivo. Gmail richiede un account Google e una connessione.

Il timer continua in background con notifica e comandi di pausa/reset. Al termine registra la sessione e mostra un avviso. Una fase successiva si avvia manualmente; una sessione sospesa o reimpostata non aggiunge minuti. Sono presenti notifiche per completamento e scadenza delle attività. [Dettagli e comportamento Android](docs/NOTIFICATIONS_TIMER.md).
Le materie sono campi delle attività; il calendario mensile mostra attività ed eventi con indicatori e agenda. Non è presente un catalogo separato delle materie né grafici.

## Struttura

- `MainActivity.java`: navigazione, modulo attività e timer.
- `PlannerViewModel.java`: LiveData e scritture al database su un executor.
- `TaskAdapter.java`: RecyclerView.
- `Task.java`, `StudySession.java`, `StudyDao.java`, `StudyDatabase.java`: database Room.
- `res/layout`: schermata e scheda attività XML.
- `res/values/themes.xml`: tema Material 3.

## Verifica

I file XML sono stati verificati per correttezza sintattica. Compilazione `assembleDebug` e controlli `lintDebug` eseguiti con SDK 35, JDK 17 e Gradle 8.11.1. Firma APK verificata con apksigner. Non verificato su emulatore o dispositivo.
Dopo la sincronizzazione esegui `./gradlew assembleDebug lintDebug`. Prova creazione/modifica, riapertura dell'app, completamento attività, rotazione del dispositivo, sospensione/ripresa del timer e tema scuro.

## Autore

Sviluppato da **fabiozagariadev**.

- [GitHub](https://github.com/fabiozagaria)
- [LinkedIn](https://linkedin.com/in/fabiozagaria)

## Tema

Dal menu principale seleziona **Tema → Scuro**, oppure apri **Impostazioni → Tema scuro**. Le preferenze vengono salvate; **Tema nero notte** usa il fondo nero in modalità scura.

## Gmail e calendario interno

Apri **Calendario** dalla barra in basso. Tocca un giorno per visualizzare gli eventi o **+ Evento** per crearne uno. Con **Gmail** l’app analizza le email recenti e propone appuntamenti da controllare: nessun filtro da impostare, nessun salvataggio automatico.

Prima di usare Gmail configura il client OAuth Android e Gmail API: [istruzioni complete](docs/GMAIL_SETUP.md). Senza questa configurazione il calendario manuale funziona, ma l’accesso Gmail può fallire. L’importazione reale non è stata provata con un account Google nell’ambiente di sviluppo.

La versione 1.3 migra il database dalla versione precedente senza eliminare attività o sessioni. Sono inclusi test unitari per il riconoscimento delle date.

## Icona

Pianeta minimal nei colori crema e verde oliva. Icona Android adattiva con variante monocromatica per le icone a tema da Android 13. Favicon PNG e ICO disponibili in `docs/assets/`.

## Novità 1.5

Notifiche di completamento e scadenza con orario modificabile, Pomodoro in background con notifica persistente e calendario mensile con puntini e agenda delle attività. [Istruzioni](docs/NOTIFICATIONS_TIMER.md).
