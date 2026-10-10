#!/usr/bin/env python3
"""Build the bundled Italian catalog. Stable topic/question ids preserve local history.
Run from repository root: python3 scripts/build_study_catalog.py
All explanations and examples are original; official references are linked, not copied.
"""
import json, random
from pathlib import Path
TOPICS=[]
JAVA='https://docs.oracle.com/en/java/javase/21/docs/api/'
JLS='https://docs.oracle.com/javase/specs/jls/se21/html/'
BOOT='https://docs.spring.io/spring-boot/3.5/reference/'
SPRING='https://docs.spring.io/spring-framework/reference/'
SEC='https://docs.spring.io/spring-security/reference/'
def add(subject,key,title,category,explanation,example,use_case,errors,source,quiz):
    rows=[]
    for n,line in enumerate(quiz.strip().splitlines()):
        prompt,correct,b,c,reason=line.split('|')
        choices=[correct,b,c]; random.Random(key+str(n)).shuffle(choices)
        rows.append(dict(id=f'{key}-q{n+1}',kind='QUIZ',prompt=prompt,options=choices,correctIndex=choices.index(correct),explanation=reason))
    facts='\n'.join('• '+r['explanation'] for r in rows)
    rows.extend([
        dict(id=key+'-r1',kind='RECALL',prompt='Spiega '+title.lower()+': descrivi il meccanismo e un caso d’uso concreto, senza leggere la teoria.',explanation=explanation+'\n\nEsempio:\n'+example,rubric='Verifica nella tua risposta:\n'+facts),
        dict(id=key+'-r2',kind='RECALL',prompt='Analizza questo errore e proponi una correzione motivata:\n'+errors,explanation=explanation+'\n\nConfronta la soluzione con questo esempio:\n'+example,rubric='La risposta deve individuare la causa, proporre una correzione coerente e descrivere come verificarla.\n'+facts)
    ])
    TOPICS.append(dict(id=key,subjectId=subject,title=title,category=category,explanation=explanation+'\n\nPunti da verificare\n'+facts,example=example,useCase=use_case,commonErrors=errors,sources=[source],versionLabel='Java SE 21; esempi Java 17 salvo indicazione' if subject=='java' else 'Spring Boot 3.5 / Spring Framework 6; Jakarta',questions=rows))

