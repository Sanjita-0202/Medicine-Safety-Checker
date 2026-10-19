# Medicine Interaction & Dosage Safety Checker

A Java mini full-stack student project (Team 3). Checks medicines for unsafe
dosages and known drug-drug interactions.

## Stack

- **Java 21**, built with **Maven** (multi-module)
- `core` — plain Java OOP domain classes (Medicine, Interaction, dosage rules).
  No framework dependencies; this is the module to point at when explaining
  the OOP design in the viva.
- `backend` — Spring Boot REST API + Spring Data MongoDB. Depends on `core`.
- `desktop` — JavaFX client UI. Depends on `core`, talks to `backend` over
  HTTP.
- **MongoDB Atlas** for persistence.
- Source data: Kaggle CSVs (drug interactions dataset), see `data/`.
- **JUnit 5** for tests.

## Project layout

```
pom.xml          parent POM (aggregator, dependency management)
core/            domain model + unit tests, no Spring/JavaFX deps
backend/         Spring Boot app, REST controllers, Mongo repositories
desktop/         JavaFX app, calls backend REST API
data/
  raw/           downloaded Kaggle CSVs — gitignored, not committed
  seed/          small cleaned/derived data files — committed
```

## Rules

- Keep code **simple and well-commented** — this is a student project
  presented at a viva, not production software. Prefer clarity over clever
  abstractions.
- **Never commit secrets.** The MongoDB URI goes in `application-local.properties`
  (gitignored) or the `MONGODB_URI` env var — never in `application.properties`
  or any tracked file.
- `data/raw/` is gitignored (large, re-downloadable from Kaggle). `data/seed/`
  holds small cleaned files that ARE committed.
- **No `Co-Authored-By` lines in commits.**

## Build

```
./mvnw clean install      # build all modules
./mvnw -pl backend spring-boot:run   # run the backend API
./mvnw -pl desktop javafx:run        # run the JavaFX client
```

## Configuration

Copy `.env.example` to `.env` (gitignored) or create
`backend/src/main/resources/application-local.properties` with your real
`MONGODB_URI`, then run the backend with `--spring.profiles.active=local`.
