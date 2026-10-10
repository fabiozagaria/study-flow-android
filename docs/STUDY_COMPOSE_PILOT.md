# Studio: Compose pilot, version 1.9

Approved scope: the real lesson → quiz → result flow, before extending the style to the catalog or other application sections. Java remains the language of the domain, Room entities, repository and ViewModel. Kotlin is used for Compose presentation only.

## Integration

- Existing StudyActivity and FragmentManager navigation remain in place. Topic reading and session presentation return ComposeView with DisposeOnViewTreeLifecycleDestroyed and a stable view ID for saved UI state.
- Kotlin/Compose compiler 2.1.21 and BOM 2025.05.01 work with the existing AGP 8.9.2, Gradle 8.11.1, JDK 17 and compile SDK 35. No toolchain or target SDK migration is introduced.
- Java LiveData supplies material, session snapshots and busy state. Grading, shuffling, confirmation, completion and the error queue remain in StudyRepository/LearningDao/QuizRules.
- Presentation state keeps the current item, selection, answer, note and recall reveal state. It survives Activity recreation through Bundle and the Fragment view being destroyed during a theory detour. Confirmed answers remain durable in Room; unconfirmed drafts retain the existing onPause policy, including its abrupt-process-kill limitation.
- The Activity consumes system bars and IME insets; Compose Scaffold consumes no additional window insets. The quiz bottom actions remain outside the scrolling question.
- Theory opened from feedback offers “Riprendi il tentativo”, preserving that attempt. Mixed sessions keep their existing semantics; no question-to-topic inference is made from ID strings.

## Design tokens and interactions

StudyDesign.kt centralizes light/dark colors, typography, shapes, spacing and reusable panels. Neutral surfaces use one blue accent (#245BBB light / #A3C2FF dark). Green/red/amber appear only for outcomes, always accompanied by labels. Body text is 17sp/27sp; titles 21–28sp; corners 12–20dp; borders 1dp; elevation 0–2dp; spacing 4/8/12/16/20/24dp. No dynamic multi-accent palette or gradients are introduced in the pilot.

Reading displays prerequisites, concept construction, mechanism and a guided reading of the example. The progress bar describes scroll position, not comprehension or automatic completion. Code emphasis uses a small cached lexical tokenizer, preserving the original text and selection/copy. Use cases and common errors expand through AnimatedVisibility/animateContentSize.

Quiz cards are whole-card radio selections with textual state and 48dp+ targets. animateColorAsState and animateFloatAsState handle selection/progress; AnimatedContent changes question and session stage. Durations are 150–200ms and use Compose's system duration-scale support. Feedback appears after the Java/Room confirmation. Results derive existing outcomes and offer the existing review action; they use LazyColumn for long sessions. Recall retains manual WRONG/PARTIAL/CORRECT evaluation and its rubric.

## Beginner theory edition

On 2026-10-10 the relevant Notion Context Hub and software-engineering roadmap were read. The roadmap records Java foundations/OOP/collections/exceptions/streams as course coverage, not automatic mastery. Concurrency is excluded/paused in the original path; I/O and essential APIs are later items. JPA and Security have evidence with support and open areas; notes mention dirty checking and derived-query understanding. Older “verified” dates (often 2026-08-11) coexist with later notes: absence of evidence must not be treated as proof of inability.

Editorial rules: define prerequisites and terms, explain the problem before the mechanism, follow the actual example, distinguish cause from symptom, relate to backend use cases, and introduce advanced terms progressively. Concurrency receives longer explanations. No personal mastery labels, private notes or automatic adaptive-learning logic are bundled in the app.

All 67 theory explanations use topic-specific beginner bridges in scripts/study_reading_guides.py. Catalog revision 2 changes only theory explanations; all 402 questions, examples, IDs, options, corrections, rubrics and version labels stay identical. CatalogImporter updates existing theory rows in place through @Update when their revision is older. It never REPLACEs referenced materials or rewrites attempts, progress, questions or options. The schema remains version 4.

## Validation gate

Before extending the design: review the installable pilot and light/dark screenshots. Automated checks cover clean APK/test APK builds, unit tests, lint, catalog reproducibility, Room migrations and theory-update preservation, reading/quiz/result/error-review journeys, recreation, theory return, recall text/manual ratings, 1.5x fonts and disabled animations.

TalkBack usability and device-specific performance remain practical review items; semantic headings, radio roles, state labels and live-region feedback are provided, but test execution is not a substitute for a screen-reader review. No main merge is authorized by this pilot approval.

Official references:
- https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- https://developer.android.com/jetpack/androidx/releases/compose-animation
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html
- https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html
