# Maple

A lightweight, self-hosted blog built with Spring Boot 3.

## Features

- **Admin login** — secure authentication with Spring Security
- **Markdown support** — write posts in Markdown with GFM tables and image attributes
- **Media manager** — view and manage uploaded files
- **Dual database** — H2 for development (zero setup), PostgreSQL for production
- **Thymeleaf templates** — server-side rendered pages with Bootstrap styling
- **Deployment guide** — see [oracle-vm-setup.md](oracle-vm-setup.md) *(personally used to deploy, but provided as-is with no guarantee or warranty — use at your own risk)*

## Tech Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| Persistence | Spring Data JPA + PostgreSQL (prod) / H2 (dev) |
| Templates | Thymeleaf |
| Markdown | commonmark-java 0.22 |
| Build | Gradle |

## Requirements

- Java 21+
- Gradle (or use the included `./gradlew` wrapper)

## Run in Development

```bash
./gradlew bootRun
```

Then open [http://localhost:8080](http://localhost:8080).

Default admin credentials are seeded on startup (see `SeedData.java`). MUST be set as environment variables in ```application.properties```

## Build a JAR

```bash
./gradlew bootJar
java -jar build/libs/maple-*.jar
```

## H2 Console (dev only)

Available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

- **JDBC URL:** `jdbc:h2:file:./data/blogapp`
- **Username:** `admin`
- **Password:** `password`

> ⚠️ These credentials are set in `application.properties` and **must be changed** before any deployment.

## Production Deployment

Full instructions for deploying to an Oracle Cloud Always Free VM (Oracle Linux 9, ARM64) are in **[oracle-vm-setup.md](oracle-vm-setup.md)**, including PostgreSQL setup, Nginx, HTTPS, systemd, and database backup.

> ⚠️ **Disclaimer:** I have personally used this exact guide to deploy Maple, but it is provided as-is with **no guarantee, no warranty, and no claim of completeness or security**. You are solely responsible for your own server. Use at your own risk.

The production Spring profile (`SPRING_PROFILES_ACTIVE=prod`) reads all secrets from environment variables — nothing sensitive is hardcoded. Required variables:

| Variable | Description |
|---|---|
| `DB_NAME` | PostgreSQL database name |
| `DB_USERNAME` | PostgreSQL user |
| `DB_PASSWORD` | PostgreSQL password |
| `UPLOAD_DIR` | Absolute path for uploaded files |
| `SEED_ADMIN_EMAIL` | Admin account e-mail (seeded on first run) |
| `SEED_ADMIN_PASSWORD` | Admin account password (seeded on first run) |
| `SEED_ADMIN_FIRST_NAME` | Admin first name |
| `SEED_ADMIN_LAST_NAME` | Admin last name |

## Configuration

Key settings in `src/main/resources/application.properties` (development):

| Property | Default | Description |
|---|---|---|
| `app.upload.dir` | `./uploads` | Directory for uploaded files |
| `app.upload.max-bytes` | `52428800` | Max file size in bytes (50 MB) |

Production overrides live in `src/main/resources/application-prod.properties`.
