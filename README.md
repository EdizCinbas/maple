# Maple

A lightweight blog application built with Spring Boot.

## Features

- **Admin login** — secure authentication with Spring Security
- **Markdown support** — write posts in Markdown, rendered on the frontend
- **File uploads** — attach images and files to posts (up to 50 MB)
- **Media manager** — view and manage uploaded files
- **H2 database** — embedded file-based database, zero setup required
- **Thymeleaf templates** — server-side rendered pages with Bootstrap styling

## Requirements

- Java 21+
- Gradle (or use the included `./gradlew` wrapper)

## Run in development

```bash
./gradlew bootRun
```

Then open [http://localhost:8080](http://localhost:8080).

Default admin credentials are seeded on startup (see `SeedData.java`).

## Build a JAR

```bash
./gradlew build
java -jar build/libs/maple-*.jar
```

## H2 Console

The database console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

- **JDBC URL:** `jdbc:h2:file:./data/blogapp`
- **Username:** `admin`
- **Password:** `password`

> ⚠️ These credentials are set in `application.properties` and **should be changed** before any deployment.

## Configuration

Key settings in `src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `app.upload.dir` | `./uploads` | Directory for uploaded files |
| `spring.servlet.multipart.max-file-size` | `50MB` | Max individual file size |

