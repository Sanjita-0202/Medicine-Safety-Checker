# Medicine Interaction & Dosage Safety Checker

A Java mini full-stack student project (Team 3). Checks medicines for unsafe
dosages and known drug-drug interactions.

## Stack

- **Java 25**, built with **Maven** (multi-module)
- `core` — plain Java OOP domain classes (Medicine, Interaction, dosage rules).
  No framework dependencies; this is the module to point at when explaining
  the OOP design in the viva.
- `backend` — Spring Boot REST API + Spring Data MongoDB. Depends on `core`.
  The UI is **JSP + Servlets**: `PrescriptionServlet` is a plain
  `@WebServlet` (registered via `@ServletComponentScan`, not a Spring
  `@Controller`), so the servlet code is clearly visible for the viva. It
  forwards to JSP pages (using JSTL, no scriptlets) under
  `src/main/resources/META-INF/resources/WEB-INF/jsp/`. The JSON REST
  endpoints under `/api/...` still exist alongside it.
- **MongoDB Atlas** for persistence.
- Source data: Kaggle CSVs (drug interactions dataset), see `data/`.
- **JUnit 5** for tests.

## Project layout

```
pom.xml          parent POM (aggregator, dependency management)
core/            domain model + unit tests, no Spring/servlet deps
backend/         Spring Boot app: REST controllers, Mongo repositories,
                 PrescriptionServlet + JSP pages (the website UI)
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
./mvnw -pl backend spring-boot:run   # run the backend + website on http://localhost:8080
```

Open `http://localhost:8080/check` in a browser for the website UI.

## Configuration

Copy `.env.example` to `.env` (gitignored) or create
`backend/src/main/resources/application-local.properties` with your real
`MONGODB_URI`, then run the backend with `--spring.profiles.active=local`.
