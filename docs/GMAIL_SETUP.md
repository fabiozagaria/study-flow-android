# Collegare Gmail a StudyFlow

L'app usa un account Google tramite Google Play Services e lo scope `https://www.googleapis.com/auth/gmail.readonly`. Il calendario è interno: non serve Google Calendar API e non vengono scritti eventi su Google Calendar.

## Configurazione richiesta dal proprietario del progetto

1. Apri https://console.cloud.google.com/ e crea o seleziona un progetto.
2. In **API e servizi → Libreria**, abilita **Gmail API**.
3. Configura la schermata di consenso in **Google Auth Platform**: nome StudyFlow, contatto sviluppatore e pubblico adatto al tuo account. Per prove con utenti esterni, lascia lo stato **Testing** e aggiungi gli indirizzi Gmail degli utenti di test.
4. In **Data Access / Accesso ai dati** aggiungi lo scope Gmail `gmail.readonly`.
5. In **Clients / Client** crea un **OAuth client Android**, con:
   - Package: `it.studyflow.app`
   - SHA-1 del certificato dell'APK debug consegnato: `05:27:89:2B:EA:ED:B6:A8:E1:E0:1D:39:96:2D:AB:4C:A8:5C:06:B1`
6. Attendi la propagazione della configurazione. Installa l'APK su un dispositivo con Google Play Services, apri **Calendario → Gmail → Continua** e scegli un account autorizzato nei test.

La configurazione OAuth associa il package e la firma al progetto Google Cloud. Questo flusso Android non richiede una chiave API o un client secret nel codice. Non pubblicare credenziali o keystore nel repository.

Se ricompili sul tuo computer, avrai normalmente un certificato debug diverso: crea anche il client OAuth Android per quella SHA-1. Puoi vederla con `./gradlew signingReport`. Per una release o per Play App Signing registra il certificato effettivo della versione distribuita.

Lo scope Gmail è ristretto. La pubblicazione del repository GitHub non configura né approva OAuth. La distribuzione pubblica dell'accesso Gmail può richiedere la verifica di Google e, a seconda dell'architettura e dell'utilizzo dei dati, ulteriori requisiti. Questa versione legge e analizza i messaggi sul dispositivo.

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
