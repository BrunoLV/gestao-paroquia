# calendario-paroquia Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-15

## Active Technologies
- PostgreSQL compartilhado (schema de calendario + tabelas externas somente leitura) (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x (Web, Validation, Security, Data JPA), Flyway, PostgreSQL driver, OpenAPI tooling (001-parish-calendar-api)
- PostgreSQL compartilhado; schema do calendario proprio da API e leitura somente de `usuarios`, `organizacoes`, `membros_organizacao` (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x, Spring Web, Spring Validation, Spring Security (RBAC), Spring Data JPA, Flyway, OpenAPI 3.0.3 (001-parish-calendar-api)
- PostgreSQL (banco compartilhado; escrita apenas nas tabelas da API e leitura de `organizacoes`, `usuarios`, `membros_organizacao`) (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x (Web, Validation, Security, Data JPA), Flyway, PostgreSQL driver, OpenAPI 3.0.3 tooling (001-parish-calendar-api)
- PostgreSQL compartilhado; escrita apenas nas tabelas da API de calendario e leitura somente de `organizacoes`, `usuarios`, `membros_organizacao` (001-parish-calendar-api)
- Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver (002-enum-fields-dto-endpoints)
- PostgreSQL (schema `calendario`) com suporte a migrations Flyway; H2 para testes (002-enum-fields-dto-endpoints)
- PostgreSQL (schema `calendario`) com migrations Flyway; H2 para testes (002-enum-fields-dto-endpoints)

- Java 21 + Spring Boot 3.x (Web, Validation, Data JPA), Spring Security (resource server/JWT), Jackson, Flyway, PostgreSQL driver, springdoc-openapi (001-parish-calendar-api)

## Project Structure

```text
app/
	src/
		main/
			java/
			resources/
		test/
			java/
			resources/
specs/
	001-parish-calendar-api/
```

## Commands

- `./gradlew clean test`
- `./gradlew :app:test`
- `./gradlew :app:bootRun`
- `./gradlew :app:build`

## Code Style

Java 21: Follow standard conventions

## Recent Changes
- 002-enum-fields-dto-endpoints: Added Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver
- 002-enum-fields-dto-endpoints: Added Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver
- 001-parish-calendar-api: Added Java 21 + Spring Boot 3.x (Web, Validation, Security, Data JPA), Flyway, PostgreSQL driver, OpenAPI 3.0.3 tooling


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
