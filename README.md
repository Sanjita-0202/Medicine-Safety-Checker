# Medicine Safety Checker

Medicine Interaction & Dosage Safety Checker — a Java mini full-stack student
project. Checks medicines for unsafe dosages and known drug-drug
interactions, with a Spring Boot REST backend, a JavaFX desktop client, and
MongoDB for storage.

## Modules
- `core` — plain Java OOP domain model (no framework dependencies)
- `backend` — Spring Boot REST API + Spring Data MongoDB
- `desktop` — JavaFX client

## Requirements
- Java 21+
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
./mvnw -pl backend spring-boot:run     # run the backend API
./mvnw -pl desktop javafx:run          # run the JavaFX client
```
