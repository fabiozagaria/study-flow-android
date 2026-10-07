# Notifiche, timer e calendario — versione 1.5

## Uso

- Consenti le notifiche quando richiesto su Android 13 o successivo. Se le hai negate, apri **Impostazioni → Gestisci notifiche**. I canali per attività, timer in corso e fine Pomodoro si possono gestire separatamente.
- Una notifica conferma il passaggio di un'attività da da fare a completata. Modificare un'attività già completata non genera un'altra conferma.
- Ogni attività ha una data e un'ora di scadenza. Per le attività precedenti l'ora iniziale è **09:00**, modificabile nella scheda. Le attività già scadute e non completate producono un promemoria al primo avvio/aggiornamento; lo stesso avviso non viene ripetuto per la stessa scadenza.
- Completamento ed eliminazione annullano il promemoria; cambiare scadenza lo riprogramma.
- Il Pomodoro continua con app in background o schermo spento. Una notifica persistente mostra il conto alla rovescia grande nel corpo, con aggiornamento nativo ogni secondo anche in modalità compatta, e permette **Pausa** e **Reimposta**. Al termine di 25 minuti viene registrata una sessione e proposta una pausa da avviare manualmente. La fine della pausa genera un altro avviso.
- Il calendario mostra attività ed eventi: punto verde per attività, punto marrone per eventi, cerchio vuoto quando tutte le attività di quel giorno sono completate. Seleziona un giorno per vedere orari, materie, priorità, stato e note degli eventi. Puoi completare le attività dall'agenda.

## Comportamento Android

Il timer usa un servizio foreground `specialUse`, con descrizione del caso d'uso nel manifest, e un wakelock limitato alla durata residua della fase più 30 secondi. Il lock viene rilasciato con pausa, reset e fine timer. La durata usa un orologio monotono, quindi cambiare l'ora di sistema non altera il timer durante lo stesso avvio del dispositivo. È presente un allarme inexact di recupero se il servizio viene interrotto dal sistema: in questo caso l'avviso può arrivare in ritardo. Non viene richiesta l'autorizzazione per allarmi esatti.

I promemoria delle attività usano WorkManager: sopravvivono alla chiusura dell'app e vengono riprogrammati dopo riavvio, aggiornamento, cambio di fuso o ora. Sono promemoria flessibili e possono essere ritardati da Android in risparmio energetico. Il worker controlla nuovamente che l'attività esista, non sia completata e abbia la stessa scadenza.

Dopo un riavvio del dispositivo il timer viene sospeso conservando il tempo residuo, oppure completato se la scadenza è già passata. Apri l'app per riprenderlo. Un arresto forzato dell'app dalle impostazioni Android interrompe servizi e lavori finché l'app non viene riaperta; alcuni produttori possono applicare ulteriori restrizioni energetiche.

## Dati e verifica

La migrazione Room 2 → 3 aggiunge l'ora di scadenza e il riferimento dell'ultimo promemoria, senza eliminare attività, sessioni o eventi. L'identificativo della sessione del Pomodoro rimane uguale attraverso pausa e ripresa; la chiave primaria impedisce il doppio conteggio quando servizio e allarme si sovrappongono. Una sessione completata in attesa di scrittura viene recuperata al successivo avvio.

Test unitari coprono timer, pausa/ripresa, reset, cambio ora, riavvio, griglia mensile, anni bisestili, conteggi dei puntini e calcolo delle scadenze, oltre al riconoscimento email già esistente. La build e Lint vengono eseguiti prima della consegna.

Da verificare su dispositivo: autorizzazione notifiche concessa/negata, attività con scadenza a pochi minuti, completamento prima della scadenza, modifica/eliminazione, timer con schermo spento, comandi dalla notifica, riapertura, riavvio e agenda nei temi chiaro/scuro. I flussi Android reali non sono stati eseguiti nell'ambiente di sviluppo.

## Correzioni 1.6

Il timer usa due layout di notifica con un contatore Android nativo: compatto ed espanso, con caratteri più grandi. Il piccolo orario nell’intestazione è nascosto. Il calendario usa sei righe con sette celle della stessa larghezza: il numero è centrato in un cerchio di dimensione fissa e i puntini hanno una riga separata sotto. Aggiungere o togliere puntini non sposta il numero.
