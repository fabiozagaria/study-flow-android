# Studio — copertura del catalogo

Catalogo originale in italiano, revisione 2: spiegazioni con prerequisiti, costruzione del concetto e lettura guidata dell’esempio; domande e rubriche invariate dalla revisione 1. Java SE 21 (esempi Java 17 salvo indicazione), Spring Boot 3.5 / Spring Framework 6 / Jakarta. Gli esempi backend sono didattici e non vengono eseguiti dall’app Android.

67 argomenti, 268 quiz a scelta singola con tre opzioni, 134 domande aperte con criteri. Le domande aperte sono autovalutate: non esiste un correttore semantico automatico.

Questa è una copertura iniziale estesa, non l’intera specifica Java o tutto l’ecosistema Spring. Non comprende ancora JavaFX, JNI/FFM, implementazione dei collector GC, Spring Batch, Spring Integration, Spring Cloud e Spring Authorization Server. Approfondimenti specialistici richiedono capitoli e test ulteriori.

| Materia | Categoria | Argomento | Quiz | Ripassi |
| --- | --- | --- | ---: | ---: |
| java | Fondamenti | JDK, bytecode e JVM | 4 | 2 |
| java | Fondamenti | Primitivi, wrapper e conversioni | 4 | 2 |
| java | Fondamenti | Scope, operatori e controllo di flusso | 4 | 2 |
| java | Fondamenti | Metodi, varargs e passaggio per valore | 4 | 2 |
| java | Fondamenti | String, immutabilità e text block | 4 | 2 |
| java | OOP | Classi, costruttori e incapsulamento | 4 | 2 |
| java | OOP | Ereditarietà, override e polimorfismo | 4 | 2 |
| java | OOP | Interfacce, classi astratte e composizione | 4 | 2 |
| java | OOP | equals, hashCode e identità | 4 | 2 |
| java | OOP | Record, enum e sealed class | 4 | 2 |
| java | Robustezza | Eccezioni e gestione delle risorse | 4 | 2 |
| java | Collezioni | Generics, wildcard e type erasure | 4 | 2 |
| java | Collezioni | List, Set, Map e complessità | 4 | 2 |
| java | Collezioni | Comparator, ordinamento e collezioni immutabili | 4 | 2 |
| java | Programmazione funzionale | Lambda e interfacce funzionali | 4 | 2 |
| java | Programmazione funzionale | Stream, pipeline e collect | 4 | 2 |
| java | Programmazione funzionale | Optional e assenza di valore | 4 | 2 |
| java | API e I/O | Date, orari e fusi | 4 | 2 |
| java | API e I/O | File, NIO e charset | 4 | 2 |
| java | API e I/O | JDBC, PreparedStatement e transazioni | 4 | 2 |
| java | JVM | Stack, heap, riferimenti e garbage collection | 4 | 2 |
| java | Concorrenza | Thread, Runnable e ciclo di vita | 4 | 2 |
| java | Concorrenza | synchronized, volatile e atomicità | 4 | 2 |
| java | Concorrenza | Executor, Future e CompletableFuture | 4 | 2 |
| java | Concorrenza | Virtual thread e limiti del parallelismo | 4 | 2 |
| java | JVM | Annotazioni, reflection e proxy | 4 | 2 |
| java | JVM | Package, classpath e moduli JPMS | 4 | 2 |
| java | Qualità | Test unitari, assert e isolamento | 4 | 2 |
| java | Qualità | Maven, Gradle e dipendenze | 4 | 2 |
| java | API e I/O | BigDecimal, precisione e denaro | 4 | 2 |
| java | API e I/O | HTTP client, timeout e gestione degli errori | 4 | 2 |
| spring | Fondamenti | Spring, Spring Boot e auto-configurazione | 4 | 2 |
| spring | Container | IoC, bean e constructor injection | 4 | 2 |
| spring | Container | Component scan, @Bean e selezione delle dipendenze | 4 | 2 |
| spring | Container | Scope, lifecycle e thread safety dei bean | 4 | 2 |
| spring | Configurazione | Proprietà, profili e configurazione tipizzata | 4 | 2 |
| spring | Web e REST | DispatcherServlet e percorso di una richiesta | 4 | 2 |
| spring | Web e REST | REST, metodi HTTP e status | 4 | 2 |
| spring | Web e REST | DTO, binding e serializzazione JSON | 4 | 2 |
| spring | Web e REST | Bean Validation e validazione di dominio | 4 | 2 |
| spring | Web e REST | ControllerAdvice e contratto degli errori | 4 | 2 |
| spring | Persistenza | Entity, identificatori e persistence context | 4 | 2 |
| spring | Persistenza | Spring Data repository e query | 4 | 2 |
| spring | Persistenza | Relazioni, mappedBy e lato proprietario | 4 | 2 |
| spring | Persistenza | Lazy loading, N+1 e fetch plan | 4 | 2 |
| spring | Persistenza | Cascade, orphanRemoval e cancellazioni | 4 | 2 |
| spring | Persistenza | Dirty checking, flush e commit | 4 | 2 |
| spring | Transazioni | @Transactional, rollback e self-invocation | 4 | 2 |
| spring | Transazioni | Propagation, isolation e confini transazionali | 4 | 2 |
| spring | Transazioni | @Version, lock e aggiornamenti concorrenti | 4 | 2 |
| spring | Persistenza | Paginazione, sorting e query efficienti | 4 | 2 |
| spring | Sicurezza | SecurityFilterChain e autenticazione | 4 | 2 |
| spring | Sicurezza | PasswordEncoder, login e password | 4 | 2 |
| spring | Sicurezza | Ruoli, method security e ownership | 4 | 2 |
| spring | Sicurezza | JWT e OAuth2 Resource Server | 4 | 2 |
| spring | Sicurezza | Refresh token, cookie e revoca | 4 | 2 |
| spring | Sicurezza | CSRF, CORS e credenziali browser | 4 | 2 |
| spring | Sicurezza | 401, 403 e test dei permessi | 4 | 2 |
| spring | Testing | SpringBootTest, slice e MockMvc | 4 | 2 |
| spring | Operatività | Flyway e migrazioni del database | 4 | 2 |
| spring | Operatività | Actuator, metriche e logging | 4 | 2 |
| spring | Integrazioni | Cache, Redis e invalidazione | 4 | 2 |
| spring | Integrazioni | @Async, scheduling e contesto | 4 | 2 |
| spring | Integrazioni | Eventi applicativi e consistenza | 4 | 2 |
| spring | Integrazioni | Client HTTP e resilienza | 4 | 2 |
| spring | Operatività | Packaging, container e rilascio | 4 | 2 |
| spring | Approfondimenti | MVC, WebFlux e backpressure | 4 | 2 |

## Aggiornare il materiale

La fonte editoriale è `scripts/build_study_catalog.py`; l’asset JSON è il risultato riproducibile. Eseguire `python3 scripts/build_study_catalog.py` e `python3 scripts/validate_study_catalog.py`. Conservare gli identificativi di argomenti e domande.

L’importazione iniziale è atomica e idempotente. In questa versione avviene solo se il catalogo locale è vuoto: l’aggiornamento di cataloghi già importati è un incremento futuro e deve preservare progressi e storico. Le copie delle domande nei tentativi isolano lo storico da eventuali cambiamenti futuri.
