# Medicine Safety Checker

Medicine Interaction & Dosage Safety Checker — a Java mini full-stack student
project. Checks medicines for unsafe dosages and known drug-drug
interactions, with a Spring Boot backend (REST API + a JSP/Servlet website)
and MongoDB for storage.

## Modules
- `core` — plain Java OOP domain model (no framework dependencies)
- `backend` — Spring Boot app: REST API + Spring Data MongoDB, and a
  JSP/Servlet website UI (`PrescriptionServlet` at `/check`)

## Requirements
- Java 25+
- A MongoDB instance (Atlas or local)

No local Maven install is required — use the included wrapper (`mvnw` /
`mvnw.cmd`), which downloads the right Maven version automatically.

## Configuration
The MongoDB connection string is read from the `MONGODB_URI` environment variable
(defaults to `mongodb://localhost:27017/medicine_safety_checker`).
See `.env.example`. Do **not** commit real credentials — `.env` and
`application-local.properties` are git-ignored.

## Data
The interaction dataset is the Kaggle "Drug Interactions" CSV. Download it
and place it at `data/raw/db_drug_interactions.csv` (this path is
git-ignored since the file is large and easily re-downloaded). Small,
cleaned/derived files that the app actually ships with live in
`data/seed/` and are committed.

## Build & run
```
./mvnw clean install                   # build all modules
./mvnw -pl backend spring-boot:run     # run the backend + website
```

Then open `http://localhost:8080/check` in a browser for the website UI.
The JSON REST API is still available under `http://localhost:8080/api/...`.

## Environment file and MongoDB Atlas

Copy `.env.example` to `.env` at the repository root and enter your own URI.
Spring Boot loads this ignored file automatically whether the app starts from
the root or `backend/`; exporting variables manually is not required. Real
environment variables take precedence over file values. Use plain `KEY=value`
lines without `export` or surrounding quotes. `.env` is not pushed to Git.

`MONGODB_URI` selects the MongoDB server. `MONGODB_DATABASE` selects the database
and defaults to `medicine_safety_checker`, even when an Atlas URI ends in `/`.
Set it to the actual database containing your application collections. The app
expects `medicines`, `interactions`, `patients`, `prescriptions`, and `doctors`
documents matching the Java models; an arbitrary Kaggle CSV import may require
mapping to that schema before the application can use it.

For an existing Atlas dataset, leave `APP_SEED_ENABLED=false`. This prevents the
medicine/interaction seed synchronization and patient demo seeding from writing
over shared data. Set it to `true` only when you intend to initialize or refresh
the project demo dataset. Optional doctor provisioning is controlled separately
by the `MSC_DOCTOR_*` settings. An existing doctor username is never reset.

