# Medicine Safety Checker

Desktop app built with JavaFX, Spring Boot and MongoDB.

## Requirements
- Java 17+
- Maven 3.9+
- A MongoDB instance

## Configuration
The MongoDB connection string is read from the `MONGODB_URI` environment variable
(defaults to `mongodb://localhost:27017/medicine_safety_checker`).
See `.env.example`. Do **not** commit real credentials — `.env` and
`application-local.properties` are git-ignored.

## Run
```
mvn javafx:run
```
