# Attivare Gmail per il tuo account

**Per l’uso personale di StudyFlow non serve un abbonamento.** Google indica che l’uso standard della Gmail API è gratuito entro le quote. Questa app importa al massimo 40 email per volta. [Costi ufficiali](https://developers.google.com/workspace/gmail/api/reference/quota#pricing).

La configurazione serve per permettere a Google di riconoscere l’app che legge le tue email. Si fa una sola volta; non devi inserire password Gmail, chiavi API o file nel progetto.

## Sei passaggi

1. Apri [Google Cloud Console](https://console.cloud.google.com/) con il tuo account Google. Dal selettore in alto scegli **Nuovo progetto**, chiamalo `StudyFlow` e selezionalo.
2. Vai in **API e servizi → Libreria**, cerca **Gmail API** e premi **Abilita**.
3. Apri **Google Auth Platform → Branding** (o **Inizia**). Inserisci `StudyFlow` come nome e la tua email come contatto. In **Audience / Pubblico**, scegli utenti esterni e lascia l’app in **Testing**; aggiungi il tuo indirizzo Gmail tra gli **utenti di test**.
4. In **Data Access / Accesso ai dati** aggiungi il permesso `https://www.googleapis.com/auth/gmail.readonly`. Consente la lettura, senza modificare o inviare email.
5. In **Clients / Client → Crea client**, scegli **Android** e copia questi due valori:

   - **Nome pacchetto:** `it.studyflow.app`
   - **SHA-1:** `05:27:89:2B:EA:ED:B6:A8:E1:E0:1D:39:96:2D:AB:4C:A8:5C:06:B1`

6. Salva. Sul telefono apri **StudyFlow → Calendario → Gmail → Continua**, scegli l’account aggiunto agli utenti di test e autorizza l’accesso. La configurazione potrebbe richiedere un po’ di tempo prima di essere riconosciuta.

Se usi l’app solo personalmente, non devi avviare la verifica per una distribuzione pubblica: Google prevede eccezioni per uso personale e test. [Indicazioni di Google](https://support.google.com/cloud/answer/13464323?hl=en).

Non serve abilitare Google Calendar API: gli eventi vengono salvati nel calendario interno di StudyFlow. Non serve acquistare servizi di intelligenza artificiale: l’analisi avviene sul dispositivo.

## Se non entra

- **Codice 10:** controlla nome pacchetto e SHA-1 del client Android.
- **Accesso negato / HTTP 403:** verifica Gmail API abilitata, scope `gmail.readonly` e il tuo account nell’elenco degli utenti di test.
- Se ricompili l’app sul tuo computer, la firma debug può cambiare. Leggi la nuova SHA-1 con `./gradlew signingReport` e registra anche quella in Google Cloud. Gli APK forniti qui mantengono la firma indicata sopra.
- In modalità di test Google può richiedere nuovamente il consenso. Usa un dispositivo con Google Play Services e una connessione Internet.

Per una distribuzione Gmail a un pubblico ampio valgono requisiti diversi, inclusa l’eventuale verifica Google degli scope. Il repository pubblico GitHub non attiva Gmail da solo.

## Funzionamento

- Importazione manuale, avviata dal pulsante Gmail; nessuna sincronizzazione automatica in background.
- Analizza al massimo le 40 email più recenti della posta in arrivo degli ultimi 30 giorni. Spam, cestino e allegati vengono esclusi.
- Riconoscimento locale con regole: parole che indicano appuntamenti, date numeriche o mesi italiani e orari. Non usa un servizio AI; può ignorare alcuni messaggi o proporre eventi non pertinenti.
- Supporta date come `12/10/2026`, `2026-10-12`, `12 ottobre 2026`, e `oggi`/`domani` riferiti al giorno di ricezione. Con anno assente usa l'anno di ricezione e richiede verifica. Con più date chiede di scegliere la data prima di salvare. Le date senza ora vengono proposte come eventi di tutto il giorno.
- Dopo **Controlla proposta → Modifica e salva**, controlla titolo, data e ora. Nessun evento viene creato automaticamente.
- Evita duplicati dello stesso messaggio per account. Eliminando l'evento, il messaggio può essere proposto nuovamente; **Ignora** vale per le proposte attuali.
- Room conserva titolo, data, ora, note e identificativo di origine. Non salva il testo completo delle email o gli access token. Le anteprime restano in memoria finché la schermata è attiva; non vengono inviate a servizi AI.
- **Scollega Gmail** revoca l'accesso e cancella le proposte; gli eventi già confermati rimangono sul dispositivo.

## Verifica manuale su dispositivo

Collega un account di test. Invia un messaggio con oggetto `Esame di matematica` e testo `Il 12 novembre 2026 alle 09:30`. Importa, controlla la proposta e salvala. Importa di nuovo: lo stesso evento non deve essere proposto. Verifica una mail con più date, annullamento del consenso, assenza di rete, rotazione, revoca accesso, tema scuro e riapertura dell'app.

La configurazione Google Cloud e la prova con un account Gmail reale non sono state eseguite nell'ambiente di sviluppo.