Windows: run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start.ps1`
or choose the VS Code build task. This uses your installed Java and Maven wrapper,
builds/tests the project, then starts the app. MongoDB Atlas needs no local
MongoDB server. For a local URI, start your own local MongoDB first.

An Atlas authentication failure means the cluster rejected the database login.
Check the user in Atlas **Database Access**, its password and permissions, and
URL-encode reserved characters in credentials. Network timeouts are a separate
case: check Atlas **Network Access** for the client IP. Credentials are never
included in committed config, scripts or documentation.

## Doctor authentication

Browser pages require a doctor session. Visit `/login`, then use Patients or
Check in the shared navigation. Logout is a CSRF-protected POST; GET `/logout`
shows a confirmation page. Sessions expire after 30 minutes of inactivity.
All HTML forms include a session CSRF token. REST API and CSS paths are excluded
from browser authentication as requested; API routes remain public.

To provision a doctor before starting the backend, set environment variables:
`MSC_DOCTOR_USERNAME`, `MSC_DOCTOR_PASSWORD`, `MSC_DOCTOR_NAME`, and
`MSC_DOCTOR_SPECIALISATION`. Username is trimmed and converted to lowercase.
The password must contain at least 12 characters and fit in BCrypt's 72-byte
UTF-8 limit. The bootstrap inserts a missing account only; it never resets an
existing password. The doctors collection has a unique username index and stores
only a BCrypt password hash. No signup page or default password is included.
For this machine, the existing launcher loads a local bootstrap configuration
from `../local-tools/doctor-bootstrap.ps1`, outside the repository.

New approved and rejected patient prescriptions record the session doctor's id
and name. History entries created before authentication show "Not recorded".
Use HTTPS and set `server.servlet.session.cookie.secure=true` when deploying.

## Patient management and reporting

- `GET /patients?q=...` searches patient names and IDs, ignoring case.
- `GET/POST /patients/add` creates a patient; `GET/POST /patients/{id}/edit`
  edits details without replacing active medicines or history.
- `GET/POST /patients/{id}/prescribe` checks and saves a prescription. Allergies
  are checked before dosage and interactions. Both approved and rejected checks
  record the doctor and rejection reason; only approved medicines become active.
- `POST /patients/{id}/stop` removes the submitted `medicineName` and records
  one dated `STOPPED` event with the doctor. Repeated stop requests are idempotent.
- `GET /patients/{id}/report` downloads UTF-8 CSV with patient details, active
  medicines and full history. ReportServlet is reached through a named servlet
  dispatch because servlet URL patterns cannot express `/patients/{id}/report`.

All modifying browser forms require the session CSRF token. Patient names are
required (100 characters maximum); age must be a whole number from 0 through
130, and weight must be finite, positive and at most 500 kg. Allergies accept
comma- or pipe-separated entries and are deduplicated ignoring case.

## Allergy checks and audit trail

Medicines include `drugClass` in the core model, MongoDB documents and seed CSV.
Recorded allergies match either a medicine's name or drug class, ignoring case
and surrounding whitespace. The core `AllergyConflictException` is checked.
Existing MongoDB medicines receive missing drug classes on startup; custom
nonempty classifications are preserved. The existing six-column medicine CSV
format remains supported alongside the seven-column format with `drugClass`.

The standalone `/check` form accepts allergies. The REST prescription check
also accepts `allergies` and an optional `patientId`; when a patient is selected,
their recorded allergies and active medicines are included automatically.
Allergy results use the `ALLERGY_CONFLICT` status. The API check evaluates a
request without saving a prescription.

Every safety check through the browser or REST checking endpoints writes a UTC
timestamp, patient ID (if available), medicine and outcome to `logs/checks.log`
relative to the backend working directory. The synchronized logger creates the
directory, prevents interleaved thread writes and removes line breaks from log
fields. Configure a different file with `app.audit.path`. Invalid parsed check
inputs are logged as `INVALID_INPUT`. Application logs remain under the existing
local launcher directory. Logs and local credentials are excluded from Git.

## Severity overrides and seed expansion

Edit `data/seed/severity_overrides.csv` with columns
`medicineNameA,medicineNameB,severity`. Medicine names and aliases are matched
ignoring case, and pair order does not matter. Values may be `MAJOR`/`SEVERE`,
`MODERATE`, or `MINOR`/`LOW`. Manual rows supersede keyword classification.
The shipped file has only a header and comments; add reviewed override rows.
Invalid names, invalid severities and conflicting overrides stop generation
before the output file is replaced.

From the repository root, after building, run:

```
java -cp core/target/classes com.medicinesafetychecker.core.tools.InteractionCsvBuilder
```

This reads `data/raw/db_drug_interactions.csv` and writes
`data/seed/interactions.csv`. Optional positional arguments specify raw CSV,
medicine CSV, output CSV and overrides CSV, in that order. The raw dataset is
not required to run the app with its shipped seed interactions.

On restart, missing seed medicines are added and seed interaction pairs are
inserted or refreshed by unordered names, including their severity. Existing
pair IDs and extra database-only pairs are retained. The graph loads after
these updates, so regenerated overrides take effect on the next restart.
Patient records and prescription histories are never reseeded over existing data.

## Verification

Run `mvnw.cmd clean install` on Windows (or `./mvnw clean install` elsewhere).
The tests cover authentication/CSRF, doctor attribution, allergy order, numeric
validation, legacy data migration, duplicate stop concurrency, concurrent audit
writes, CSV quoting/formula escaping and severity override precedence.
Patient writes are serialized within this single application instance. MongoDB
standalone does not provide cross-collection transactions; application-level
compensation removes a new history entry if its patient update fails.

## Interactive demo and refreshed UI

The local demo login on this machine is username `doctor`, password `doctor`.
The local bootstrap configuration explicitly sets `MSC_ALLOW_DEMO_PASSWORD=true`
to allow this exact demo credential pair if provisioning a new local database.
The default bootstrap password requirement remains 12 characters; the opt-in
does not accept other short credentials and never resets existing accounts.
The requested existing local account was updated separately, retaining its ID.

Visit `/samples` for a guide and links that prefill safe, allergy, overdose and
interaction examples. The sample patient buttons on Add patient prefill Maya,
Arjun and Neha. They never submit forms automatically. Sample scenarios are
illustrations of the project dataset, not treatment recommendations.

The JSP interface uses responsive teal, violet and coral styling, patient summary
cards, live filtering within the current patient list, a current-medicine filter,
selection counts, daily-dose previews, password visibility, save notifications
and a stop confirmation dialog. Native forms and server validation remain usable
without JavaScript. Static `/js/` resources are public alongside CSS so the
login page can load its interactions before authentication.