add('java','java-runtime','JDK, bytecode e JVM','Fondamenti',
'''Il JDK contiene compilatore e strumenti; javac traduce il sorgente in bytecode. La JVM carica e verifica le classi, esegue le istruzioni e può compilare le parti frequenti in codice macchina tramite JIT. Il sistema operativo non esegue direttamente un file .java.
La compatibilità dipende dalla versione del bytecode e dalle API usate. Compilare con --release imposta il livello del linguaggio e delle API del rilascio scelto. La JVM gestisce memoria e thread, ma questo non elimina problemi di prestazioni o concorrenza.''',
'javac --release 17 Main.java\njava Main\njavap -c Main',
'Capire perché un’app compilata con un JDK recente non parte su un runtime più vecchio.',
'Confondere compilazione riuscita con compatibilità del runtime; usare API nuove pur distribuendo su una JVM vecchia.',JLS+'jls-12.html',
'''Che cosa produce normalmente javac?|Bytecode nei file .class|Un processo già in esecuzione|Soltanto codice JavaScript|Il compilatore produce classi che un runtime Java compatibile può caricare.
Quale componente può ottimizzare a runtime i metodi frequenti?|Compilatore JIT della JVM|Editor XML|Garbage collector come compilatore sorgente|Il JIT traduce bytecode caldo in codice macchina durante l’esecuzione.
A cosa serve javac --release 17?|Compilare usando linguaggio e API della release 17|Installare Java 17|Rendere compatibile qualunque libreria esterna|--release limita linguaggio e API della piattaforma; le dipendenze richiedono una verifica separata.
Quando si carica una classe, che cosa NON implica il caricamento?|Che ogni suo metodo sia già stato eseguito|Che la JVM ne conosca la definizione|Che il bytecode debba essere verificato nel linking|Caricamento, linking, inizializzazione ed esecuzione dei metodi sono fasi distinte.''')
add('java','java-types','Primitivi, wrapper e conversioni','Fondamenti',
'''I primitivi rappresentano valori come int e boolean; i wrapper sono oggetti come Integer. Boxing e unboxing convertono tra queste forme. Un riferimento Integer può essere null: convertirlo in int provoca NullPointerException.
Le conversioni numeriche possono perdere precisione. Il narrowing richiede spesso un cast esplicito, che non garantisce che il valore sia rappresentabile. L’aritmetica intera può andare in overflow senza eccezione; i confronti di wrapper con == non misurano in generale il valore.''',
'Integer optional = null;\nint safe = optional == null ? 0 : optional;\nlong total = 3_000_000_000L;\nint checked = Math.toIntExact(total); // ArithmeticException',
'Gestire valori opzionali nei DTO e prevenire overflow nei conteggi.',
'Fare unboxing di null; affidarsi alla cache degli Integer per confrontare valori; usare cast per nascondere overflow.',JLS+'jls-5.html',
'''Integer n = null; int x = n; che succede?|NullPointerException durante unboxing|x diventa 0|Errore sempre a compilazione|Unboxing richiede un oggetto non null.
Quale confronto è appropriato per due wrapper nullable?|Objects.equals(a,b)|a == b sempre|a.intValue() senza controllo|Objects.equals gestisce null e delega a equals quando necessario.
Un cast long→int garantisce l’assenza di overflow?|No|Sì|Solo se il valore è positivo|Il narrowing può scartare bit; Math.toIntExact verifica il range.
Quanto vale 5 / 2 con operandi int?|2|2.5|Non compila|La divisione intera tronca verso zero; un operando floating point cambia il calcolo.''')
add('java','java-control','Scope, operatori e controllo di flusso','Fondamenti',
'''Lo scope stabilisce dove un nome è utilizzabile; non coincide con la durata di un oggetto. Le variabili locali devono essere inizializzate prima dell’uso. && e || valutano il secondo operando solo quando necessario; & e | su boolean valutano entrambi.
I cicli descrivono ripetizioni; break termina il ciclo, continue passa all’iterazione successiva. Le switch expression restituiscono un valore e devono coprire i casi richiesti. La precedenza degli operatori va resa evidente con parentesi quando una condizione è complessa.''',
'String name = null;\nif (name != null && !name.isBlank()) {\n    System.out.println(name);\n}\nString label = switch (2) { case 1 -> "uno"; default -> "altro"; };',
'Validare input prima di dereferenziarlo e rendere comprensibili regole condizionali.',
'Usare & al posto di && in un controllo null; leggere una variabile locale non inizializzata.',JLS+'jls-14.html',
'''Perché x != null && x.isEmpty() è protetto dal null?|&& usa short circuit|isEmpty accetta null|La JVM sostituisce null con stringa vuota|Il secondo operando non viene valutato quando il primo è falso.
Una variabile locale int non inizializzata vale automaticamente 0?|No, non può essere letta prima dell’assegnazione|Sì|Solo dentro un metodo static|L’inizializzazione predefinita dei campi non si applica alle variabili locali.
Che cosa fa continue in un for?|Passa alla prossima iterazione|Termina sempre il metodo|Termina tutti i cicli annidati|continue salta il resto del corpo corrente e prosegue il ciclo.
Una switch expression deve produrre un risultato per i casi ammessi?|Sì|No, ritorna null automaticamente|Solo per String|Una switch expression deve essere esaustiva rispetto al suo dominio.''')
add('java','java-methods','Metodi, varargs e passaggio per valore','Fondamenti',
'''Java passa sempre gli argomenti per valore. Con un oggetto, il valore copiato è il riferimento: il metodo può mutare l’oggetto condiviso, ma riassegnare il parametro non cambia la variabile del chiamante.
Un metodo ha nome, parametri e tipo di ritorno; l’overload si basa sulle liste di parametri, non sul solo ritorno. I varargs sono esposti al metodo come array e devono essere l’ultimo parametro. Il metodo dovrebbe comunicare chiaramente se modifica gli argomenti.''',
'void rename(List<String> values) {\n    values.add("Java"); // modifica l’oggetto condiviso\n    values = new ArrayList<>(); // riassegna solo il parametro\n}',
'Comprendere effetti collaterali nei service e scegliere API prevedibili.',
'Dire che Java passa oggetti per riferimento; aspettarsi che riassegnare un parametro sostituisca l’oggetto del chiamante.',JLS+'jls-8.html',
'''Java passa gli argomenti in quale modo?|Sempre per valore|Sempre per riferimento|Per riferimento solo le List|Anche il riferimento a un oggetto viene copiato come valore.
Riassegnare un parametro oggetto cambia la variabile del chiamante?|No|Sì sempre|Solo se il parametro è final|La riassegnazione agisce sulla variabile locale del metodo.
Due metodi possono differire soltanto nel tipo di ritorno?|No|Sì|Solo quando sono static|Il solo ritorno non distingue firme sovraccaricate.
Dove deve trovarsi un parametro varargs?|Alla fine della lista parametri|All’inizio|In qualsiasi posizione|I varargs rappresentano l’ultimo parametro e ricevono un array.''')
add('java','java-strings','String, immutabilità e text block','Fondamenti',
'''String è immutabile: operazioni come trim e replace producono un nuovo valore quando necessario. Il pool delle stringhe non rende == un confronto di contenuto. StringBuilder serve per costruire testo mutabile, soprattutto in cicli.
Le stringhe Java usano unità UTF-16: length non è il numero di caratteri percepiti dall’utente. I text block rendono leggibile testo multilinea senza cambiare il tipo String. Non includere password o token nei testi di log.''',
'String raw = " Java ";\nString normalized = raw.strip();\nboolean same = "Java".equals(normalized);\nStringBuilder b = new StringBuilder();\nb.append("Materia: ").append(normalized);',
'Normalizzare input, costruire messaggi e confrontare identificativi testuali.',
'Chiamare trim senza usare il risultato; confrontare stringhe con ==; assumere che length conti emoji complete.',JAVA+'java.base/java/lang/String.html',
'''String s=" a "; s.trim(); quale valore ha s?|Ancora " a "|"a"|null|String è immutabile: occorre usare il valore restituito da trim.
Per confrontare il contenuto di due String si usa normalmente?|equals|==|hashCode da solo|equals confronta il contenuto, == l’identità dei riferimenti.
Per molte concatenazioni in un ciclo quale struttura è adatta?|StringBuilder|Integer|ThreadLocal obbligatorio|StringBuilder accumula testo in un buffer mutabile.
String.length() misura cosa?|Unità UTF-16|Sempre caratteri visibili|Byte UTF-8|Un carattere percepito può occupare più unità UTF-16.''')
add('java','java-oop','Classi, costruttori e incapsulamento','OOP',
'''Una classe definisce stato e comportamento. Il costruttore inizializza un’istanza valida; se dichiari un costruttore, il compilatore non aggiunge automaticamente quello senza parametri. Incapsulare significa controllare gli invarianti attraverso un’API, non soltanto generare getter e setter.
Campi private e metodi con responsabilità chiare impediscono transizioni incoerenti. static appartiene alla classe, mentre i campi d’istanza appartengono agli oggetti. final impedisce la riassegnazione di un riferimento, ma non rende immutabile l’oggetto referenziato.''',
'class Counter {\n    private int value;\n    Counter(int initial) {\n        if (initial < 0) throw new IllegalArgumentException();\n        value = initial;\n    }\n    void increment() { value++; }\n    int value() { return value; }\n}',
'Proteggere regole di dominio, come saldo non negativo e transizioni di stato.',
'Esporre campi mutabili pubblici; usare setter che permettono stati invalidi; confondere final con immutabilità profonda.',JLS+'jls-8.html',
'''Dichiarando soltanto Counter(int), esiste automaticamente Counter()?|No|Sì|Solo se la classe è public|Il costruttore di default viene generato solo quando non ne dichiari alcuno.
Un riferimento final a una ArrayList impedisce add()?|No|Sì|Solo fuori dal costruttore|final impedisce riassegnazione, non mutazione dell’oggetto.
Qual è lo scopo dell’incapsulamento?|Proteggere invarianti tramite un’API controllata|Aggiungere tutti i setter|Nascondere ogni metodo|L’API dovrebbe permettere soltanto operazioni coerenti col dominio.
Un campo static è associato a cosa?|Alla classe|A ogni singola istanza separatamente|Alla variabile locale del chiamante|Lo stato static è condiviso dalle istanze della classe nel relativo contesto di caricamento.''')
add('java','java-inheritance','Ereditarietà, override e polimorfismo','OOP',
'''L’ereditarietà modella una relazione is-a. L’override ridefinisce un metodo d’istanza ereditato e il dispatch sceglie l’implementazione in base al tipo reale dell’oggetto. L’overload sceglie la firma a compilazione usando i tipi disponibili nel punto di chiamata.
Un override non può restringere la visibilità né ampliare arbitrariamente le checked exception. I metodi static sono nascosti, non sovrascritti con dispatch dinamico. Preferisci composizione quando vuoi riutilizzare un comportamento senza una vera relazione di sottotipo.''',
'class Animal { String sound() { return "?"; } }\nclass Dog extends Animal {\n    @Override String sound() { return "bau"; }\n}\nAnimal a = new Dog();\nSystem.out.println(a.sound()); // bau',
'Usare implementazioni intercambiabili senza dipendere dalla classe concreta.',
'Confondere overload e override; restringere public a private in un override; usare ereditarietà solo per riutilizzare codice.',JLS+'jls-8.html#jls-8.4.8',
'''Animal a=new Dog(); a.sound() usa quale override?|Quello del tipo reale Dog|Sempre Animal|Quello scelto dal nome della variabile|Il dispatch dei metodi d’istanza usa il tipo runtime.
Quale scelta riguarda l’overload?|Selezione della firma a compilazione|Selezione sempre a runtime|Sostituzione dei costruttori ereditati|L’overload viene risolto dai tipi e dalle conversioni applicabili a compilazione.
Si può ridurre public a protected in un override?|No|Sì|Solo con @Override|Un override non può restringere l’accessibilità del contratto ereditato.
Un metodo static partecipa al dispatch dinamico come un metodo d’istanza?|No|Sì|Solo con una sottoclasse final|I metodi static possono essere nascosti; non hanno lo stesso dispatch polimorfico.''')
add('java','java-contracts','Interfacce, classi astratte e composizione','OOP',
'''Un’interfaccia descrive un contratto; può contenere metodi astratti, default, static e privati. Una classe può implementare più interfacce, ma estende una sola classe. Una classe astratta può conservare stato e costruttori condivisi, senza essere istanziabile direttamente.
La composizione delega un comportamento a un collaboratore e facilita sostituzione e test. Un contratto utile specifica anche precondizioni, risultati ed errori. Dipendere da un’interfaccia non basta se le implementazioni violano le aspettative del chiamante.''',
'interface Sender { void send(String text); }\nclass Reminder {\n    private final Sender sender;\n    Reminder(Sender sender) { this.sender = sender; }\n    void notifyUser() { sender.send("Ripassa Java"); }\n}',
'Sostituire invio email con un fake nei test e separare dominio da infrastruttura.',
'Creare una gerarchia rigida per ogni variante; credere che un’interfaccia possa contenere soltanto metodi astratti.',JLS+'jls-9.html',
'''Una classe Java può implementare più interfacce?|Sì|No|Solo se non ha un costruttore|Java ammette più contratti implementati da una classe.
Una classe astratta può avere un costruttore?|Sì|No|Solo se è final|Il costruttore astratto inizializza la parte comune delle istanze concrete.
Un metodo default di interfaccia può contenere codice?|Sì|No|Solo bytecode scritto a mano|default fornisce un’implementazione ereditabile dai tipi che implementano l’interfaccia.
Quando è utile la composizione?|Quando vuoi delegare e sostituire un comportamento|Solo per ottenere ereditarietà multipla di classi|Quando non esistono dipendenze|La composizione mantiene collaboratori sostituibili senza imporre una relazione is-a.''')
add('java','java-equality','equals, hashCode e identità','OOP',
'''== confronta valori primitivi oppure identità dei riferimenti. equals definisce l’uguaglianza logica secondo il contratto del tipo. Se due oggetti sono uguali secondo equals devono avere lo stesso hashCode; il contrario non è garantito.
Le collezioni hash usano hashCode per individuare candidati e equals per distinguerli. Modificare i campi usati nell’hash mentre una chiave è nella mappa può renderla non ritrovabile. L’uguaglianza dovrebbe essere riflessiva, simmetrica, transitiva e coerente.''',
'record Email(String value) {}\nMap<Email,String> users = new HashMap<>();\nusers.put(new Email("a@example.org"), "Fabio");\nString name = users.get(new Email("a@example.org"));',
'Usare value object come chiavi senza perdere elementi o duplicare valori logici.',
'Ridefinire equals senza hashCode; modificare una chiave mutabile già inserita in HashMap.',JAVA+'java.base/java/lang/Object.html',
'''Due oggetti equals devono avere hashCode uguale?|Sì|No|Solo se sono la stessa istanza|È un requisito del contratto hashCode.
HashCode uguale implica equals vero?|No|Sì|Solo in HashSet|Le collisioni sono possibili e vengono distinte tramite equals.
Che cosa confronta == tra due riferimenti?|Identità del riferimento|Sempre tutti i campi|Il risultato di toString|== verifica se i riferimenti indicano lo stesso oggetto o sono entrambi null.
Perché una chiave mutata può non essere trovata in HashMap?|Può cambiare il suo hash logico dopo l’inserimento|HashMap converte le chiavi in null|Il GC elimina chiavi ancora referenziate|Le chiavi devono mantenere stabili i campi rilevanti per equals e hashCode.''')
add('java','java-records','Record, enum e sealed class','OOP',
'''Un record descrive dati tramite componenti e genera accessori, equals, hashCode e toString. L’immutabilità è superficiale: un componente List può ancora essere mutabile se non ne fai una copia. Un costruttore compatto può validare i dati.
Un enum rappresenta un insieme chiuso di costanti e può contenere comportamento. Una sealed class limita i sottotipi consentiti; i sottotipi devono rispettare le regole final, sealed o non-sealed. Questi strumenti rendono espliciti stati e alternative del dominio.''',
'record StudyPlan(List<String> topics) {\n    StudyPlan { topics = List.copyOf(topics); }\n}\nenum Status { OPEN, COMPLETED }\nsealed interface Result permits Success, Failure {}\nrecord Success(String value) implements Result {}\nrecord Failure(String message) implements Result {}',
'Modellare DTO, stati e risultati evitando stringhe arbitrarie.',
'Credere che un record renda immutabili le collezioni interne; persistere enum per ordinal e poi riordinare le costanti.',JLS+'jls-8.html',
'''L’immutabilità di un record è automaticamente profonda?|No|Sì|Solo se ha due componenti|I riferimenti dei componenti non sono riassegnabili, ma gli oggetti interni possono essere mutabili.
Quale accessorio genera record User(String name)?|name()|getName() obbligatorio|name(int)|Gli accessori generati hanno il nome del componente.
Per persistere enum in modo leggibile è generalmente preferibile?|Un nome stabile rispetto a ordinal|Sempre ordinal|Il suo indirizzo in memoria|ordinal dipende dall’ordine e può cambiare significato quando riordini le costanti.
A cosa serve sealed?|Limitare i sottotipi ammessi|Impedire qualsiasi istanza|Rendere ogni campo static|sealed rende esplicito il gruppo di implementazioni consentite.''')
add('java','java-exceptions','Eccezioni e gestione delle risorse','Robustezza',
'''Le checked exception devono essere catturate o dichiarate; RuntimeException e i suoi sottotipi sono unchecked. Error indica in genere problemi gravi del runtime. Catturare un’eccezione significa scegliere un recupero sensato o tradurla preservando la causa.
Il try-with-resources chiude gli AutoCloseable in ordine inverso. Se corpo e chiusura falliscono, l’eccezione di chiusura può diventare suppressed. Non usare catch vuoti: nascondono guasti, rendono difficile il debug e possono lasciare stato incoerente.''',
'try (BufferedReader reader = Files.newBufferedReader(path)) {\n    return reader.readLine();\n} catch (IOException ex) {\n    throw new UncheckedIOException("Lettura non riuscita", ex);\n}',
'Gestire file, JDBC e traduzione degli errori tra livelli dell’applicazione.',
'Fare catch(Exception) e ignorare il guasto; perdere la causa; chiudere risorse soltanto nel percorso di successo.',JLS+'jls-11.html',
'''Una IOException è normalmente checked?|Sì|No|Solo quando arriva dalla rete|IOException richiede gestione o dichiarazione nel contratto del metodo.
Quale requisito serve al try-with-resources?|Una risorsa AutoCloseable|Una risorsa Runnable|Una classe necessariamente final|Il linguaggio invoca close sulle risorse AutoCloseable.
In che ordine vengono chiuse più risorse?|In ordine inverso alla dichiarazione|In ordine casuale|Mai se il corpo lancia un’eccezione|Il try-with-resources chiude le risorse dall’ultima alla prima.
Perché preservare la causa in una nuova eccezione?|Per conservare l’origine del guasto|Per evitare sempre il rollback|Per ignorare lo stack trace|La catena delle cause rende diagnostica la traduzione tra livelli.''')
add('java','java-generics','Generics, wildcard e type erasure','Collezioni',
'''I generics esprimono vincoli di tipo a compilazione. List<Integer> non è un sottotipo di List<Number>: consentirlo permetterebbe inserimenti incompatibili. Una wildcard extends è utile per leggere un produttore; super per scrivere in un consumatore.
La type erasure rimuove gran parte dei parametri di tipo dal runtime. Non puoi creare normalmente new T() o new List<String>[10]. I raw type eludono verifiche e possono spostare gli errori al runtime. PECS è un criterio pratico, non una regola che sostituisce il ragionamento sull’API.''',
'void copy(List<? extends Number> source, List<? super Number> target) {\n    for (Number n : source) target.add(n);\n}',
'Progettare utility type-safe senza cast fragili.',
'Assegnare List<Integer> a List<Number>; usare raw List; scrivere un Integer in List<? extends Number> senza conoscere il tipo concreto.',JLS+'jls-4.html',
'''List<Integer> è assegnabile direttamente a List<Number>?|No|Sì|Solo se non contiene elementi|I tipi generici sono invarianti rispetto a questa relazione.
Da List<? extends Number> puoi leggere un elemento come?|Number|Sempre Integer|Sempre Double|Il limite superiore garantisce che ogni elemento non null sia un Number.
List<? super Integer> permette di aggiungere Integer?|Sì|No|Solo null|La wildcard super identifica un consumatore compatibile con Integer.
La type erasure permette normalmente new T()?|No|Sì|Solo dentro un metodo static|Il parametro di tipo non fornisce un costruttore runtime istanziabile come T.''')
add('java','java-lists','List, Set, Map e complessità','Collezioni',
'''List conserva una sequenza e ammette duplicati. Set rappresenta unicità secondo la propria semantica di uguaglianza. Map associa chiavi a valori e non è una Collection. ArrayList offre accesso indicizzato efficiente; inserire al centro richiede spostamenti.
HashMap offre operazioni mediamente efficienti ma non un ordine di iterazione garantito. LinkedHashMap mantiene un ordine definito; TreeMap ordina secondo un confronto. La struttura va scelta in base alle operazioni reali, non al nome o a una supposizione generica di velocità.''',
'List<String> ordered = new ArrayList<>(List.of("Java", "Java"));\nSet<String> unique = new HashSet<>(ordered);\nMap<String,Integer> scores = new LinkedHashMap<>();\nscores.put("Java", 80);',
'Organizzare risultati, deduplicare codici e indicizzare oggetti per id.',
'Aspettarsi ordine stabile da HashMap; scegliere LinkedList per accessi ripetuti per indice; usare una List per ricerche frequenti per chiave.',JAVA+'java.base/java/util/Collection.html',
'''Quale struttura associa chiavi uniche a valori?|Map|List|Queue soltanto|Map modella associazioni chiave-valore e non estende Collection.
HashMap garantisce ordine d’inserimento?|No|Sì|Solo con chiavi String|Per ordine d’inserimento si può usare LinkedHashMap.
ArrayList.get(index) ha complessità tipica?|O(1)|O(n) sempre|O(n²)|ArrayList usa un array per l’accesso diretto all’indice.
Che cosa distingue normalmente Set da List?|Unicità degli elementi|Immutabilità obbligatoria|Memoria sempre minore|Set evita duplicati secondo la semantica della sua implementazione.''')
add('java','java-ordering','Comparator, ordinamento e collezioni immutabili','Collezioni',
'''Comparable definisce un ordine naturale; Comparator rappresenta un criterio esterno. Un confronto deve essere coerente e transitivo. Evita sottrazioni tra int per confrontare: possono andare in overflow.
List.of e List.copyOf producono liste non modificabili e rifiutano null. Collections.unmodifiableList crea una vista che può riflettere modifiche alla lista sottostante. Non modificabile non significa che gli elementi siano immutabili. Nei TreeSet il confronto a zero determina l’equivalenza per la struttura.''',
'Comparator<User> order = Comparator.comparing(User::name)\n    .thenComparingInt(User::age);\nList<User> snapshot = List.copyOf(users);\nint comparison = Integer.compare(a, b);',
'Creare ordinamenti riproducibili e proteggere snapshot consegnati ai chiamanti.',
'Usare a-b come comparator; confondere vista non modificabile con copia; ignorare che TreeSet può eliminare elementi con confronto zero.',JAVA+'java.base/java/util/Comparator.html',
'''Per confrontare int senza overflow è preferibile?|Integer.compare(a,b)|a-b sempre|Convertire sempre in byte|Integer.compare evita overflow della sottrazione.
List.of("a",null) che comportamento ha?|Lancia NullPointerException|Crea una lista con null|Ignora null|Le factory List.of rifiutano elementi null.
Una vista unmodifiableList può riflettere modifiche all’origine?|Sì|No, è sempre una copia|Solo se ha zero elementi|La vista vieta mutazioni attraverso di sé ma osserva la collezione sottostante.
In TreeSet due elementi con confronto zero vengono trattati come?|Equivalenti per il set|Sempre distinti se equals è false|Entrambi null|TreeSet usa l’ordinamento per l’unicità, che va progettato coerentemente.''')
add('java','java-lambdas','Lambda e interfacce funzionali','Programmazione funzionale',
'''Una lambda implementa un’interfaccia funzionale con un solo metodo astratto. Predicate testa una condizione, Function trasforma un valore, Consumer esegue un’azione e Supplier produce un valore.
Le variabili locali catturate devono essere final o effectively final; un oggetto catturato può comunque essere mutabile. I method reference sono una sintassi alternativa quando una chiamata esistente corrisponde al contratto. Una lambda non rende automaticamente il codice puro o thread-safe.''',
'Predicate<String> valid = s -> s != null && !s.isBlank();\nFunction<String,Integer> size = String::length;\nList<String> selected = names.stream().filter(valid).toList();',
'Passare filtri e trasformazioni a pipeline e API configurabili.',
'Catturare una variabile locale e poi riassegnarla; nascondere effetti collaterali dentro lambda usate in parallelo.',JLS+'jls-15.html#jls-15.27',
'''Quale contratto ha Predicate<T>?|T→boolean|T→T obbligatorio|Nessun parametro e void|Predicate rappresenta un test booleano su un valore.
Una variabile locale catturata può essere riassegnata dopo?|No se perde effectively final|Sì sempre|Solo se è int|La cattura richiede final o effectively final.
Function<T,R> rappresenta cosa?|Trasformazione T→R|Azione senza ritorno|Creazione obbligatoria di thread|Function mappa un input a un risultato.
Un’interfaccia funzionale può avere metodi default aggiuntivi?|Sì|No|Solo se è privata|Il requisito riguarda un solo metodo astratto funzionale, non i default.''')
add('java','java-streams','Stream, pipeline e collect','Programmazione funzionale',
'''Uno Stream descrive una pipeline su dati, non una nuova collezione. Le operazioni intermedie sono lazy e un’operazione terminale avvia la traversata. filter seleziona, map trasforma, flatMap appiattisce più risultati. Uno stream consumato non va riutilizzato.
I collector aggregano risultati. Stream.toList restituisce una lista non modificabile; Collectors.toList non garantisce una particolare implementazione. Per operazioni parallele occorrono funzioni non interferenti e riduzioni associative. Il parallelismo non è una scorciatoia universale alle prestazioni.''',
'List<String> result = users.stream()\n    .filter(User::active)\n    .map(User::name)\n    .sorted()\n    .toList();',
'Trasformare dati in modo dichiarativo mantenendo chiari i passaggi.',
'Usare map per effetti collaterali; riutilizzare uno Stream dopo count; assumere che parallelStream sia sempre più veloce.',JAVA+'java.base/java/util/stream/Stream.html',
'''Una filter da sola avvia sempre la traversata?|No|Sì|Solo su ArrayList|Le operazioni intermedie sono lazy e attendono un’operazione terminale.
Che cosa fa flatMap?|Appiattisce stream di risultati|Ordina sempre|Rimuove ogni duplicato|flatMap mappa e appiattisce sorgenti multiple in una pipeline.
Si può normalmente riutilizzare uno Stream dopo count()?|No|Sì|Solo se count ritorna 0|Una pipeline consumata va ricreata dalla sorgente.
La lista restituita da Stream.toList() è modificabile?|No|Sì sempre|Solo se contiene record|Stream.toList produce una lista non modificabile.''')
add('java','java-optional','Optional e assenza di valore','Programmazione funzionale',
'''Optional esplicita che un risultato può mancare. map trasforma quando è presente, flatMap evita Optional annidati e orElseGet calcola il fallback soltanto quando necessario. Optional.of rifiuta null; ofNullable lo converte in assenza.
Usalo soprattutto nei ritorni dove l’assenza è parte del contratto. Non chiamare get senza una garanzia di presenza. Optional non sostituisce la validazione e un Optional dovrebbe a sua volta non essere null. Nei modelli persistenti o serializzati va valutata la compatibilità del framework.''',
'User user = repository.find(id)\n    .orElseThrow(() -> new UserNotFound(id));\nString label = Optional.ofNullable(name).map(String::strip)\n    .filter(s -> !s.isEmpty()).orElse("Anonimo");',
'Esplicitare ricerche che possono non trovare un risultato.',
'Usare Optional.get senza controllo; restituire null al posto di Optional.empty; chiamare un fallback costoso tramite orElse.',JAVA+'java.base/java/util/Optional.html',
'''Optional.of(null) produce cosa?|NullPointerException|Optional.empty|Un Optional presente con null|of richiede un valore non null.
Quale fallback è lazy?|orElseGet(supplier)|orElse(expensive())|get()|orElseGet invoca il supplier soltanto quando il valore manca.
Quando è utile flatMap su Optional?|Quando la funzione restituisce già Optional|Per ordinare numeri|Per forzare un valore presente|flatMap evita Optional<Optional<T>>.
Un metodo che dichiara Optional dovrebbe restituire null?|No|Sì per assenza|Solo su errori di rete|L’assenza deve essere rappresentata da Optional.empty.''')
add('java','java-time','Date, orari e fusi','API e I/O',
'''LocalDate rappresenta una data senza fuso; LocalDateTime una data e ora locale; Instant un punto sulla linea temporale. ZonedDateTime collega data e ora a un fuso con le sue regole. Duration misura tempo trascorso, Period differenze calendariali.
Usa Clock iniettato per test riproducibili. Un giorno locale non dura necessariamente 24 ore per via dell’ora legale. Una data locale da sola non identifica un istante. Per memorizzare eventi globali scegli un riferimento temporale esplicito e conserva il fuso quando serve alla regola di dominio.''',
'Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);\nLocalDate today = LocalDate.now(clock);\nZonedDateTime rome = Instant.now(clock).atZone(ZoneId.of("Europe/Rome"));',
'Gestire scadenze, appuntamenti e statistiche giornaliere senza errori di fuso.',
'Usare LocalDateTime come istante universale; assumere che ogni giorno duri 24 ore; usare l’orologio reale nei test.',JAVA+'java.base/java/time/package-summary.html',
'''Quale tipo rappresenta un istante globale?|Instant|LocalDate|LocalTime|Instant identifica un punto sulla linea temporale.
LocalDateTime contiene un fuso orario?|No|Sì sempre UTC|Sì quello del dispositivo|Serve un offset o un fuso per collegarlo a un istante.
Per testare il tempo in modo deterministico cosa conviene iniettare?|Clock|Thread.sleep|StringBuilder|Clock permette di controllare il tempo senza attese reali.
Un giorno in un fuso con ora legale dura sempre 24 ore?|No|Sì|Solo se è lunedì|Le transizioni di ora legale possono alterare la durata del giorno locale.''')
add('java','java-io','File, NIO e charset','API e I/O',
'''Path identifica un percorso e Files offre operazioni su file e directory. I byte non diventano testo senza una codifica: scegli un charset esplicito. Stream di file e directory devono essere chiusi quando possiedono risorse del sistema.
Leggere tutto un file in memoria è semplice ma può essere inadatto a file grandi. I percorsi forniti dall’utente vanno risolti dentro una directory consentita: normalizzare non basta a gestire tutti i casi di link simbolici. Controlla limiti di dimensione e permessi, non solo l’estensione.''',
'String text = Files.readString(path, StandardCharsets.UTF_8);\ntry (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {\n    long count = lines.filter(s -> !s.isBlank()).count();\n}',
'Importare cataloghi e leggere configurazioni senza dipendere dal charset della macchina.',
'Non chiudere Files.lines; leggere file enormi in una sola stringa; concatenare input utente in un percorso senza controlli.',JAVA+'java.base/java/nio/file/Files.html',
'''Perché scegliere UTF-8 esplicitamente?|Per decodificare i byte in modo prevedibile|Per cifrare automaticamente|Per rendere il file immutabile|Un charset esplicito evita dipendenze dal default dell’ambiente.
Lo Stream di Files.lines va chiuso?|Sì|No|Solo se è vuoto|Lo stream mantiene una risorsa di I/O e va gestito con try-with-resources.
Files.readString è sempre adatto a file enormi?|No|Sì|Solo per JSON|Carica il contenuto in memoria e richiede valutare dimensioni e limiti.
Path.normalize() elimina da solo ogni rischio di traversal con symlink?|No|Sì|Solo su Linux|La validazione deve considerare la directory consentita e la risoluzione reale dei link.''')
add('java','java-jdbc','JDBC, PreparedStatement e transazioni','API e I/O',
'''JDBC espone Connection, PreparedStatement e ResultSet. I parametri di un PreparedStatement rappresentano valori e separano dati da SQL; non sostituiscono nomi di tabella o colonne. Le risorse vanno chiuse e le connessioni possono provenire da un pool.
Con autoCommit disabilitato, commit conferma e rollback annulla la transazione. Una transazione deve includere l’intera operazione logica. I binding prevengono SQL injection sui valori, ma SQL dinamico e ordinamenti richiedono allowlist. Non costruire query concatenando input esterno.''',
'connection.setAutoCommit(false);\ntry (PreparedStatement ps = connection.prepareStatement("UPDATE users SET name=? WHERE id=?")) {\n    ps.setString(1, name); ps.setLong(2, id);\n    ps.executeUpdate(); connection.commit();\n} catch (SQLException ex) { connection.rollback(); throw ex; }',
'Eseguire accesso relazionale esplicito e riconoscere ciò che un ORM automatizza.',
'Concatenare input SQL; dimenticare rollback; usare un placeholder come nome di colonna.',JAVA+'java.sql/java/sql/PreparedStatement.html',
'''I placeholder di PreparedStatement sostituiscono normalmente cosa?|Valori|Nomi di tabella arbitrari|Parole chiave SQL|I parametri rappresentano dati, non la struttura della query.
Con autoCommit false, che cosa conferma la transazione?|commit()|flush() JDBC obbligatorio|close() come regola affidabile|commit rende definitive le modifiche della transazione secondo le garanzie del database.
Concatenare user input in SQL è sicuro se poi usi PreparedStatement?|No|Sì|Solo per String|La separazione vale per i valori realmente passati tramite binding.
Chi gestisce ResultSet e statement quando non servono più?|Il codice tramite chiusura delle risorse|Sempre il GC subito|Soltanto il database remoto|Il try-with-resources rende esplicita e tempestiva la chiusura.''')
add('java','java-memory','Stack, heap, riferimenti e garbage collection','JVM',
'''Ogni thread ha uno stack di esecuzione con frame dei metodi; l’heap è condiviso ed è il modello principale per gli oggetti. La JVM può ottimizzare le allocazioni: evita di trasformare stack e heap in una regola fisica assoluta su ogni valore.
Il GC recupera oggetti non raggiungibili dalle radici, non semplicemente quelli usciti da uno scope. Una collezione statica può trattenere oggetti non più utili e produrre un leak logico. Il GC non chiude automaticamente in modo affidabile file e connessioni. StackOverflowError e OutOfMemoryError indicano problemi distinti.''',
'List<byte[]> retained = new ArrayList<>();\n// Se questa lista resta raggiungibile, conserva tutti gli array aggiunti.\nretained.add(new byte[1024]);\nretained.clear(); // rimuove questi riferimenti, non forza immediatamente il GC',
'Diagnosticare ritenzione di memoria, ricorsione senza limite e crescita delle cache.',
'Credere che uscire dal metodo liberi subito ogni oggetto; affidarsi al GC per chiudere connessioni.', 'https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html',
'''Quando un oggetto diventa candidato al GC?|Quando non è più raggiungibile dalle radici|Quando termina qualunque metodo|Quando diventa final|La raggiungibilità determina se il runtime può recuperarlo.
Una lista statica può trattenere oggetti non più utili?|Sì|No, il GC ignora static|Solo se contiene String|Riferimenti raggiungibili possono mantenere vivo un intero grafo di oggetti.
Il GC sostituisce try-with-resources per file e connessioni?|No|Sì|Solo con Java 21|Le risorse esterne richiedono gestione esplicita.
La ricorsione troppo profonda può causare quale errore?|StackOverflowError|Sempre SQLException|ClassNotFoundException|Ogni chiamata usa un frame e una profondità eccessiva può esaurire lo stack.''')
add('java','java-threads','Thread, Runnable e ciclo di vita','Concorrenza',
'''Runnable descrive un lavoro; Thread fornisce un contesto di esecuzione. start avvia un nuovo thread che esegue run; chiamare run direttamente esegue sul thread chiamante. Un Thread può essere avviato una sola volta.
join attende il completamento. InterruptedException è un segnale cooperativo: gestiscilo o ripristina il flag quando non puoi propagarlo. Non affidare l’ordine tra thread a sleep. Stato condiviso e accesso concorrente richiedono coordinamento; creare thread non rende automaticamente sicuro il codice.''',
'Thread worker = new Thread(() -> System.out.println("Lavoro"));\nworker.start();\nworker.join();',
'Capire background task e distinguere avvio concorrente da una normale chiamata.',
'Chiamare run aspettandosi parallelismo; avviare due volte lo stesso Thread; ignorare l’interruzione.',JAVA+'java.base/java/lang/Thread.html',
'''new Thread(task).run() avvia un nuovo thread?|No|Sì|Solo con Runnable|run chiamato direttamente esegue nel thread corrente.
Quale metodo avvia un Thread?|start()|join()|sleep()|start chiede al runtime di avviare il contesto di esecuzione.
Si può chiamare start due volte sullo stesso Thread?|No|Sì dopo join|Solo se il task è breve|Un Thread non è riavviabile.
Che cosa fa join()?|Attende il completamento del thread|Rende atomiche tutte le variabili|Annulla sempre il task|join coordina il completamento del thread prima di proseguire.''')
add('java','java-locks','synchronized, volatile e atomicità','Concorrenza',
'''synchronized protegge una sezione usando un monitor: soltanto un thread alla volta può possedere quel monitor. Il rilascio e la successiva acquisizione creano garanzie di visibilità. Occorre usare lo stesso lock per proteggere lo stesso stato.
volatile garantisce visibilità e ordinamento pertinenti, ma non rende atomiche operazioni composte come count++. AtomicInteger offre incrementi atomici. Più lock acquisiti in ordini diversi possono causare deadlock. Blocchi sincronizzati lunghi o con I/O riducono la concorrenza.''',
'private int count;\nsynchronized void increment() { count++; }\nsynchronized int value() { return count; }\nAtomicInteger atomic = new AtomicInteger();\natomic.incrementAndGet();',
'Proteggere contatori e invarianti condivisi senza confondere visibilità e atomicità.',
'Dichiarare volatile un contatore e usare ++ da più thread; sincronizzare su oggetti diversi per lo stesso dato.',JLS+'jls-17.html',
'''volatile int count; count++ è atomico?|No|Sì|Solo su CPU a 64 bit|L’incremento è una sequenza di lettura, calcolo e scrittura.
Due blocchi synchronized su lock diversi si escludono a vicenda?|No|Sì|Solo se appartengono alla stessa classe|La mutua esclusione dipende dallo stesso monitor.
Quale operazione incrementa atomicamente un AtomicInteger?|incrementAndGet()|get()+1 poi set()|toString()|L’operazione atomica include lettura e aggiornamento come un’unica azione.
Un rischio di acquisire più lock in ordine incoerente è?|Deadlock|Type erasure|Unboxing|Due thread possono attendere reciprocamente lock posseduti dall’altro.''')
add('java','java-executors','Executor, Future e CompletableFuture','Concorrenza',
'''Executor separa il task dalla politica di esecuzione. Un pool riutilizza thread, mentre Future espone completamento, cancellazione e risultato. get può bloccare il chiamante. Il proprietario dell’executor deve definirne la chiusura.
CompletableFuture compone operazioni: thenApply trasforma un risultato, thenCompose collega una nuova operazione asincrona. Le varianti async usano un executor esplicito o un default che va valutato. Gestisci eccezioni, timeout e limiti di concorrenza; non saturare un pool con attese su task che usano lo stesso pool.''',
'ExecutorService pool = Executors.newFixedThreadPool(2);\ntry {\n    Future<Integer> result = pool.submit(() -> 42);\n    System.out.println(result.get());\n} finally { pool.shutdown(); }',
'Organizzare lavori di I/O e pipeline mantenendo il thread UI libero.',
'Chiamare get sul thread UI; creare executor senza chiuderli; usare thenApply quando la funzione restituisce un altro Future.',JAVA+'java.base/java/util/concurrent/CompletableFuture.html',
'''Future.get() può bloccare?|Sì|No|Solo se il risultato è String|get attende il completamento quando il risultato non è pronto.
thenCompose è utile quando la funzione restituisce cosa?|Un altro CompletionStage|Solo un int|Sempre void|thenCompose appiattisce una composizione asincrona.
Chi dovrebbe definire la chiusura di un executor?|Il suo proprietario|Sempre il singolo task|Soltanto il GC|La gestione del lifecycle evita thread e risorse trattenuti inutilmente.
Un pool fisso garantisce assenza di saturazione?|No|Sì|Solo con due thread|Task bloccanti o code senza limiti possono saturare risorse e aumentare la latenza.''')
add('java','java-virtual','Virtual thread e limiti del parallelismo','Concorrenza',
'''I virtual thread, disponibili stabilmente da Java 21, riducono il costo di molti task che attendono I/O. Non rendono più veloce il calcolo CPU né eliminano limiti di database, rete o memoria. Il modello consigliato è un virtual thread per task, con controllo separato delle risorse scarse.
Il comportamento di pinning dipende dalla versione della JVM: non applicare indicazioni di Java 21 indiscriminatamente alle versioni successive. ThreadLocal resta utilizzabile, ma moltiplicare dati pesanti per moltissimi thread è costoso. Il codice deve comunque essere thread-safe.''',
'// Richiede Java 21, non è codice da eseguire dentro l’app Android.\ntry (var executor = Executors.newVirtualThreadPerTaskExecutor()) {\n    Future<String> response = executor.submit(() -> "risultato I/O");\n    System.out.println(response.get());\n}',
'Servizi backend con molte richieste bloccanti, dopo aver misurato carico e limiti.',
'Aspettarsi accelerazione CPU; eliminare i limiti del pool connessioni; usare questo esempio come API Android.',JAVA+'java.base/java/lang/Thread.html',
'''I virtual thread sono stabili a partire da quale Java?|21|8|11|Java 21 introduce stabilmente i virtual thread.
Quale carico beneficia soprattutto dei virtual thread?|Molti task in attesa di I/O|Ogni calcolo CPU senza limiti|Solo ordinamenti in memoria|Il vantaggio principale riguarda alta concorrenza con attese bloccanti.
I virtual thread eliminano il limite di connessioni al database?|No|Sì|Solo con JDBC|Le risorse esterne restano limitate e vanno controllate.
Un esempio Java 21 con virtual thread è automaticamente eseguibile su Android API 26?|No|Sì|Solo in XML|Le API Java SE del backend non coincidono automaticamente con quelle Android.''')
add('java','java-reflection','Annotazioni, reflection e proxy','JVM',
'''Le annotazioni descrivono metadati, non eseguono da sole un comportamento. La retention determina se sono disponibili al runtime. Un framework può leggerle tramite reflection e applicare configurazione o intercettazione.
Reflection ispeziona classi e membri, ma deve rispettare accessibilità e regole dei moduli. Un proxy avvolge chiamate e aggiunge comportamento; chiamare direttamente un metodo sull’oggetto può aggirare il proxy. Il costo e la complessità devono essere giustificati, non usati per evitare un contratto esplicito.''',
'@Retention(RetentionPolicy.RUNTIME)\n@interface Audited {}\nboolean enabled = service.getClass().isAnnotationPresent(Audited.class);',
'Capire come DI, validazione e transazioni leggono metadati e intercettano chiamate.',
'Credere che scrivere un’annotazione attivi da solo la logica; ignorare i limiti dei proxy e della self-invocation.',JLS+'jls-9.html#jls-9.6',
'''Un’annotazione esegue automaticamente codice soltanto perché è presente?|No|Sì|Solo se il nome inizia con Enable|Serve un compilatore o runtime che interpreti i metadati.
Quale retention rende un’annotazione disponibile alla reflection runtime?|RUNTIME|SOURCE|CLASS sempre|RUNTIME conserva i metadati accessibili alla reflection.
Un proxy può aggiungere comportamento alle chiamate intercettate?|Sì|No|Solo sui campi static|I proxy permettono intercettazione di invocazioni secondo la loro tecnologia.
Chiamare un metodo direttamente sull’istanza può aggirare un proxy esterno?|Sì|No|Solo nei record|L’intercettazione richiede passare attraverso il proxy.''')
add('java','java-modules','Package, classpath e moduli JPMS','JVM',
'''I package organizzano nomi e contribuiscono all’accessibilità; il classpath indica dove cercare classi. JPMS, introdotto in Java 9, aggiunge moduli dichiarati con module-info.java. requires dichiara dipendenze, exports espone package e opens consente accesso riflessivo pertinente.
Un modulo non è semplicemente un package rinominato. L’incapsulamento dei moduli può far fallire reflection prima tollerata. Non aggiungere opzioni --add-opens senza capire cosa stai esponendo e perché una libreria lo richiede.''',
'module study.core {\n    requires java.sql;\n    exports it.study.core.api;\n}',
'Organizzare applicazioni Java modulari e capire errori di accessibilità nelle librerie.',
'Confondere exports con opens; credere che package diversi impediscano automaticamente ogni accesso pubblico.',JLS+'jls-7.html',
'''JPMS è stato introdotto in quale Java?|9|5|21|Il sistema dei moduli fa parte di Java 9.
Che cosa dichiara requires?|Una dipendenza da un modulo|Un metodo astratto|Una risorsa da chiudere|requires indica i moduli richiesti.
exports e opens hanno lo stesso scopo?|No|Sì|Solo per java.base|exports espone API; opens riguarda l’accesso riflessivo.
Il classpath identifica cosa?|Percorsi dove cercare classi e risorse|Solo package esportati JPMS|La memoria heap|Il classpath definisce sorgenti di caricamento per il modello non modulare.''')
add('java','java-testing','Test unitari, assert e isolamento','Qualità',
'''Un test unitario verifica un comportamento con dipendenze controllate. Arrange prepara dati, Act esegue e Assert verifica il risultato. Un test utile fallisce quando cambia il comportamento importante, non quando cambia un dettaglio interno irrilevante.
Fake e stub sostituiscono dipendenze; mock verificano interazioni quando queste fanno parte del contratto. Evita tempo reale, rete e database esterno nei test unitari. I test di integrazione verificano invece il collegamento tra componenti reali. Coprire righe non equivale a coprire casi limite.''',
'@Test void rejectsNegativeAmount() {\n    assertThrows(IllegalArgumentException.class, () -> new Wallet(-1));\n}\n@Test void increments() {\n    Counter counter = new Counter(0);\n    counter.increment();\n    assertEquals(1, counter.value());\n}',
'Proteggere regole di dominio e riprodurre bug senza dipendere dall’ambiente.',
'Testare soltanto il percorso felice; mockare ogni riga; usare sleep per sincronizzare test.', 'https://docs.junit.org/5.11.0/user-guide/',
'''Che cosa dovrebbe verificare un test unitario?|Un comportamento osservabile|Ogni variabile privata|Soltanto che non ci siano eccezioni|Le asserzioni devono proteggere il contratto del comportamento.
Un test con database reale e mapping ORM è tipicamente?|Di integrazione|Sempre unitario|Un test di sintassi XML|Verifica più componenti e la loro collaborazione reale.
Alta copertura di righe garantisce assenza di bug?|No|Sì|Solo oltre il 90%|La copertura non dimostra la correttezza delle asserzioni e dei casi scelti.
Per testare una scadenza è meglio usare cosa?|Un orologio controllato|Sleep lungo|L’ora corrente del server|Dipendenze temporali controllate rendono il test deterministico.''')
add('java','java-build','Maven, Gradle e dipendenze','Qualità',
'''Un build tool risolve dipendenze, compila, esegue test e produce artefatti. Maven usa un modello dichiarativo con lifecycle; Gradle usa task e configurazione del progetto. Il wrapper fissa il tool richiesto e rende il build più riproducibile.
Dipendenze transitive possono introdurre conflitti di versione. Non scegliere versioni a caso per correggere un errore: ispeziona l’albero, allinea il BOM quando presente e verifica compatibilità Java. Segreti e configurazioni locali non devono essere versionati nel repository.''',
'./mvnw test\n./mvnw dependency:tree\n./gradlew test\n./gradlew dependencies',
'Riprodurre build, capire conflitti e preparare pipeline CI.',
'Aggiungere versioni incompatibili; ignorare dipendenze transitive; committare file con credenziali.', 'https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html',
'''A cosa serve il wrapper del build tool?|Usare la versione di tool prevista dal progetto|Eliminare tutte le dipendenze|Sostituire il JDK|Il wrapper rende esplicita e riproducibile la versione del tool.
Una dipendenza transitiva arriva come?|Dipendenza di un’altra dipendenza|Soltanto file locale manuale|Variabile d’ambiente|Il grafo include dipendenze richieste dalle librerie dirette.
Prima di cambiare versioni per un conflitto cosa conviene fare?|Ispezionare l’albero delle dipendenze|Aggiornare tutto alla cieca|Rimuovere tutti i test|L’albero mostra quali librerie introducono le versioni in conflitto.
È corretto versionare password in application.properties?|No|Sì se il repository è privato|Solo con un commento|I segreti devono restare fuori dai file versionati.''')
add('java','java-numbers','BigDecimal, precisione e denaro','API e I/O',
'''double rappresenta numeri binari floating point e molti decimali non sono esatti. BigDecimal permette calcoli decimali con precisione e scala esplicite. Costruirlo da una stringa evita di importare l’approssimazione di un double.
Divisioni non terminanti richiedono una politica di arrotondamento. equals confronta anche la scala; compareTo considera il valore numerico. Nei sistemi monetari devi definire valuta, scala, arrotondamento e gestione dei valori negativi: usare BigDecimal da solo non risolve il dominio.''',
'BigDecimal price = new BigDecimal("10.20");\nBigDecimal unit = price.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);\nboolean equalValue = new BigDecimal("1.0").compareTo(new BigDecimal("1.00")) == 0;',
'Calcolare prezzi e importi con regole di arrotondamento esplicite.',
'Usare new BigDecimal(0.1); confondere equals e compareTo; dividere senza definire precisione per risultati non terminanti.',JAVA+'java.base/java/math/BigDecimal.html',
'''Per creare esattamente il decimale 0.1 quale scelta è adatta?|new BigDecimal("0.1")|new BigDecimal(0.1)|Cast int di 0.1|La stringa preserva il valore decimale desiderato senza importare l’approssimazione double.
BigDecimal("1.0").equals(BigDecimal("1.00")) è vero?|No|Sì|Non compila|equals considera valore e scala.
compareTo tra 1.0 e 1.00 restituisce?|0|1|-1|compareTo confronta il valore numerico indipendentemente dalla diversa scala.
Una divisione non terminante senza arrotondamento può causare?|ArithmeticException|Sempre null|SQLException|Occorre definire precisione o scala e arrotondamento quando il risultato esatto non è rappresentabile.''')
add('java','java-network','HTTP client, timeout e gestione degli errori','API e I/O',
'''HttpClient consente richieste HTTP sincrone o asincrone. Il timeout di connessione e quello della richiesta sono controlli diversi. Una risposta 404 o 500 è una risposta HTTP, non necessariamente un’eccezione di trasporto.
Controlla status, dimensione del corpo e formato prima di usare i dati. TLS protegge il canale ma non autorizza automaticamente l’operazione. I retry devono considerare idempotenza, limiti e backoff; ripetere una POST può duplicare un’azione se il server l’ha già eseguita.''',
'HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();\nHttpRequest request = HttpRequest.newBuilder(URI.create("https://example.org"))\n    .timeout(Duration.ofSeconds(5)).GET().build();\nHttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());\nif (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode());',
'Consumare API esterne con limiti e distinguere guasti di rete da errori applicativi.',
'Ignorare lo status; fare retry illimitati; disabilitare verifica TLS per risolvere un problema locale.',JAVA+'java.net.http/java/net/http/HttpClient.html',
'''Una risposta HTTP 404 implica sempre IOException dal client?|No|Sì|Solo con GET|Gli status HTTP vanno controllati nel risultato; gli errori di trasporto sono distinti.
Il timeout di connessione copre ogni fase della richiesta?|No|Sì|Solo in HTTPS|Vanno valutati separatamente connessione e durata della richiesta.
Ripetere automaticamente una POST è sempre sicuro?|No|Sì|Solo se ritorna JSON|Serve una strategia di idempotenza per evitare duplicazioni.
TLS sostituisce l’autorizzazione dell’utente?|No|Sì|Solo con JWT|TLS protegge il canale; autorizzazione e autenticazione restano responsabilità separate.''')
add('spring','spring-boot','Spring, Spring Boot e auto-configurazione','Fondamenti',
'''Spring Framework offre container, web, accesso dati e infrastruttura. Spring Boot configura applicazioni Spring con convenzioni, starter e auto-configurazioni condizionali. Non genera magicamente la logica di dominio e non rende irrilevante comprendere i bean.
Gli starter raggruppano dipendenze; il BOM allinea versioni compatibili. L’auto-configurazione valuta classpath, proprietà e bean già presenti. Per diagnosticare una scelta usa il condition evaluation report. Il catalogo usa Spring Boot 3.5 e Jakarta: non mischiare esempi Boot 2 javax con Boot 3 jakarta.''',
'@SpringBootApplication\npublic class App {\n    public static void main(String[] args) {\n        SpringApplication.run(App.class, args);\n    }\n}',
'Avviare un backend con configurazione prevedibile e capire perché viene creato un componente.',
'Credere che Boot sostituisca Spring; aggiungere versioni manuali incompatibili; copiare import javax da vecchi tutorial.',BOOT+'using/auto-configuration.html',
'''Spring Boot sostituisce completamente Spring Framework?|No|Sì|Solo per REST|Boot costruisce su Spring e ne semplifica configurazione e avvio.
Uno starter contiene principalmente cosa?|Un insieme coordinato di dipendenze|Tutta la logica dell’app|Un database obbligatorio|Gli starter raggruppano dipendenze per un tipo di funzionalità.
L’auto-configurazione considera bean già definiti?|Sì|No|Solo il nome del progetto|Molte auto-configurazioni si attivano o arretrano in base alle condizioni e ai bean esistenti.
Quale namespace usa normalmente Bean Validation in Boot 3?|jakarta.validation|javax.validation come regola Boot 3|android.validation|Boot 3 si basa sul namespace Jakarta per queste API.''')
add('spring','spring-di','IoC, bean e constructor injection','Container',
'''Il container IoC crea, collega e gestisce i componenti definiti come bean. Dependency injection consegna collaboratori a un oggetto invece di farglieli costruire internamente. La constructor injection rende visibili dipendenze obbligatorie e permette campi final.
Un oggetto creato con new fuori dal container non riceve automaticamente DI o proxy Spring. Un bean singleton è condiviso nel container, quindi stato mutabile e richieste concorrenti richiedono attenzione. Le classi di dominio semplici non devono diventare bean senza motivo.''',
'@Service\nclass StudyService {\n    private final TopicRepository repository;\n    StudyService(TopicRepository repository) { this.repository = repository; }\n}',
'Separare responsabilità e sostituire collaboratori nei test.',
'Usare field injection per nascondere dipendenze; creare manualmente un service aspettandosi transazioni Spring; tenere dati della richiesta in campi singleton.',SPRING+'core/beans/dependencies/factory-collaborators.html',
'''Che cosa fa dependency injection?|Fornisce collaboratori dall’esterno|Elimina ogni dipendenza|Crea sempre un nuovo database|DI rende esplicita la collaborazione senza costruirla internamente.
Un service creato con new riceve automaticamente proxy transazionali?|No|Sì|Solo se ha @Service|La gestione del container è necessaria per le normali funzionalità Spring applicate ai bean.
Un costruttore unico richiede sempre @Autowired?|No|Sì|Solo con un parametro|Spring può usare automaticamente l’unico costruttore.
Un bean singleton con campo per l’utente corrente è una buona scelta?|No|Sì|Solo se il campo è String|Richieste concorrenti possono condividere e sovrascrivere quello stato.''')
add('spring','spring-beans','Component scan, @Bean e selezione delle dipendenze','Container',
'''Component scanning scopre classi annotate nei package configurati. @Component, @Service e @Repository dichiarano stereotipi; @Bean registra il risultato di un metodo di configurazione. @SpringBootApplication include configurazione, scanning e auto-configurazione.
Se ci sono più candidati dello stesso tipo, @Qualifier seleziona una dipendenza e @Primary propone il candidato principale. Lo scanning non attraversa automaticamente ogni package del mondo: posizionare la classe applicazione sopra i package dell’app evita sorprese. Le dipendenze circolari spesso indicano responsabilità mal separate.''',
'@Configuration\nclass Config {\n    @Bean Clock clock() { return Clock.systemUTC(); }\n}\n@Service\nclass Planner {\n    Planner(Clock clock) { }\n}',
'Registrare collaboratori esterni e diagnosticare bean mancanti o ambigui.',
'Mettere componenti fuori dallo scan senza configurarlo; aggiungere @Primary per coprire un’ambiguità non compresa.',SPRING+'core/beans/java/bean-annotation.html',
'''@Bean registra normalmente cosa?|Il valore restituito dal metodo|Il nome di ogni parametro|Soltanto una classe astratta|Il metodo factory produce un oggetto gestito dal container.
Con due candidati quale annotazione può selezionarne uno specifico?|@Qualifier|@Override|@Entity|Qualifier restringe i candidati per quella dipendenza.
Component scan trova automaticamente classi fuori dai package configurati?|No|Sì|Solo se sono public|La ricerca segue i package e i filtri configurati.
Una dipendenza circolare va spesso affrontata come?|Problema di separazione delle responsabilità|Un motivo per rendere tutto static|Un requisito normale di ogni app|La revisione dei confini può eliminare il ciclo invece di mascherarlo.''')
add('spring','spring-scope','Scope, lifecycle e thread safety dei bean','Container',
'''Singleton significa una istanza per definizione di bean nel container, non una sola istanza universale nel processo. Prototype crea istanze quando vengono richieste al container. Request e session sono scope web e hanno lifecycle diversi.
Il container non rende thread-safe un oggetto perché è un bean. Iniettare un prototype in un singleton con una dipendenza diretta non crea una nuova istanza a ogni metodo: l’iniezione avviene durante la costruzione. Per risorse gestite definisci inizializzazione e chiusura coerenti.''',
'@Bean(destroyMethod = "shutdown")\nExecutorService worker() {\n    return Executors.newFixedThreadPool(2);\n}',
'Gestire componenti condivisi, risorse e dipendenze con durata corretta.',
'Confondere singleton con thread safety; aspettarsi un nuovo prototype ad ogni chiamata del singleton.',SPRING+'core/beans/factory-scopes.html',
'''Singleton Spring significa cosa?|Una istanza per bean nel container|Una istanza mondiale di quella classe|Una nuova istanza per richiesta|Lo scope singleton è relativo al container e alla definizione.
Un bean è automaticamente thread-safe?|No|Sì|Solo se usa constructor injection|La sicurezza dipende dal suo stato e dal coordinamento.
Prototype iniettato direttamente in un singleton cambia ad ogni metodo?|No|Sì|Solo con @Autowired|La dipendenza viene normalmente risolta durante la costruzione del singleton.
Perché definire destroyMethod su una risorsa?|Per chiuderla col lifecycle del container|Per eliminarla dal database|Per impedirne l’iniezione|Una risorsa deve essere rilasciata quando termina il contesto che la possiede.''')
add('spring','spring-config','Proprietà, profili e configurazione tipizzata','Configurazione',
'''Boot legge configurazione da più fonti con precedenza definita: file, variabili d’ambiente e argomenti possono modificare il risultato. I profili attivano configurazioni per contesti diversi, ma non devono moltiplicare combinazioni incontrollate.
@ConfigurationProperties raggruppa impostazioni tipizzate e può essere validata. @Value è utile per valori singoli. Segreti vanno forniti dall’ambiente o da un gestore dedicato. Non mettere fallback di produzione per credenziali mancanti: è meglio fallire con un messaggio chiaro.''',
'@ConfigurationProperties(prefix = "study")\n@Validated\npublic record StudySettings(@Min(1) int quizSize) {}\n// Registrare con @ConfigurationPropertiesScan o @EnableConfigurationProperties.\n// application.yml: study.quiz-size: 10',
'Gestire ambienti e configurazioni validate senza stringhe sparse nei service.',
'Committare password; credere che il file application.yml vinca sempre; creare configurazioni tipizzate senza registrarle.',BOOT+'features/external-config.html',
'''@ConfigurationProperties è utile per cosa?|Raggruppare configurazioni tipizzate|Mappare tabelle JPA|Proteggere endpoint|L’annotazione associa un prefisso a un modello di impostazioni.
Le variabili d’ambiente possono influenzare configurazione Boot?|Sì|No|Solo se l’app è Android|Boot considera più fonti di proprietà con regole di precedenza.
I profili devono contenere password versionate?|No|Sì se chiamati prod|Solo in YAML|Un profilo seleziona configurazione; non rende sicuri i segreti nel repository.
Un record annotato ConfigurationProperties deve essere registrato?|Sì|No, basta creare il file|Solo se ha String|Scan o abilitazione registrano il bean di configurazione.''')
add('spring','spring-mvc','DispatcherServlet e percorso di una richiesta','Web e REST',
'''Nel modello Servlet, i filter possono intercettare la richiesta prima del DispatcherServlet. Il dispatcher seleziona handler, esegue binding e invoca il controller; converter trasformano corpi e risultati. Interceptor MVC e filter hanno punti d’intervento diversi.
Il controller gestisce il contratto HTTP e delega operazioni applicative al service. Non dovrebbe contenere tutto il dominio né esporre direttamente dettagli del database. Gli errori di binding o validazione possono avvenire prima che il corpo del metodo venga eseguito.''',
'@RestController\n@RequestMapping("/topics")\nclass TopicController {\n    private final TopicService service;\n    TopicController(TopicService service) { this.service = service; }\n    @GetMapping("/{id}") TopicDto find(@PathVariable long id) { return service.find(id); }\n}',
'Diagnosticare richieste che non arrivano al controller e mantenere confini puliti.',
'Mettere SQL e regole di business nel controller; confondere filter e interceptor.',SPRING+'web/webmvc/mvc-servlet.html',
'''Quale componente coordina il dispatch MVC Servlet?|DispatcherServlet|EntityManager|ApplicationRunner|DispatcherServlet gestisce il percorso verso gli handler MVC.
Un filter può agire prima del controller?|Sì|No|Solo dopo la risposta|I filter appartengono alla catena Servlet e possono precedere il dispatcher.
Un errore di binding può impedire l’esecuzione del metodo controller?|Sì|No|Solo su GET|La preparazione degli argomenti precede l’invocazione del metodo.
Dove dovrebbe stare un’operazione applicativa con regole di dominio?|Nel service pertinente|Sempre nel controller|Nel file YAML|Il controller traduce HTTP e delega il comportamento applicativo.''')
add('spring','spring-http','REST, metodi HTTP e status','Web e REST',
'''REST organizza risorse e usa la semantica HTTP. GET è safe e non dovrebbe modificare lo stato applicativo; PUT è idempotente e rappresenta normalmente sostituzione; PATCH descrive modifiche parziali; POST non garantisce idempotenza.
Gli status comunicano il contratto: 201 per creazione, 204 senza corpo, 400 per input invalido, 404 per risorsa assente, 409 per conflitti pertinenti. Idempotente significa che ripetere l’operazione ha lo stesso effetto previsto, non che status e corpo siano identici ogni volta.''',
'@PostMapping\nResponseEntity<TopicDto> create(@Valid @RequestBody CreateTopic input) {\n    TopicDto created = service.create(input);\n    return ResponseEntity.created(URI.create("/topics/" + created.id())).body(created);\n}',
'Progettare API leggibili e permettere ai client di distinguere esiti.',
'Usare GET per cancellare; restituire sempre 200; associare idempotenza a identità della risposta.',SPRING+'web/webmvc/mvc-controller/ann-methods/responseentity.html',
'''GET dovrebbe modificare stato di business come cancellare una risorsa?|No|Sì|Solo se il client lo vuole|GET è definito safe e non deve essere usato come comando distruttivo.
Quale status è adatto a una creazione riuscita?|201|401|500|201 comunica che una risorsa è stata creata.
Idempotenza implica risposta byte per byte identica?|No|Sì|Solo per PUT|La proprietà riguarda l’effetto previsto di richieste ripetute.
Una risposta 204 dovrebbe avere un corpo JSON?|No|Sì|Solo per DELETE|204 indica successo senza contenuto nella risposta.''')
add('spring','spring-dto','DTO, binding e serializzazione JSON','Web e REST',
'''I DTO definiscono input e output dell’API separatamente dalle entity persistenti. @RequestBody usa converter per leggere il corpo; @PathVariable e @RequestParam prendono dati da percorso e query. Jackson serializza oggetti secondo configurazione e proprietà esposte.
Separare DTO ed entity evita esporre password, relazioni lazy e campi aggiornabili impropriamente. Un DTO non è automaticamente sicuro: valida ogni campo e seleziona esplicitamente cosa puoi modificare. Gestisci campi mancanti, null e formati temporali con un contratto preciso.''',
'public record CreateUser(@NotBlank String name, @Email @NotBlank String email) {}\npublic record UserDto(long id, String name) {}\n// La password hash non compare nel DTO pubblico.',
'Proteggere il contratto API da dettagli ORM e mass assignment.',
'Restituire entity con password hash; accettare dal client ruoli amministrativi senza autorizzazione; confondere body e query param.',SPRING+'web/webmvc/mvc-controller/ann-methods/requestbody.html',
'''@RequestBody legge normalmente da dove?|Corpo della richiesta|Solo segmento del path|Sempre cookie|Il converter legge il corpo nel tipo richiesto.
Un DTO pubblico dovrebbe includere password hash?|No|Sì se bcrypt|Solo per l’utente proprietario|Gli hash sono dati sensibili e non fanno parte della normale risposta utente.
Perché separare DTO ed entity?|Controllare contratto e campi esposti|Eliminare ogni mapping|Garantire automaticamente autorizzazione|La separazione rende esplicita la superficie API ma richiede comunque validazione e sicurezza.
@PathVariable prende un valore da dove?|Un segmento del percorso mappato|Solo JSON|Sempre header Authorization|PathVariable associa una variabile del mapping del percorso.''')
add('spring','spring-validation','Bean Validation e validazione di dominio','Web e REST',
'''Bean Validation applica vincoli dichiarativi come @NotNull, @NotBlank, @Size e @Email. @Valid attiva la validazione del modello nei punti supportati e può cascatare su oggetti annidati. @Email da solo non esprime necessariamente obbligatorietà.
I vincoli strutturali non sostituiscono regole applicative come disponibilità del saldo o proprietà della risorsa. Anche i dati provenienti da integrazioni interne devono essere controllati al confine appropriato. Non restituire input sensibile dentro messaggi di errore.''',
'public record RegisterRequest(\n    @NotBlank @Email String email,\n    @NotBlank @Size(min=12,max=100) String password\n) {}\n@PostMapping("/register") void register(@Valid @RequestBody RegisterRequest input) { }',
'Respingere input invalido prima delle operazioni applicative.',
'Usare @NotNull per stringhe vuote; dimenticare @Valid; trattare validazione come autorizzazione.',SPRING+'core/validation/beanvalidation.html',
'''@NotNull vieta anche una stringa vuota?|No|Sì|Solo su DTO|NotNull verifica soltanto assenza di null.
Quale vincolo vieta stringhe composte solo da spazi?|@NotBlank|@NotNull|@Positive|NotBlank richiede un contenuto non blank.
@Email da solo garantisce campo obbligatorio?|No|Sì|Solo con record|La validazione del formato va combinata con obbligatorietà quando richiesta.
Validare un id garantisce che l’utente possa accedere alla risorsa?|No|Sì|Solo se id è positivo|L’autorizzazione è una verifica distinta dalla forma dell’input.''')
add('spring','spring-errors','ControllerAdvice e contratto degli errori','Web e REST',
'''@RestControllerAdvice centralizza la traduzione delle eccezioni in risposte HTTP. @ExceptionHandler associa classi di errore a status e payload. ProblemDetail rappresenta un formato standard per problemi HTTP e può essere arricchito con dettagli controllati.
Separa errori di input, risorsa assente, conflitto e guasti interni. Log e risposta hanno pubblico diverso: uno stack trace può aiutare i log ma non dovrebbe essere esposto al client. Non catturare ogni errore nel service per restituire null, perché cancella il significato del guasto.''',
'@RestControllerAdvice\nclass Errors {\n    @ExceptionHandler(TopicNotFound.class)\n    ProblemDetail missing(TopicNotFound ex) {\n        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Argomento non trovato");\n    }\n}',
'Mantenere errori coerenti senza duplicare try/catch in ogni controller.',
'Esporre stack trace e query SQL; tradurre tutti gli errori in 200; catturare eccezioni e restituire null.',SPRING+'web/webmvc/mvc-controller/ann-exceptionhandler.html',
'''@RestControllerAdvice è utile per cosa?|Centralizzare gestione errori dei controller|Aprire transazioni JDBC sempre|Generare entity|L’advice associa eccezioni a risposte coerenti.
Uno stack trace completo dovrebbe essere normalmente inviato al client?|No|Sì|Solo per 500|I dettagli interni appartengono ai log controllati.
Una risorsa assente va normalmente distinta da un guasto interno?|Sì|No|Solo se il database è MySQL|Status e payload dovrebbero riflettere categorie diverse.
Restituire null dopo aver catturato ogni eccezione facilita il contratto?|No|Sì|Solo nei repository|Nasconde il guasto e rende ambigua l’assenza di valore.''')
add('spring','spring-jpa','Entity, identificatori e persistence context','Persistenza',
'''JPA definisce un modello di persistenza; Hibernate è un’implementazione. Un’entity ha identità persistente e attraversa stati transient, managed, detached e removed. Il persistence context conserva entity gestite e coordina cambiamenti e identità.
Un DTO non è automaticamente un’entity e un oggetto detached non beneficia del normale dirty checking di quel contesto. Le entity richiedono una strategia di id e un costruttore senza argomenti pubblico o protetto. Evita uguaglianza basata su campi mutabili o id generati senza considerare lo stato prima della persistenza.''',
'@Entity\nclass Topic {\n    @Id @GeneratedValue private Long id;\n    private String title;\n    protected Topic() {}\n    Topic(String title) { this.title = title; }\n}',
'Comprendere cosa viene realmente seguito dall’ORM e perché una modifica viene persistita.',
'Confondere JPA con Hibernate; usare entity detached aspettandosi aggiornamento automatico; eliminare il costruttore richiesto.', 'https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html',
'''JPA e Hibernate sono esattamente la stessa cosa?|No|Sì|Solo con MySQL|JPA è una specifica e Hibernate è una sua implementazione.
Quale stato viene normalmente seguito dal dirty checking del contesto?|Managed|Detached sempre|Qualsiasi DTO|Il contesto osserva le entity gestite.
Una entity JPA deve avere quale costruttore?|Senza argomenti pubblico o protetto|Soltanto privato con tutti i campi|Sempre un record|La specifica richiede un costruttore senza argomenti accessibile nel modo previsto.
Il persistence context coordina quale identità?|Entity gestite per identità persistente|Ogni oggetto Java del processo|Soltanto stringhe|Il contesto mantiene identità e stato delle entity gestite.''')
add('spring','spring-repositories','Spring Data repository e query','Persistenza',
'''Spring Data crea implementazioni di interfacce repository. I metodi derivati esprimono query tramite nomi di proprietà; @Query permette JPQL o SQL nativo. JPQL usa nomi di entity e attributi, non automaticamente nomi di tabella.
findById restituisce Optional e materializza la ricerca; getReferenceById può fornire un riferimento che richiede accesso successivo al contesto. Le projection limitano dati selezionati. Nomi di metodo molto lunghi possono suggerire una query esplicita o una responsabilità da rivedere.''',
'interface TopicRepository extends JpaRepository<Topic,Long> {\n    List<Topic> findByTitleContainingIgnoreCase(String title);\n    @Query("select t from Topic t where t.title = :title")\n    List<Topic> byTitle(@Param("title") String title);\n}',
'Accedere ai dati con contratti espliciti e diagnosticare query derivate.',
'Usare nomi SQL dentro JPQL; credere che getReferenceById verifichi sempre subito l’esistenza; usare findAll per ogni pagina.', 'https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html',
'''JPQL usa normalmente quali nomi?|Entity e attributi Java|Sempre tabelle e colonne SQL|File YAML|JPQL opera sul modello persistente delle entity.
findById ritorna normalmente cosa?|Optional dell’entity|Sempre una entity non null|Soltanto boolean|Optional rende esplicita l’eventuale assenza.
getReferenceById verifica sempre subito l’esistenza?|No|Sì|Solo se l’id è positivo|Un riferimento può rinviare caricamento e verifica all’accesso successivo.
Una projection può essere utile per cosa?|Selezionare soltanto dati necessari|Disabilitare validazione|Creare sempre nuove tabelle|Le projection possono ridurre il payload e l’accesso a dati non richiesti.''')
add('spring','spring-relations','Relazioni, mappedBy e lato proprietario','Persistenza',
'''Le relazioni JPA descrivono associazioni, ma il database conserva chiavi esterne o tabelle di collegamento. Nelle associazioni bidirezionali mappedBy indica il lato inverso e fa riferimento al nome dell’attributo proprietario, non al nome della colonna.
Il lato proprietario determina l’aggiornamento della relazione nel database. Metodi di dominio dovrebbero mantenere coerenti entrambi i lati in memoria. Le cardinalità non implicano automaticamente cascade o caricamento ideale. Mostrare entity direttamente in JSON può introdurre cicli e caricamenti indesiderati.''',
'@OneToMany(mappedBy="subject")\nprivate List<Topic> topics = new ArrayList<>();\n// Nell’entity Topic:\n@ManyToOne(fetch=FetchType.LAZY)\n@JoinColumn(name="subject_id")\nprivate Subject subject;',
'Mappare materia e argomenti mantenendo correttamente le chiavi esterne.',
'Scrivere mappedBy="subject_id" invece del nome attributo; aggiornare soltanto la lista inversa.', 'https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html',
'''mappedBy contiene normalmente cosa?|Nome dell’attributo proprietario|Nome della colonna SQL|Nome del database|mappedBy indica l’attributo dell’altro lato che possiede l’associazione.
Quale lato guida la scrittura della relazione?|Il lato proprietario|Sempre quello con una List|Entrambi indipendentemente senza regole|Il proprietario determina la rappresentazione persistente dell’associazione.
Una relazione bidirezionale va mantenuta coerente in memoria?|Sì|No|Solo nelle query native|Aggiornare entrambi i lati evita un grafo Java incoerente.
Una relazione ManyToOne imposta automaticamente cascade ALL?|No|Sì|Solo in Boot|Cascade è una scelta separata dalla cardinalità.''')
add('spring','spring-fetch','Lazy loading, N+1 e fetch plan','Persistenza',
'''LAZY rimanda il caricamento di un’associazione; EAGER richiede che sia disponibile, ma non garantisce una singola join SQL. Accedere a un proxy lazy fuori da un contesto utile può causare LazyInitializationException in Hibernate.
N+1 significa una query iniziale più query aggiuntive per le associazioni di molti risultati. Risolvilo con un piano mirato: projection, fetch join, entity graph o batching. Non trasformare tutto in EAGER. Una fetch join su collezioni con paginazione richiede attenzione a duplicati e limiti applicati.''',
'@EntityGraph(attributePaths = "subject")\nPage<Topic> findByTitleContaining(String title, Pageable page);\n// Carica l’associazione necessaria al caso d’uso, non tutto il grafo.',
'Ridurre query e costruire DTO dentro un confine applicativo controllato.',
'Usare EAGER ovunque per nascondere errori lazy; serializzare entity senza conoscere le query eseguite.', 'https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html',
'''EAGER garantisce sempre una singola query con join?|No|Sì|Solo su ManyToOne|La disponibilità richiesta non determina da sola la forma SQL.
Che cosa descrive N+1?|Una query iniziale più query aggiuntive per risultati associati|Sempre un indice mancante|Un errore di compilazione|Il costo cresce quando ogni risultato attiva nuove letture.
Rendere tutto EAGER è una soluzione generale a N+1?|No|Sì|Solo con pochi campi|Serve un piano di caricamento mirato al caso d’uso.
Una projection può evitare caricamenti non necessari?|Sì|No|Solo con Redis|Selezionare i dati richiesti può evitare traversate del grafo ORM.''')
add('spring','spring-cascade','Cascade, orphanRemoval e cancellazioni','Persistenza',
'''Cascade propaga operazioni ORM come persist, merge e remove lungo un’associazione. Non è la stessa cosa di ON DELETE CASCADE del database. CascadeType.ALL include REMOVE e può essere troppo ampio su relazioni condivise.
OrphanRemoval elimina figli rimossi dalla relazione che rappresenta la loro ownership. Va usato quando il figlio appartiene al ciclo di vita del padre. Non propagare REMOVE da un figlio a una materia condivisa. Per cancellazioni importanti verifica vincoli, autorizzazione e comportamento con test di integrazione.''',
'@OneToMany(mappedBy="quiz", cascade=CascadeType.ALL, orphanRemoval=true)\nprivate List<Option> options = new ArrayList<>();\n// Le opzioni appartengono al ciclo di vita di questo quiz.',
'Gestire aggregati con figli esclusivi senza cancellare entità condivise.',
'Applicare ALL a tutte le relazioni; confondere cascade ORM con vincoli database; usare orphanRemoval su oggetti condivisi.', 'https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1.html',
'''CascadeType.ALL include REMOVE?|Sì|No|Solo in Hibernate|ALL comprende anche rimozione e va valutato attentamente.
Cascade ORM e ON DELETE CASCADE SQL sono identici?|No|Sì|Solo con JPA|Operano in livelli diversi e hanno configurazioni distinte.
OrphanRemoval è adatto soprattutto a figli di quale tipo?|Con ciclo di vita posseduto dal padre|Condivisi da molti aggregati|Sempre utenti globali|La rimozione della relazione dovrebbe significare che quel figlio non deve più esistere.
Propagare REMOVE da Topic a Subject condiviso è generalmente corretto?|No|Sì|Solo se LAZY|Rischia di eliminare una materia condivisa da altre risorse.''')
add('spring','spring-dirty','Dirty checking, flush e commit','Persistenza',
'''Hibernate confronta lo stato delle entity managed e rileva modifiche. All’interno di una transazione appropriata una modifica può essere persistita senza chiamare save sull’entity già gestita. Il dirty checking non si applica nello stesso modo a oggetti detached.
Flush sincronizza cambiamenti col database inviando SQL; commit conclude la transazione e ne conferma gli effetti. Un flush riuscito non garantisce commit riuscito e le query possono fallire già al flush. Non confondere save, flush e commit: hanno responsabilità diverse.''',
'@Transactional\npublic void rename(long id, String title) {\n    Topic topic = repository.findById(id).orElseThrow();\n    topic.rename(title); // entity managed\n    // Il flush sincronizza; il commit avviene al confine transazionale.\n}',
'Capire perché una modifica viene salvata e dove emergono errori SQL.',
'Dire che flush equivale a commit; pensare che save sia obbligatorio per ogni entity managed modificata.',SPRING+'data-access/transaction/declarative/annotations.html',
'''Flush e commit sono la stessa operazione?|No|Sì|Solo con Hibernate|Flush invia SQL; commit conferma la transazione.
Una entity managed modificata richiede sempre save esplicito per dirty checking?|No|Sì|Solo con id generato|Il contesto può rilevare la modifica e sincronizzarla al flush.
Un flush riuscito garantisce che il commit riuscirà?|No|Sì|Solo se non hai DTO|Vincoli o guasti possono ancora impedire il completamento della transazione.
Un oggetto detached è seguito dal dirty checking del vecchio contesto?|No|Sì|Solo se ha @Entity|Detached significa che non è più gestito da quel contesto.''')
add('spring','spring-transactions','@Transactional, rollback e self-invocation','Transazioni',
'''Le transazioni dichiarative usano normalmente un proxy che intercetta chiamate dall’esterno al bean. Una chiamata interna this.method() non attraversa quel proxy e non attiva automaticamente il comportamento dell’annotazione sul metodo chiamato.
Per default Spring esegue rollback su RuntimeException ed Error, non su ogni checked exception; puoi configurare rollbackFor. Se catturi e nascondi un errore, il confine transazionale potrebbe vedere un ritorno normale. readOnly è un’indicazione per infrastruttura e ottimizzazioni, non una barriera universale contro ogni scrittura.''',
'@Transactional(rollbackFor = IOException.class)\npublic void importTopics() throws IOException {\n    // L’operazione applicativa deve essere invocata tramite il bean gestito.\n}',
'Proteggere operazioni multi-step e diagnosticare rollback mancanti.',
'Invocare internamente un metodo annotato aspettandosi una nuova transazione; catturare errori senza decidere il rollback.',SPRING+'data-access/transaction/declarative/annotations.html',
'''Nel modello proxy standard, self-invocation attiva l’annotazione del metodo interno?|No|Sì|Solo se public|La chiamata interna non attraversa il proxy esterno.
Il rollback predefinito copre normalmente RuntimeException?|Sì|No|Solo SQLException checked|RuntimeException ed Error causano normalmente rollback secondo le regole predefinite.
Una checked exception causa sempre rollback per default?|No|Sì|Solo se dichiarata throws|È necessario configurare la regola quando il default non corrisponde al caso d’uso.
readOnly=true impedisce universalmente qualunque scrittura?|No|Sì|Solo con MySQL|È un’indicazione e non una garanzia universale di divieto.''')
add('spring','spring-propagation','Propagation, isolation e confini transazionali','Transazioni',
'''REQUIRED partecipa alla transazione esistente o ne crea una. REQUIRES_NEW usa una transazione indipendente, sospendendo quella esterna quando supportato. Può richiedere ulteriori connessioni e non annulla automaticamente ciò che ha già confermato se la transazione esterna fallisce.
Isolation regola anomalie come letture sporche e non ripetibili secondo il database. Una transazione non elimina automaticamente ogni race condition. Mantieni brevi i confini e non includere chiamate remote lente senza ragionare su timeout e consistenza.''',
'@Transactional(propagation = Propagation.REQUIRED)\npublic void transfer() { /* entrambe le modifiche nello stesso confine */ }\n// REQUIRES_NEW soltanto se serve davvero un commit indipendente.',
'Progettare operazioni atomiche e comprendere effetti di metodi transazionali annidati.',
'Usare REQUIRES_NEW ovunque; pensare che ogni transazione serializzi automaticamente tutte le richieste.',SPRING+'data-access/transaction/declarative/tx-propagation.html',
'''REQUIRED con una transazione esistente fa cosa?|Vi partecipa normalmente|La conferma subito|La elimina|REQUIRED usa il confine esistente quando presente.
REQUIRES_NEW può confermare dati anche se l’esterna poi fallisce?|Sì|No|Solo con Redis|La transazione interna è indipendente da quella esterna.
Le transazioni eliminano automaticamente ogni race condition?|No|Sì|Solo se il service è singleton|Servono isolamento, vincoli o locking adatti al caso.
Tenere aperta una transazione durante una chiamata remota lenta può essere un problema?|Sì|No|Solo con GET|Può trattenere connessioni e lock aumentando latenza e contesa.''')
add('spring','spring-locking','@Version, lock e aggiornamenti concorrenti','Transazioni',
'''Optimistic locking usa una versione per verificare che l’entity non sia stata modificata da un altro writer. @Version consente al provider di rilevare aggiornamenti in conflitto invece di perdere silenziosamente una modifica.
Un conflitto richiede una scelta applicativa: mostrare errore, rileggere o ritentare l’intera operazione quando sicuro. Pessimistic locking acquisisce lock database e può bloccare altre transazioni. Vincoli unici e aggiornamenti condizionali restano importanti; @Version non sostituisce ogni controllo di concorrenza.''',
'@Version private Long version;\n// Un UPDATE verifica id e versione attesa.\n// Due writer della stessa versione non dovrebbero entrambi sovrascrivere silenziosamente.',
'Evitare lost update su risorse modificate da richieste concorrenti.',
'Ritentare senza limite; ignorare conflitti; credere che @Version renda atomico un flusso su più sistemi.', 'https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html',
'''@Version serve principalmente a rilevare cosa?|Conflitti di aggiornamento ottimistico|Ogni errore HTTP|Versione del JDK|La versione verifica che lo stato atteso non sia stato modificato da un altro writer.
Un conflitto ottimistico va sempre ignorato?|No|Sì|Solo se l’utente è admin|Serve una gestione applicativa esplicita.
Un lock pessimista può far attendere altre transazioni?|Sì|No|Solo in memoria|Il database coordina l’accesso e può bloccare operazioni concorrenti.
@Version sostituisce un vincolo unico sul database?|No|Sì|Solo con Long|Unicità e concorrenza ottimistica proteggono invarianti diversi.''')
add('spring','spring-pagination','Paginazione, sorting e query efficienti','Persistenza',
'''Pageable descrive pagina, dimensione e ordinamento. Page include informazioni sul totale e può richiedere una count query; Slice indica se esiste un seguito senza necessariamente calcolare il totale. L’ordinamento deve essere stabile, includendo un criterio univoco quando necessario.
Limita dimensione e campi ordinabili. Offset elevati possono essere costosi; keyset pagination usa l’ultima chiave osservata e richiede un contratto adatto. Non caricare tutto e poi ritagliare in Java quando il dataset può crescere.''',
'Pageable page = PageRequest.of(0, 20, Sort.by("title").and(Sort.by("id")));\nPage<Topic> result = repository.findAll(page);',
'Servire cataloghi e liste senza caricare l’intera tabella.',
'Accettare pageSize illimitato; usare ordinamento instabile; credere che Page non esegua query di conteggio.', 'https://docs.spring.io/spring-data/commons/reference/repositories/query-methods-details.html',
'''Page può richiedere una count query aggiuntiva?|Sì|No|Solo su MongoDB|Il totale può richiedere un conteggio separato.
Slice garantisce sempre il numero totale di risultati?|No|Sì|Solo quando è l’ultima pagina|Slice descrive una porzione e il seguito, non necessariamente il totale.
Perché aggiungere id a un sorting non univoco?|Per avere un ordine stabile tra valori uguali|Per cancellare duplicati|Per rendere tutto EAGER|Un criterio univoco riduce ambiguità nel confine tra pagine.
Il client dovrebbe poter chiedere una pagina senza limite di dimensione?|No|Sì|Solo agli utenti autenticati|Un limite protegge memoria, database e latenza.''')
add('spring','spring-security-chain','SecurityFilterChain e autenticazione','Sicurezza',
'''Spring Security Servlet usa catene di filter per autenticazione, gestione del contesto e autorizzazione. SecurityFilterChain configura il comportamento delle richieste selezionate. L’autenticazione determina chi è il chiamante; l’autorizzazione verifica quali azioni può compiere.
SecurityContext conserva l’identità nel contesto previsto dalla strategia e dai filter. AuthenticationManager coordina provider di autenticazione. Non inventare un filtro JWT manuale quando il Resource Server risolve il requisito. Le regole specifiche devono precedere quelle generali pertinenti.''',
'@Bean SecurityFilterChain security(HttpSecurity http) throws Exception {\n    http.authorizeHttpRequests(auth -> auth\n        .requestMatchers("/public/**").permitAll()\n        .anyRequest().authenticated());\n    return http.build();\n}',
'Definire accesso agli endpoint e diagnosticare richieste fermate prima del controller.',
'Confondere autenticazione e autorizzazione; aggiungere permitAll a tutto per risolvere un 403.',SEC+'servlet/architecture.html',
'''Autenticazione risponde principalmente a quale domanda?|Chi è il chiamante?|Quale indice SQL usare?|Qual è il payload JSON?|L’autenticazione stabilisce l’identità.
Autorizzazione verifica cosa?|Azioni consentite all’identità|Soltanto formato email|Solo hash della password|L’autorizzazione decide accesso e permessi.
Spring Security può rifiutare una richiesta prima del controller?|Sì|No|Solo dopo JPA|I filter precedono l’invocazione del controller.
permitAll disattiva automaticamente tutti gli altri filter di sicurezza?|No|Sì|Solo per POST|Consentire l’accesso non elimina automaticamente controlli come CSRF.''')
add('spring','spring-passwords','PasswordEncoder, login e password','Sicurezza',
'''Le password vanno memorizzate tramite hashing dedicato e verificabili con PasswordEncoder.matches. Non si devono decifrare per confrontarle. BCrypt incorpora un salt e un costo; due hash della stessa password possono differire.
Un login deve prevenire enumerazione, brute force e logging di credenziali. La verifica delle password non sostituisce autorizzazione, gestione sessioni o revoca. Usa HTTPS e non restituire mai hash o password nei DTO. Scegli policy e costi coerenti con l’infrastruttura e aggiornabili nel tempo.''',
'PasswordEncoder encoder = new BCryptPasswordEncoder();\nString hash = encoder.encode(rawPassword);\nboolean valid = encoder.matches(candidate, hash);',
'Gestire registrazione e autenticazione senza conservare password in chiaro.',
'Confrontare encode(candidate) con hash tramite equals; usare SHA-256 semplice per password; loggare il body di login.',SEC+'features/authentication/password-storage.html',
'''Per verificare password e hash BCrypt si usa normalmente?|matches(raw, encoded)|encode(raw).equals(encoded)|decode(encoded)|matches verifica usando i parametri e il salt dell’hash.
Due hash BCrypt della stessa password devono essere identici?|No|Sì|Solo se è ASCII|Il salt rende normalmente diversi gli hash di encoding distinti.
La password dovrebbe essere restituita nel DTO di registrazione?|No|Sì|Solo sotto HTTPS|Il trasporto protetto non giustifica l’esposizione della credenziale.
Un semplice hash veloce SHA-256 è una scelta standard appropriata per password?|No|Sì|Solo con una password lunga|Si usano funzioni dedicate con costo e salt, adatte a resistere ai tentativi offline.''')
add('spring','spring-authorization','Ruoli, method security e ownership','Sicurezza',
'''Le regole URL proteggono percorsi; method security protegge operazioni applicative. @EnableMethodSecurity abilita l’infrastruttura e @PreAuthorize esprime controlli prima della chiamata intercettata. hasRole usa la convenzione ROLE_, mentre hasAuthority confronta l’autorità esatta.
Essere autenticati non basta per leggere qualsiasi risorsa: controlla ownership o permessi. Un id fornito dal client non dimostra proprietà. Evita controlli presenti solo nel frontend. Anche i metodi protetti devono attraversare il proxy pertinente per l’intercettazione.''',
'@EnableMethodSecurity\n@Configuration class SecurityConfig {}\n@PreAuthorize("hasAuthority(\'topic:write\')")\npublic void rename(long topicId, String title) { /* verifica anche ownership pertinente */ }',
'Proteggere operazioni e prevenire accesso a risorse di altri utenti.',
'Controllare soltanto autenticazione; confondere hasRole e hasAuthority; fidarsi dell’id utente nel body.',SEC+'servlet/authorization/method-security.html',
'''hasRole("ADMIN") usa normalmente quale autorità?|ROLE_ADMIN|ADMIN senza prefisso per regola generale|admin_id|hasRole applica normalmente il prefisso ROLE_.
Un utente autenticato può automaticamente leggere ogni risorsa?|No|Sì|Solo se conosce l’id|Occorre verificare permessi e ownership.
Quale annotazione abilita la method security moderna?|@EnableMethodSecurity|@Entity|@RestController|L’abilitazione registra l’infrastruttura per i controlli sui metodi.
Un controllo soltanto nel frontend protegge l’API?|No|Sì|Solo con Angular|Un client può aggirare il frontend e invocare direttamente il backend.''')
add('spring','spring-jwt','JWT e OAuth2 Resource Server','Sicurezza',
'''Un JWT firmato protegge integrità e autenticità dei claim; non cifra il payload. Il Resource Server valida token secondo algoritmo, chiavi e vincoli configurati, inclusi scadenza e issuer; audience richiede una configurazione adatta al servizio.
Decodificare Base64 non significa verificare un token. Non fidarti di claim senza firma valida. Un access token breve riduce la finestra di abuso, ma logout non revoca automaticamente token già emessi. La gestione chiavi e gli algoritmi ammessi devono essere espliciti.''',
'http.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));\n// application.yml:\n// spring.security.oauth2.resourceserver.jwt.issuer-uri: https://issuer.example.org\n// Configurare anche validazione audience secondo il contratto.',
'Proteggere API con token emessi da un’autorità e separare emissione da validazione.',
'Credere che JWT sia cifrato; controllare soltanto exp; accettare issuer o algoritmo non verificati.',SEC+'servlet/oauth2/resource-server/jwt.html',
'''Un JWT firmato cifra automaticamente i claim?|No|Sì|Solo con HS256|La firma protegge integrità, non riservatezza del payload.
Decodificare il payload dimostra la validità del token?|No|Sì|Solo se exp è futuro|Occorre verificare firma e vincoli di validazione.
Resource Server serve principalmente a cosa?|Validare token e proteggere risorse|Emettere sempre refresh token|Salvare password in chiaro|L’emissione appartiene all’autorità; il resource server verifica l’accesso.
Il logout revoca automaticamente ogni JWT già emesso?|No|Sì|Solo con HTTPS|Token stateless possono restare validi fino a scadenza senza una strategia di revoca.''')
add('spring','spring-refresh','Refresh token, cookie e revoca','Sicurezza',
'''Un refresh token permette ottenere nuovi access token senza ripetere il login. La rotazione sostituisce il refresh token usato e deve essere atomica; il riuso di un token vecchio può indicare furto e richiedere revoca della famiglia.
Cookie HttpOnly impedisce lettura da JavaScript ma non impedisce l’invio automatico dal browser. Secure richiede HTTPS e SameSite regola invio cross-site. Non memorizzare refresh token raw nei log; valuta hashing a riposo, scadenza e logout. Più richieste simultanee richiedono un contratto preciso per evitare falsi riusi o doppio rinnovo.''',
'// Modello concettuale: transazione unica.\n// 1. Verifica hash, scadenza e stato del refresh token.\n// 2. Invalida il token usato.\n// 3. Salva il nuovo token e consegnalo in cookie sicuro.\n// 4. Gestisci riuso secondo la policy della famiglia.',
'Gestire sessioni lunghe mantenendo access token brevi e revocabili tramite policy.',
'Pensare che HttpOnly risolva CSRF; ruotare token senza concorrenza controllata; salvare token nei log.',SEC+'servlet/oauth2/client/authorized-clients.html',
'''HttpOnly impedisce quale accesso?|Lettura del cookie da JavaScript|Ogni invio automatico del cookie|Ogni attacco CSRF|HttpOnly limita la lettura via script, non l’invio del browser.
La rotazione del refresh token dovrebbe essere atomica?|Sì|No|Solo se il token è JWT|Verifica, invalidazione e sostituzione devono proteggere l’uso concorrente.
Un access token breve elimina ogni rischio di furto?|No|Sì|Solo con RSA|Riduce la finestra ma non elimina l’abuso durante validità.
Il riuso di un refresh token già ruotato può indicare cosa?|Possibile compromissione|Sempre compilazione errata|Solo token scaduto senza altri rischi|La policy può revocare la famiglia per contrastare il riuso.''')
add('spring','spring-csrf','CSRF, CORS e credenziali browser','Sicurezza',
'''CSRF sfrutta credenziali che il browser invia automaticamente, come cookie di sessione. Un’API stateless non è automaticamente immune: conta come vengono trasmesse le credenziali. Un bearer token in header impostato esplicitamente ha un modello diverso da un cookie automatico.
CORS controlla la lettura cross-origin nel browser, non autorizza utenti e non sostituisce CSRF. Cookie cross-site e richieste con credenziali richiedono configurazione coerente. Disabilita CSRF soltanto dopo aver verificato il modello di autenticazione di tutti gli endpoint, incluso refresh e logout.''',
'// Per un’app browser con cookie valuta CSRF token e invio tramite header.\n// Non usare disable() soltanto per far sparire un 403.\n// CORS: origini esplicite e credenziali solo quando necessarie.',
'Integrare frontend e backend senza scambiare problemi CORS, CSRF e autorizzazione.',
'Disabilitare CSRF perché il backend è stateless; usare CORS come controllo di permessi.',SEC+'servlet/exploits/csrf.html',
'''Stateless significa automaticamente immune a CSRF?|No|Sì|Solo con JSON|Conta se il browser invia automaticamente credenziali.
CORS sostituisce autorizzazione sul backend?|No|Sì|Solo per localhost|CORS è una policy browser e non verifica permessi applicativi.
Un cookie HttpOnly viene inviato dal browser quando applicabile?|Sì|No|Solo quando JavaScript lo legge|HttpOnly non impedisce l’invio automatico.
Prima di disabilitare CSRF bisogna valutare cosa?|Il modo di trasmissione delle credenziali|Soltanto il metodo GET|Solo la presenza di JWT nel progetto|Tutti gli endpoint con credenziali automatiche richiedono una valutazione del rischio CSRF.''')
add('spring','spring-security-errors','401, 403 e test dei permessi','Sicurezza',
'''401 comunica normalmente autenticazione mancante o non valida; 403 un rifiuto di accesso, che può riguardare permessi o altri controlli come CSRF. Il significato preciso dipende dalla configurazione e dall’entry point.
AuthenticationEntryPoint gestisce l’avvio o errore dell’autenticazione; AccessDeniedHandler gestisce rifiuti pertinenti. I test devono verificare utente anonimo, autenticato senza permesso, con permesso e proprietario non corretto. Non convertire indiscriminatamente ogni 403 in permitAll.''',
'// Matrice di test:\n// anonimo -> endpoint protetto\n// autenticato senza authority -> rifiuto\n// authority corretta ma ownership errata -> rifiuto\n// identity e ownership corrette -> successo',
'Diagnosticare sicurezza e costruire una matrice di autorizzazione verificabile.',
'Interpretare ogni 403 come password sbagliata; correggere problemi rimuovendo protezioni.',SEC+'servlet/authorization/authorize-http-requests.html',
'''401 indica normalmente quale problema?|Autenticazione mancante o non valida|Sempre risorsa inesistente|Sempre errore database|La configurazione dell’entry point determina la risposta di autenticazione.
Un 403 può derivare da CSRF?|Sì|No|Solo in app senza cookie|Il rifiuto può arrivare da controlli diversi dai soli ruoli.
Quale caso va incluso nei test di sicurezza?|Autenticato ma senza permesso|Solo admin autorizzato|Solo risposta 200|La verifica dei rifiuti protegge il contratto di autorizzazione.
permitAll è la correzione generale per un 403 inatteso?|No|Sì|Solo sui POST|Serve individuare il controllo che ha rifiutato la richiesta.''')
add('spring','spring-tests','SpringBootTest, slice e MockMvc','Testing',
'''@SpringBootTest carica un contesto applicativo ampio. @WebMvcTest concentra il test sul livello MVC e i collaboratori applicativi vanno forniti o sostituiti. @DataJpaTest concentra la verifica sulla persistenza e può configurare un database di test.
MockMvc esegue richieste attraverso infrastruttura MVC senza richiedere necessariamente una porta reale. Un test di slice non verifica tutta l’applicazione. Per SQL e locking usa anche il database di produzione tramite ambiente isolato, perché H2 può avere semantica diversa.''',
'@WebMvcTest(TopicController.class)\nclass TopicControllerTest {\n    @Autowired MockMvc mvc;\n    // Fornire un collaboratore TopicService controllato.\n}\n// Verificare status, payload, validazione e sicurezza pertinenti.',
'Usare il livello di test adeguato invece di avviare tutta l’app per ogni caso.',
'Pensare che MockMvc verifichi sempre rete reale; considerare H2 identico a MySQL; mockare anche la parte che si vuole verificare.',BOOT+'testing/spring-boot-applications.html',
'''@WebMvcTest verifica principalmente quale livello?|MVC|Tutto il database reale|Solo algoritmi Java puri|È una slice centrata sul livello web.
MockMvc richiede sempre un server su porta TCP?|No|Sì|Solo per POST|Può esercitare MVC senza una porta reale.
H2 garantisce comportamento identico a MySQL?|No|Sì|Solo con JPA|Dialetto, vincoli e locking possono differire.
Un test di slice dimostra automaticamente funzionamento di tutta l’app?|No|Sì|Solo se passa|La verifica è limitata ai componenti inclusi nella slice.''')
add('spring','spring-migrations','Flyway e migrazioni del database','Operatività',
'''Le migrazioni versionate descrivono evoluzione dello schema e dei dati. Flyway mantiene una cronologia e checksum per individuare modifiche alle migrazioni già applicate. Correggi uno schema con una nuova migrazione, non riscrivendo la storia distribuita.
DDL auto come update non sostituisce una strategia di migrazione riproducibile. Testa upgrade da una versione con dati e installazione vuota. Cambiamenti grandi possono richiedere expand-and-contract per mantenere compatibilità durante il rilascio. Non tutte le operazioni DDL sono transazionali su ogni database.''',
'-- V1__create_topics.sql\nCREATE TABLE topics (id BIGINT PRIMARY KEY, title VARCHAR(200) NOT NULL);\n-- V2__add_topic_description.sql\nALTER TABLE topics ADD COLUMN description TEXT;',
'Portare lo schema tra ambienti senza modifiche manuali non tracciate.',
'Modificare una migrazione già applicata; usare ddl-auto=update come unica strategia produttiva.','https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html',
'''Una migrazione già distribuita dovrebbe essere riscritta per aggiungere una colonna?|No|Sì|Solo se è V1|Si aggiunge una nuova migrazione per mantenere storia e checksum coerenti.
Flyway registra normalmente cosa?|Cronologia delle migrazioni e checksum|Password utenti in chiaro|Ogni richiesta HTTP|La cronologia consente verifica delle migrazioni applicate.
Va testato soltanto il database vuoto?|No|Sì|Solo se usi SQL|Serve verificare anche upgrade con dati esistenti.
ddl-auto=update è una strategia completa di migrazione riproducibile?|No|Sì|Solo con Hibernate|Migrazioni esplicite documentano e verificano evoluzione schema e dati.''')
add('spring','spring-actuator','Actuator, metriche e logging','Operatività',
'''Actuator espone strumenti operativi come health e metriche. Non tutti gli endpoint devono essere pubblici: configura esposizione e autorizzazione. Micrometer fornisce misure interoperabili; evita tag con cardinalità elevata come id di ogni utente.
Log strutturati e correlation id aiutano a seguire una richiesta, ma non devono includere credenziali. Readiness e liveness rappresentano condizioni diverse. Misura tempi, errori e saturazione; un’app che risponde 200 può comunque essere lenta o non pronta a servire correttamente.''',
'// application.yml\n// management.endpoints.web.exposure.include: health,info\n// Proteggere gli endpoint di gestione secondo ambiente e necessità.\n// Non usare userId come tag di ogni metrica.',
'Osservare salute e prestazioni senza esporre dettagli interni.',
'Esporre ogni endpoint Actuator pubblicamente; loggare token; usare id unici come label metriche.',BOOT+'actuator/endpoints.html',
'''Tutti gli endpoint Actuator dovrebbero essere pubblici?|No|Sì|Solo in produzione|L’esposizione va limitata e protetta in base alla sensibilità.
Un tag metrica con ogni userId può causare cosa?|Cardinalità elevata|Miglioramento gratuito della memoria|Eliminazione delle query|Molte combinazioni di tag aumentano costo e risorse della telemetria.
Liveness e readiness sono identiche?|No|Sì|Solo senza database|Liveness riguarda la vitalità, readiness la capacità di servire traffico.
I token di accesso dovrebbero comparire nei log?|No|Sì per debug normale|Solo con livello INFO|Le credenziali non devono essere registrate nei log.''')
add('spring','spring-cache','Cache, Redis e invalidazione','Integrazioni',
'''La cache conserva risultati per ridurre calcoli o accessi ripetuti. @Cacheable consulta e popola, @CacheEvict invalida e @CachePut aggiorna tramite esecuzione del metodo. Il modello proxy richiede chiamate intercettabili, con gli stessi limiti di self-invocation.
Una cache non è automaticamente la fonte autorevole. Definisci chiave, TTL e invalidazione dopo modifiche; considera dati per utente e permessi. Redis non elimina inconsistenza o stampede. Non memorizzare risultati sensibili sotto una chiave condivisa fra utenti.''',
'@Cacheable(cacheNames="topics", key="#id")\npublic TopicDto find(long id) { return load(id); }\n@CacheEvict(cacheNames="topics", key="#id")\npublic void rename(long id, String title) { update(id, title); }',
'Ridurre letture ripetute quando è accettabile una policy di freschezza definita.',
'Non invalidare dopo scrittura; usare chiavi che ignorano l’utente; confondere cache con database autorevole.',SPRING+'integration/cache/annotations.html',
'''@Cacheable serve normalmente a cosa?|Riutilizzare e memorizzare il risultato|Cancellare sempre dati SQL|Aprire un thread per richiesta|Il risultato può essere riutilizzato quando la chiave è già presente.
Dopo una modifica cosa va considerato?|Invalidazione o aggiornamento della cache|Solo il nome del metodo|Nessuna operazione se usi Redis|La cache deve seguire una policy di coerenza con la fonte.
Una chiave condivisa può esporre dati di un altro utente?|Sì|No|Solo se Redis è remoto|La chiave deve includere il contesto necessario e rispettare autorizzazione.
Self-invocation attiva sempre le annotazioni cache nel modello proxy?|No|Sì|Solo con @Service|La chiamata deve passare dal proxy per essere intercettata.''')
add('spring','spring-async','@Async, scheduling e contesto','Integrazioni',
'''@Async esegue una chiamata intercettata tramite un executor configurato; richiede abilitazione e un modello di chiamata compatibile col proxy. @Scheduled pianifica operazioni, ma più istanze dell’app possono eseguirle contemporaneamente.
I ThreadLocal e il contesto transazionale non si trasferiscono automaticamente a un nuovo thread. Gestisci errori, code, shutdown e backpressure. Un metodo async void richiede una strategia esplicita per errori; per operazioni affidabili oltre il processo può servire una coda persistente, non solo un executor.''',
'@EnableAsync\n@Configuration class AsyncConfig {}\n@Async("studyExecutor")\npublic CompletableFuture<String> prepareReport() {\n    return CompletableFuture.completedFuture("pronto");\n}',
'Eseguire lavoro fuori dal thread chiamante con limiti e osservabilità.',
'Chiamare internamente @Async aspettandosi background; presumere che la transazione del chiamante valga sul worker.',SPRING+'integration/scheduling.html',
'''@Async richiede una chiamata intercettata dal proxy nel modello standard?|Sì|No|Solo se ritorna void|Self-invocation può aggirare l’intercettazione asincrona.
La transazione del chiamante passa automaticamente a un nuovo thread?|No|Sì|Solo con CompletableFuture|Il contesto è legato alla strategia e normalmente al thread corrente.
Due istanze con @Scheduled possono eseguire lo stesso lavoro?|Sì|No|Solo se hanno lo stesso PID|Serve coordinamento se il lavoro deve essere unico tra istanze.
Un executor in memoria garantisce consegna dopo crash?|No|Sì|Solo se usa 10 thread|L’affidabilità oltre il processo richiede persistenza e una strategia dedicata.''')
add('spring','spring-events','Eventi applicativi e consistenza','Integrazioni',
'''Gli eventi applicativi permettono notificare un fatto a collaboratori disaccoppiati. Per default la pubblicazione e l’ascolto non implicano una coda durabile. @TransactionalEventListener può associare l’esecuzione a una fase della transazione.
Un effetto esterno dopo commit può fallire anche se il database è già aggiornato. Per consegna affidabile puoi usare un outbox persistente e un worker idempotente. Non promettere exactly-once end-to-end senza considerare deduplicazione e finestre di guasto.''',
'publisher.publishEvent(new TopicCreated(topicId));\n@TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)\npublic void onCreated(TopicCreated event) { /* effetto successivo, gestire fallimenti */ }',
'Separare notifiche da modifiche di dominio e progettare effetti affidabili.',
'Credere che ApplicationEventPublisher sia una coda persistente; perdere eventi esterni dopo commit senza retry.',SPRING+'data-access/transaction/event.html',
'''Un evento Spring è automaticamente persistente dopo crash?|No|Sì|Solo se è record|Gli eventi applicativi in-process non sono una coda durabile.
AFTER_COMMIT significa esecuzione dopo quale fase?|Commit riuscito della transazione|Ogni flush anche fallito|Compilazione Java|Il listener è legato al completamento positivo della transazione pertinente.
Un’email dopo commit può fallire lasciando dati già confermati?|Sì|No|Solo con Gmail|L’effetto esterno non è automaticamente atomico col database.
Un outbox può essere utile per cosa?|Registrare eventi da consegnare in modo affidabile|Eliminare ogni duplicato senza logica|Sostituire TLS|Outbox conserva intenti di consegna e va accompagnato da retry e idempotenza.''')
add('spring','spring-webclient','Client HTTP e resilienza','Integrazioni',
'''Spring offre client per modelli diversi: RestClient per chiamate sincrone moderne e WebClient per flussi reattivi. Non scegliere reattività solo per il nome: considera il modello dell’app e delle dipendenze. Imposta timeout e limiti del trasporto pertinente.
Gestisci status, formato, retry limitati e idempotenza. Circuit breaker e bulkhead hanno scopi distinti: interrompere chiamate a servizi degradati e isolare consumo di risorse. Non ritentare indiscriminatamente errori di validazione o comandi non idempotenti.''',
'RestClient client = RestClient.create("https://api.example.org");\nTopicDto dto = client.get().uri("/topics/{id}", id)\n    .retrieve().body(TopicDto.class);\n// Configurare il request factory per timeout coerenti.',
'Integrare API esterne con un modello coerente e guasti gestibili.',
'Bloccare un event loop con I/O sincrono; usare retry senza limiti; non configurare timeout.',SPRING+'integration/rest-clients.html',
'''RestClient è principalmente quale tipo di client?|Sincrono|Sempre reattivo non bloccante|Solo WebSocket|RestClient offre un’API sincrona moderna.
WebClient appartiene soprattutto a quale modello?|Reattivo|Solo JDBC|Solo XML statico|WebClient supporta flussi reattivi e un modello non bloccante pertinente.
Retry senza limite è una buona policy?|No|Sì|Solo su 500|Serve limitare tentativi e considerare carico e idempotenza.
Un circuit breaker elimina la necessità di timeout?|No|Sì|Solo con HTTP|Il timeout resta necessario per limitare durata e risorse delle singole chiamate.''')
add('spring','spring-deployment','Packaging, container e rilascio','Operatività',
'''Un’app Boot può essere distribuita come jar eseguibile con server embedded. Un container impacchetta runtime e applicazione, ma non sostituisce configurazione, migrazioni e osservabilità. Usa versioni riproducibili e un utente non privilegiato quando possibile.
Al rilascio controlla readiness, graceful shutdown e compatibilità schema. Non includere segreti nell’immagine né affidare la persistenza al filesystem effimero del container. Le risorse devono essere dimensionate e i log raccolti all’esterno del processo.''',
'./mvnw verify\njava -jar target/study-app.jar\n// In un container: configurazione e segreti dall’ambiente,\n// database persistente separato, health check e shutdown controllato.',
'Distribuire backend mantenendo configurazione, dati e operazioni controllabili.',
'Mettere password nel Dockerfile; salvare il database nel layer effimero; distribuire senza verificare migrazioni.',BOOT+'packaging/container-images/index.html',
'''Un jar Boot può includere un server embedded?|Sì|No|Solo con Android|Il packaging eseguibile permette avvio standalone con il server configurato.
I segreti dovrebbero essere incorporati nell’immagine container?|No|Sì|Solo se privata|Vanno forniti tramite un canale esterno adeguato.
Filesystem effimero del container è una strategia di persistenza sufficiente?|No|Sì|Solo per MySQL|I dati persistenti richiedono storage durevole e gestione separata.
Readiness serve a indicare cosa?|Se l’istanza può servire traffico|Se ogni utente è admin|Se il sorgente compila|La prontezza operativa governa l’invio di richieste all’istanza.''')
add('spring','spring-reactive','MVC, WebFlux e backpressure','Approfondimenti',
'''Spring MVC usa il modello Servlet; WebFlux supporta elaborazione reattiva con Publisher come Mono e Flux. La differenza non coincide semplicemente con più o meno veloce. Il vantaggio dipende dal carico e dall’intera catena di dipendenze non bloccanti.
JDBC è bloccante: spostarlo dentro un Mono non lo rende non bloccante. Backpressure esprime domanda dal consumatore, ma il sistema richiede comunque limiti. Non chiamare block su un event loop. Mantieni il modello MVC quando risolve bene il caso, senza introdurre complessità ingiustificata.''',
'// Esempio WebFlux concettuale:\n@GetMapping("/topic/{id}")\nMono<TopicDto> find(@PathVariable long id) {\n    return reactiveRepository.findById(id).map(this::toDto);\n}',
'Valutare quando un flusso reattivo è coerente con I/O e dipendenze.',
'Inserire JDBC bloccante sull’event loop; usare block senza comprendere il contesto; scegliere WebFlux soltanto per curriculum.',SPRING+'web/webflux.html',
'''Inserire una chiamata JDBC in Mono la rende automaticamente non bloccante?|No|Sì|Solo con map|Il lavoro JDBC resta bloccante e va considerato nel modello di esecuzione.
Mono rappresenta normalmente quale cardinalità?|Zero o un risultato|Sempre molti risultati|Solo nessun risultato|Mono descrive un flusso con al massimo un elemento.
Flux può rappresentare più elementi?|Sì|No|Solo String|Flux supporta sequenze di elementi.
WebFlux è sempre più veloce di MVC?|No|Sì|Solo con HTTPS|La scelta dipende da carico, dipendenze e costi dell’architettura.''')


def build():
    catalog={'revision':1,'scope':'Java SE 21 e Spring Boot 3.5: catalogo introduttivo e intermedio con approfondimenti. Non è l’intera specifica Java né tutto l’ecosistema Spring.','subjects':[{'id':'java','title':'Java'},{'id':'spring','title':'Spring Boot'}],'topics':TOPICS}
    path=Path(__file__).resolve().parents[1]/'app/src/main/assets/study_catalog.json'
    path.write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
    print(f'{len(TOPICS)} argomenti; {sum(q["kind"]=="QUIZ" for t in TOPICS for q in t["questions"])} quiz; {sum(q["kind"]=="RECALL" for t in TOPICS for q in t["questions"])} ripassi')
if __name__=='__main__':build()
