# 🔗 URL Shortener Service

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?logo=swagger&logoColor=black)](https://swagger.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-JUnit%205%20%2B%20Testcontainers-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)

> A **production-grade URL shortening service** with click analytics, geo/device tracking, custom slugs, and rate limiting — built with **Spring Boot 3.4** and **Java 21** for learning and portfolio demonstration.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Current Status — Phase 1](#-current-status--phase-1-foundation)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
  - [Verifying the Setup](#verifying-the-setup)
  - [Running Tests](#running-tests)
- [API Endpoints](#-api-endpoints)
  - [Phase 1 — Available Now](#phase-1--available-now)
  - [Phase 2+ — Planned](#phase-2--planned)
- [Configuration](#-configuration)
  - [Application Profiles](#application-profiles)
  - [Custom Properties](#custom-properties)
  - [Environment Variables (Production)](#environment-variables-production)
- [Testing Strategy](#-testing-strategy)
- [Phase 1 Detailed Checklist](#-phase-1-detailed-checklist)
- [Java 21 Features Used](#-java-21-features-used)
- [Development Roadmap](#-development-roadmap)
- [What I Learned](#-what-i-learned)
- [Makefile Commands](#-makefile-commands)
- [Troubleshooting](#-troubleshooting)
- [Future Improvements](#-future-improvements)
- [License](#-license)

---

## 🎯 Overview

This project is a **fully functional URL shortener** designed and built from scratch as a learning exercise and portfolio piece. It goes far beyond a simple redirect service — it includes:

- **Custom & auto-generated slugs** with collision handling
- **Click tracking** with asynchronous event processing
- **Geographic analytics** via MaxMind GeoIP database
- **Device/browser analytics** via Yauaa User-Agent parsing
- **Distributed rate limiting** with Bucket4j + Redis
- **JWT + API Key authentication**
- **Password-protected and expiring links**

The project is being built in **8 phases**, each adding a complete vertical slice of functionality. This README documents **Phase 1: Foundation**, which establishes the entire project skeleton, database schema, infrastructure, and testing framework.

---

## 🏁 Current Status — Phase 1 (Foundation)

```
Phase 1: Foundation & Skeleton     ████████████████████ 100% ✅ COMPLETE
Phase 2: Core URL Shortening       ░░░░░░░░░░░░░░░░░░░░   0% ⬜ NEXT
Phase 3: Redis Caching             ░░░░░░░░░░░░░░░░░░░░   0% ⬜
Phase 4: Click Tracking            ░░░░░░░░░░░░░░░░░░░░   0% ⬜
Phase 5: Authentication            ░░░░░░░░░░░░░░░░░░░░   0% ⬜
Phase 6: Rate Limiting             ░░░░░░░░░░░░░░░░░░░░   0% ⬜
Phase 7: Advanced Features         ░░░░░░░░░░░░░░░░░░░░   0% ⬜
Phase 8: Observability & Deploy    ░░░░░░░░░░░░░░░░░░░░   0% ⬜
```

**Phase 1 delivers:**
- Complete Maven project with all dependencies declared
- 5 JPA entities + 2 enums fully mapped
- 5 repository interfaces with custom analytics queries
- 7 Flyway migration scripts creating all database tables
- Global exception handler with 6 custom exception types
- Java record DTOs for structured error responses
- Spring Security configured (permit-all for Phase 1)
- Redis configuration and template setup
- OpenAPI 3 / Swagger UI integration
- Docker Compose for PostgreSQL 16 + Redis 7
- Testcontainers base class for integration tests
- ArchUnit tests enforcing 6 architectural rules
- Multi-profile YAML configuration (dev/test/prod)
- Java 21 virtual threads enabled
- Makefile with developer-friendly commands

---

## ✨ Features

### ✅ Implemented (Phase 1)

| Feature | Description |
|---------|-------------|
| **Project Skeleton** | Clean package structure following domain-driven layout |
| **JPA Entities** | `User`, `ShortUrl`, `UrlGroup`, `ClickEvent`, `DailyStat` with auditing |
| **Database Migrations** | 7 Flyway scripts — users, url_groups, short_urls, click_events, daily_stats, rate_limit_config, seed data |
| **Repository Layer** | Spring Data JPA repos with custom JPQL queries for analytics aggregation |
| **Error Handling** | `@RestControllerAdvice` with structured JSON errors using Java records |
| **Custom Exceptions** | `ShortUrlNotFoundException`, `SlugAlreadyExistsException`, `ShortUrlExpiredException`, `InvalidUrlException`, `RateLimitExceededException`, `UnauthorizedException` |
| **API Documentation** | SpringDoc OpenAPI 3 with Swagger UI at `/swagger-ui.html` |
| **Security** | Spring Security with stateless session, CSRF disabled, BCrypt encoder |
| **Redis** | `RedisTemplate<String, Object>` with JSON serialization configured |
| **Docker Infrastructure** | PostgreSQL 16 + Redis 7 with health checks and persistent volumes |
| **Testing Foundation** | Testcontainers base class, ArchUnit rules, smoke tests |
| **Virtual Threads** | Java 21 virtual threads enabled via Spring Boot config |
| **CORS** | Configurable allowed origins for frontend integration |
| **Health Endpoint** | `/api/v1/health` returning service status, timestamp, Java version |

### ⬜ Planned (Future Phases)

| Feature | Phase | Description |
|---------|-------|-------------|
| URL CRUD | 2 | Create, read, update, delete short URLs |
| Custom Slugs | 2 | User-defined slugs with validation |
| Auto Slug Generation | 2 | Base62 encoding + NanoID |
| 302 Redirect | 2 | `GET /{slug}` → redirect to original URL |
| Redis Caching | 3 | Cache-aside pattern for slug → URL resolution |
| Click Tracking | 4 | Async fire-and-forget event processing |
| GeoIP Analytics | 4 | Country, city, region from IP address |
| Device Analytics | 4 | Device type, OS, browser from User-Agent |
| Referrer Tracking | 4 | Referrer domain extraction and aggregation |
| Time Series Stats | 4 | Clicks over time with configurable granularity |
| JWT Auth | 5 | Access token (15min) + Refresh token (7d) |
| API Key Auth | 5 | `X-API-Key` header for programmatic access |
| Rate Limiting | 6 | Token bucket per tier (FREE/PRO/ENTERPRISE) |
| Link Expiration | 7 | By date or by max click count |
| Password Protection | 7 | BCrypt-protected link access |
| QR Code Generation | 7 | QR code image for any short URL |
| Bulk Operations | 7 | Create multiple short URLs in one request |
| Prometheus Metrics | 8 | Custom counters for redirects, creates |
| Grafana Dashboards | 8 | Pre-built dashboard JSON |
| Dockerfile | 8 | Multi-stage build with distroless base |
| CI/CD Pipeline | 8 | GitHub Actions: build → test → docker → push |

---

## 🏗️ Architecture

### High-Level System Design

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         URL SHORTENER SYSTEM                            │
│                                                                         │
│  ┌──────────┐                                                           │
│  │  Client   │                                                          │
│  │ (Browser, │                                                          │
│  │  cURL,    │                                                          │
│  │  Postman) │                                                          │
│  └─────┬─────┘                                                          │
│        │                                                                │
│        ▼                                                                │
│  ┌──────────┐    ┌────────────────────────────────────┐    ┌──────────┐ │
│  │  Rate    │───▶│        Spring Boot Application      │───▶│  Redis   │ │
│  │  Limiter │    │                                     │    │  Cache   │ │
│  │ (Ph.6)   │    │  ┌────────────┐  ┌──────────────┐  │    │  + Rate  │ │
│  └──────────┘    │  │ Controllers │──│   Services   │  │    │  Limit   │ │
│                  │  └────────────┘  └──────┬───────┘  │    └──────────┘ │
│                  │                         │          │                  │
│                  │               ┌─────────┼────────┐ │                  │
│                  │               ▼         ▼        ▼ │                  │
│                  │         ┌──────┐  ┌────────┐  ┌───────┐              │
│                  │         │ JPA  │  │ GeoIP  │  │ Yauaa │              │
│                  │         │ Repos│  │ Lookup │  │ Parse │              │
│                  │         └──┬───┘  └────────┘  └───────┘              │
│                  │            │                                          │
│                  └────────────┼──────────────────────────┘               │
│                               │                                         │
│                               ▼                                         │
│                         ┌──────────┐                                    │
│                         │PostgreSQL│                                    │
│                         │    16    │                                    │
│                         └──────────┘                                    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Redirect Flow (Hot Path — Phase 2+)

```
Client          Controller       Redis Cache       Database        Async Listener
  │                  │               │                │                  │
  │  GET /abc123     │               │                │                  │
  │─────────────────▶│               │                │                  │
  │                  │  GET slug     │                │                  │
  │                  │──────────────▶│                │                  │
  │                  │  CACHE HIT ✓  │                │                  │
  │                  │◀──────────────│                │                  │
  │                  │                                │                  │
  │  302 Redirect    │                                │                  │
  │◀─────────────────│                                │                  │
  │                  │  publish click event async (non-blocking)         │
  │                  │─────────────────────────────────────────────────▶│
  │                  │                                │                  │
  │                  │                                │   GeoIP lookup   │
  │                  │                                │   UA parse       │
  │                  │                                │◀─ INSERT click   │
  │                  │                                │◀─ UPDATE count   │
```

### Layered Architecture (Enforced by ArchUnit)

```
┌──────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                   │
│   (REST endpoints, request/response mapping)          │
│   • Cannot be accessed by any other layer             │
│   • Cannot directly access Repository layer           │
├──────────────────────────────────────────────────────┤
│                    SERVICE LAYER                      │
│   (Business logic, validation, orchestration)         │
│   • Accessed only by Controller and Config            │
├──────────────────────────────────────────────────────┤
│                   REPOSITORY LAYER                    │
│   (Data access, custom queries)                       │
│   • Accessed only by Service and Config               │
│   • Must be interfaces                                │
├──────────────────────────────────────────────────────┤
│                    DOMAIN LAYER                       │
│   (Entities, enums, business methods)                 │
│   • No dependency on Service, Controller, or Config   │
└──────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 21 (LTS) | Virtual threads, records, pattern matching, sealed classes |
| **Framework** | Spring Boot | 3.4.1 | Auto-config, embedded server, starters |
| **Build** | Maven | 3.9.9 | Dependency management, build lifecycle |
| **Database** | PostgreSQL | 16 (Alpine) | Primary data store, UUID support, INET type |
| **Cache** | Redis | 7 (Alpine) | URL resolution cache, rate limiting store |
| **ORM** | Hibernate | 6.x (via Spring) | JPA implementation, entity mapping |
| **Migrations** | Flyway | (managed) | Version-controlled database schema |
| **Security** | Spring Security | 6.x (via Spring) | Auth filter chain, password encoding |
| **Auth (Phase 5)** | JJWT | 0.12.6 | JWT token generation and validation |
| **Rate Limiting (Ph.6)** | Bucket4j | 8.14.0 | Token bucket algorithm, Redis-backed |
| **GeoIP (Phase 4)** | MaxMind GeoIP2 | 4.2.1 | IP → country/city/coordinates lookup |
| **UA Parsing (Phase 4)** | Yauaa | 7.28.1 | User-Agent → device/browser/OS |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 | Swagger UI, auto-generated API specs |
| **DTO Mapping** | MapStruct | 1.6.3 | Compile-time entity ↔ DTO mapping |
| **Boilerplate** | Lombok | (managed) | Getters, setters, builders, constructors |
| **Testing** | JUnit 5 | (managed) | Unit and integration test framework |
| **Test Containers** | Testcontainers | (managed) | Real PostgreSQL + Redis in tests |
| **Architecture Tests** | ArchUnit | 1.3.0 | Enforce package dependency rules |
| **API Testing** | REST Assured | (managed) | Fluent HTTP test assertions |
| **Containers** | Docker Compose | 3.9 | Local development infrastructure |

---

## 📂 Project Structure

```
url-shortener-service/
│
├── 📄 pom.xml                          ← Maven config with all dependencies
├── 📄 Makefile                         ← Developer-friendly commands
├── 📄 README.md                        ← This file
├── 📄 .gitignore                       ← Git exclusions
├── 📄 .editorconfig                    ← Consistent code formatting
│
├── 🐳 docker/
│   └── docker-compose.yml              ← PostgreSQL 16 + Redis 7
│
├── ☕ src/main/java/com/nazir/urlshortener/
│   │
│   ├── UrlShortenerApplication.java    ← @SpringBootApplication entry point
│   │
│   ├── 📁 config/                      ← Spring configuration beans
│   │   ├── AsyncConfig.java            ← Virtual thread executor, @EnableAsync
│   │   ├── JpaConfig.java              ← @EnableJpaAuditing
│   │   ├── OpenApiConfig.java          ← Swagger metadata & description
│   │   ├── RedisConfig.java            ← RedisTemplate with JSON serialization
│   │   ├── SecurityConfig.java         ← SecurityFilterChain (permit-all Phase 1)
│   │   └── WebConfig.java              ← CORS mappings from config
│   │
│   ├── 📁 controller/
│   │   └── HealthController.java       ← GET /api/v1/health
│   │
│   ├── 📁 domain/
│   │   ├── 📁 entity/
│   │   │   ├── User.java               ← Users with tier (FREE/PRO/ENTERPRISE)
│   │   │   ├── ShortUrl.java           ← Core entity — slug, URL, expiry, clicks
│   │   │   ├── UrlGroup.java           ← Folders for organizing links
│   │   │   ├── ClickEvent.java         ← Raw click data — geo, device, referrer
│   │   │   └── DailyStat.java          ← Pre-aggregated daily statistics
│   │   └── 📁 enums/
│   │       ├── UserTier.java           ← FREE, PRO, ENTERPRISE
│   │       └── DeviceType.java         ← DESKTOP, MOBILE, TABLET, BOT, UNKNOWN
│   │
│   ├── 📁 dto/
│   │   └── 📁 response/
│   │       └── ErrorResponse.java      ← Java record with nested ValidationError
│   │
│   ├── 📁 exception/
│   │   ├── GlobalExceptionHandler.java ← @RestControllerAdvice — catches all errors
│   │   ├── ShortUrlNotFoundException.java
│   │   ├── SlugAlreadyExistsException.java
│   │   ├── ShortUrlExpiredException.java
│   │   ├── InvalidUrlException.java
│   │   ├── RateLimitExceededException.java  ← Includes retryAfterSeconds
│   │   └── UnauthorizedException.java
│   │
│   ├── 📁 repository/
│   │   ├── UserRepository.java         ← findByEmail, findByApiKey
│   │   ├── ShortUrlRepository.java     ← findBySlug, search, incrementClickCount
│   │   ├── UrlGroupRepository.java     ← findByUserId
│   │   ├── ClickEventRepository.java   ← Analytics: countByCountry, countByDevice...
│   │   └── DailyStatRepository.java    ← findByShortUrlIdAndDateRange
│   │
│   └── 📁 (future packages — created as empty placeholders)
│       ├── service/                    ← Business logic (Phase 2)
│       ├── security/                   ← JWT, API key filters (Phase 5)
│       ├── ratelimit/                  ← Bucket4j interceptor (Phase 6)
│       ├── event/                      ← Click event publishing (Phase 4)
│       ├── scheduler/                  ← Cron jobs (Phase 4)
│       └── util/                       ← Base62, IP extractor (Phase 2)
│
├── 📁 src/main/resources/
│   ├── application.yml                 ← Common config (server, JPA, Flyway, app.*)
│   ├── application-dev.yml             ← Dev: localhost DB/Redis, debug logging
│   ├── application-test.yml            ← Test: minimal config (Testcontainers inject)
│   ├── application-prod.yml            ← Prod: env var references
│   └── 📁 db/migration/
│       ├── V1__create_users_table.sql
│       ├── V2__create_url_groups_table.sql
│       ├── V3__create_short_urls_table.sql
│       ├── V4__create_click_events_table.sql
│       ├── V5__create_daily_stats_table.sql
│       ├── V6__create_rate_limit_config_table.sql
│       └── V7__seed_rate_limit_tiers.sql
│
└── 🧪 src/test/java/com/nazir/urlshortener/
    ├── UrlShortenerApplicationTests.java       ← Context load smoke test
    ├── 📁 integration/
    │   ├── AbstractIntegrationTest.java         ← Testcontainers base class
    │   ├── HealthControllerIT.java              ← Health endpoint returns 200
    │   └── DatabaseConnectionIT.java            ← DB connects + migrations run
    └── 📁 architecture/
        └── ArchitectureTest.java                ← 6 ArchUnit rules
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌──────────────────────┐
│       users           │
├──────────────────────┤
│ id         UUID   PK │─────┐
│ email      VARCHAR    │     │
│ password_hash VARCHAR │     │
│ name       VARCHAR    │     │
│ api_key    VARCHAR UK │     │
│ tier       ENUM       │     │         ┌──────────────────────────┐
│ created_at TIMESTAMP  │     │         │      url_groups           │
│ updated_at TIMESTAMP  │     │         ├──────────────────────────┤
└──────────────────────┘     │    ┌───│ id          UUID   PK     │
                              │    │    │ user_id     UUID   FK     │──┐
                              │    │    │ name        VARCHAR       │  │
                              │    │    │ description TEXT          │  │
                              │    │    │ created_at  TIMESTAMP     │  │
                              │    │    └──────────────────────────┘  │
                              │    │                                   │
                              ▼    ▼                                   │
┌──────────────────────────────────────────────────────────┐          │
│                      short_urls                           │          │
├──────────────────────────────────────────────────────────┤          │
│ id              UUID        PK                            │          │
│ slug            VARCHAR(20) UNIQUE NOT NULL               │          │
│ original_url    TEXT        NOT NULL                      │          │
│ user_id         UUID        FK → users (nullable)        │◀─────────┘
│ group_id        UUID        FK → url_groups (nullable)   │
│ is_active       BOOLEAN     DEFAULT true                 │
│ expires_at      TIMESTAMP   (nullable)                   │
│ password_hash   VARCHAR     (nullable)                   │
│ max_clicks      INTEGER     (nullable)                   │
│ click_count     BIGINT      DEFAULT 0                    │
│ created_at      TIMESTAMP                                │
│ updated_at      TIMESTAMP                                │
└──────────────────────┬───────────────────────────────────┘
                       │
                       │ 1:N
                       ▼
┌──────────────────────────────────────────────────────────┐
│                    click_events                           │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT       PK (IDENTITY)               │
│ short_url_id    UUID         FK → short_urls             │
│ clicked_at      TIMESTAMP    NOT NULL                    │
│ ip_address      VARCHAR(45)                              │
│ country         VARCHAR(2)   ← ISO code                  │
│ city            VARCHAR(100)                             │
│ region          VARCHAR(100)                             │
│ latitude        DECIMAL(9,6)                             │
│ longitude       DECIMAL(9,6)                             │
│ device_type     VARCHAR(20)  ← DESKTOP/MOBILE/TABLET/BOT│
│ os_name         VARCHAR(50)                              │
│ os_version      VARCHAR(20)                              │
│ browser_name    VARCHAR(50)                              │
│ browser_version VARCHAR(20)                              │
│ referrer        TEXT                                      │
│ referrer_domain VARCHAR(255)                             │
│ user_agent      TEXT                                      │
│ language        VARCHAR(10)                               │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                     daily_stats                           │
├──────────────────────────────────────────────────────────┤
│ id              BIGINT       PK (IDENTITY)               │
│ short_url_id    UUID         FK → short_urls             │
│ stat_date       DATE         NOT NULL                    │
│ click_count     INTEGER      DEFAULT 0                   │
│ unique_visitors INTEGER      DEFAULT 0                   │
│ top_country     VARCHAR(2)                               │
│ top_device      VARCHAR(20)                              │
│ top_referrer    VARCHAR(255)                             │
│ UNIQUE (short_url_id, stat_date)                         │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                  rate_limit_config                         │
├──────────────────────────────────────────────────────────┤
│ tier                     VARCHAR(20)  PK                  │
│ requests_per_minute      INTEGER                         │
│ urls_per_day             INTEGER                         │
│ max_custom_slugs         INTEGER                         │
│ analytics_retention_days INTEGER                         │
├──────────────────────────────────────────────────────────┤
│ Seeded data:                                              │
│   FREE       →  60 rpm,   50 urls/day,   10 slugs, 30d  │
│   PRO        → 600 rpm,  500 urls/day,  100 slugs, 365d │
│   ENTERPRISE →6000 rpm, 5000 urls/day, 1000 slugs, 730d │
└──────────────────────────────────────────────────────────┘
```

### Key Database Design Decisions

| Decision | Rationale |
|----------|-----------|
| **UUID primary keys** | Globally unique, no sequence bottleneck, safe for distributed systems |
| **`GENERATED ALWAYS AS IDENTITY`** for click_events | High-volume table — BIGINT auto-increment is faster than UUID |
| **Partial indexes** | `WHERE api_key IS NOT NULL` — index only non-null sparse columns |
| **Check constraints** | `tier IN ('FREE', 'PRO', 'ENTERPRISE')` — DB-level enum validation |
| **Separate `daily_stats`** | Pre-aggregated data avoids scanning millions of click_events |
| **Nullable `user_id` on short_urls** | Supports anonymous URL creation without auth |
| **`ON DELETE CASCADE`** | Click events deleted when short URL is deleted |
| **Indexes on `clicked_at`** | Time-range queries are the most common analytics pattern |
| **VARCHAR(2) for country** | ISO 3166-1 alpha-2 codes are always 2 characters |

---

## 🚀 Getting Started

### Prerequisites

| Tool | Minimum Version | Check Command | Install |
|------|----------------|---------------|---------|
| **Java JDK** | 21+ | `java -version` | [SDKMAN](https://sdkman.io/): `sdk install java 21-tem` |
| **Maven** | 3.9+ | `mvn -v` | [SDKMAN](https://sdkman.io/): `sdk install maven` |
| **Docker** | 24+ | `docker --version` | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| **Docker Compose** | 2.x (plugin) | `docker compose version` | Included with Docker Desktop |
| **Git** | 2.x | `git --version` | [git-scm.com](https://git-scm.com/) |

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/Nazir2608/url-shortener-service.git
cd url-shortener-service

# 2. Generate Maven wrapper (one-time setup)
mvn wrapper:wrapper -Dmaven=3.9.9

# 3. Verify the wrapper works
./mvnw --version
```

### Running the Application

```bash
# Step 1: Start infrastructure (PostgreSQL + Redis)
make docker-up
# OR manually:
docker compose -f docker/docker-compose.yml up -d

# Step 2: Verify containers are healthy (~5 seconds)
docker compose -f docker/docker-compose.yml ps

# Step 3: Start the Spring Boot application
make run
# OR manually:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Expected startup log output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.1)

... Flyway: Successfully applied 7 migrations ...
... Tomcat started on port 8080 (http) ...
... Started UrlShortenerApplication in X.XXX seconds ...
```

### Verifying the Setup

```bash
# 1. Health check endpoint
curl -s http://localhost:8080/api/v1/health | python3 -m json.tool
```

**Expected response:**
```json
{
    "status": "UP",
    "service": "url-shortener-service",
    "timestamp": "2025-01-15T10:30:00.123456",
    "java": "21.0.5"
}
```

```bash
# 2. Swagger UI — open in browser
open http://localhost:8080/swagger-ui.html
# On Linux: xdg-open http://localhost:8080/swagger-ui.html

# 3. Spring Actuator health
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```

**Expected response:**
```json
{
    "status": "UP"
}
```

```bash
# 4. Verify database tables were created
docker exec -it urlshortener-postgres psql -U postgres -d urlshortener -c "\dt"
```

**Expected output:**
```
              List of relations
 Schema |        Name         | Type  |  Owner
--------+---------------------+-------+----------
 public | click_events        | table | postgres
 public | daily_stats         | table | postgres
 public | flyway_schema_history | table | postgres
 public | rate_limit_config   | table | postgres
 public | short_urls          | table | postgres
 public | url_groups          | table | postgres
 public | users               | table | postgres
(7 rows)
```

```bash
# 5. Verify seed data
docker exec -it urlshortener-postgres psql -U postgres -d urlshortener \
  -c "SELECT * FROM rate_limit_config;"
```

**Expected output:**
```
    tier     | requests_per_minute | urls_per_day | max_custom_slugs | analytics_retention_days
-------------+---------------------+--------------+------------------+--------------------------
 FREE        |                  60 |           50 |               10 |                       30
 PRO         |                 600 |          500 |              100 |                      365
 ENTERPRISE  |                6000 |         5000 |             1000 |                      730
```

### Running Tests

```bash
# Run all tests (unit + integration)
# ⚠️  Requires Docker running — Testcontainers starts real PostgreSQL + Redis
make test
# OR:
./mvnw clean test

# Run full verification (includes integration test phase)
make verify
# OR:
./mvnw clean verify

# Run a single test class
./mvnw test -Dtest=ArchitectureTest

# Run only integration tests
./mvnw failsafe:integration-test
```

**Expected test output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0  ← UrlShortenerApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0  ← HealthControllerIT
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0  ← DatabaseConnectionIT
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  ← ArchitectureTest
[INFO]
[INFO] BUILD SUCCESS
```

---

## 🔌 API Endpoints

### Phase 1 — Available Now

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/v1/health` | Service health check | None |
| `GET` | `/swagger-ui.html` | Swagger UI documentation | None |
| `GET` | `/v3/api-docs` | OpenAPI 3 JSON spec | None |
| `GET` | `/actuator/health` | Spring Actuator health | None |
| `GET` | `/actuator/info` | Application info | None |
| `GET` | `/actuator/metrics` | Metrics listing | None |

#### Example: Health Check

```bash
curl -s http://localhost:8080/api/v1/health | jq .
```

```json
{
  "status": "UP",
  "service": "url-shortener-service",
  "timestamp": "2025-01-15T10:30:00.123456",
  "java": "21.0.5"
}
```

### Phase 2+ — Planned

#### URL Shortening (Phase 2)
```
POST   /api/v1/urls                              Create short URL
GET    /api/v1/urls                              List user's URLs (paginated)
GET    /api/v1/urls/{slug}                       Get URL details
PATCH  /api/v1/urls/{slug}                       Update URL
DELETE /api/v1/urls/{slug}                       Delete URL
GET    /{slug}                                    302 Redirect
GET    /{slug}+                                   Link preview
POST   /api/v1/urls/bulk                         Bulk create (PRO+)
```

#### Analytics (Phase 4)
```
GET    /api/v1/urls/{slug}/analytics/summary     Click summary
GET    /api/v1/urls/{slug}/analytics/timeseries  Clicks over time
GET    /api/v1/urls/{slug}/analytics/geo          Geographic breakdown
GET    /api/v1/urls/{slug}/analytics/devices     Device/browser/OS stats
GET    /api/v1/urls/{slug}/analytics/referrers   Referrer analysis
GET    /api/v1/urls/{slug}/analytics/clicks      Raw click log (PRO+)
```

#### Authentication (Phase 5)
```
POST   /api/v1/auth/register                     Create account
POST   /api/v1/auth/login                        Login (returns JWT)
POST   /api/v1/auth/refresh                      Refresh token
GET    /api/v1/users/me                           User profile
POST   /api/v1/users/me/api-keys                 Generate API key
```

### Error Response Format

All errors follow a consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found for slug: abc123",
  "path": "/api/v1/urls/abc123",
  "timestamp": "2025-01-15T10:30:00.123456"
}
```

Validation errors include field details:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request body contains invalid fields",
  "path": "/api/v1/urls",
  "timestamp": "2025-01-15T10:30:00.123456",
  "validationErrors": [
    {
      "field": "url",
      "message": "must not be blank",
      "rejectedValue": ""
    }
  ]
}
```

Rate limit errors include `Retry-After` header:

```json
// HTTP 429 Too Many Requests
// Header: Retry-After: 30
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Retry after 30 seconds.",
  "path": "/api/v1/urls",
  "timestamp": "2025-01-15T10:30:00.123456"
}
```

---

## ⚙️ Configuration

### Application Profiles

| Profile | File | Purpose | Activated By |
|---------|------|---------|--------------|
| **common** | `application.yml` | Shared settings (server, JPA, Flyway, app.*) | Always loaded |
| **dev** | `application-dev.yml` | Local development (localhost DB/Redis, debug logging) | Default active |
| **test** | `application-test.yml` | Test execution (Testcontainers inject properties) | `@ActiveProfiles("test")` |
| **prod** | `application-prod.yml` | Production (env var references, minimal logging) | `SPRING_PROFILES_ACTIVE=prod` |

### Custom Properties

```yaml
# application.yml — custom 'app.*' namespace

app:
  base-url: http://localhost:8080         # Used to construct short URLs
  slug:
    default-length: 7                      # Auto-generated slug length
    min-custom-length: 3                   # Minimum custom slug length
    max-custom-length: 20                  # Maximum custom slug length
  cors:
    allowed-origins:                       # Frontend origins
      - http://localhost:3000
      - http://localhost:5173
```

### Environment Variables (Production)

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `DATABASE_URL` | ✅ | PostgreSQL JDBC URL | `jdbc:postgresql://db-host:5432/urlshortener` |
| `DATABASE_USERNAME` | ✅ | Database username | `app_user` |
| `DATABASE_PASSWORD` | ✅ | Database password | `s3cr3t_p@ssw0rd` |
| `REDIS_HOST` | ✅ | Redis hostname | `redis.example.com` |
| `REDIS_PORT` | ❌ | Redis port (default: 6379) | `6379` |
| `REDIS_PASSWORD` | ❌ | Redis password | `redis_secret` |
| `APP_BASE_URL` | ✅ | Public-facing base URL | `https://sho.rt` |
| `PORT` | ❌ | Server port (default: 8080) | `8080` |
| `SPRING_PROFILES_ACTIVE` | ✅ | Active profile | `prod` |

---

## 🧪 Testing Strategy

### Test Pyramid

```
         ┌──────────────────────┐
         │   Architecture (6)    │  ← ArchUnit: enforce layer rules
         │    ArchUnit Tests     │     as executable tests
         ├──────────────────────┤
         │  Integration (3)      │  ← Testcontainers: REAL PostgreSQL
         │  Testcontainers +     │     + Redis. No mocks, no H2.
         │  TestRestTemplate     │
         ├──────────────────────┤
         │    Unit (future)      │  ← Mockito + JUnit 5
         │  Service layer tests  │     (Phase 2+)
         └──────────────────────┘
```

### Test Classes (Phase 1)

| Test Class | Type | What It Verifies |
|------------|------|------------------|
| `UrlShortenerApplicationTests` | Smoke | Spring ApplicationContext loads without errors |
| `HealthControllerIT` | Integration | `GET /api/v1/health` returns `200 OK` with correct body |
| `DatabaseConnectionIT` | Integration | PostgreSQL connects, Flyway runs all 7 migrations, tables exist |
| `ArchitectureTest` | Architecture | **6 rules enforced** (see below) |

### ArchUnit Rules Enforced

| # | Rule | Description |
|---|------|-------------|
| 1 | **Layered architecture** | Controller → Service → Repository → Domain (no reverse access) |
| 2 | **Controllers cannot access repositories** | All DB access must go through service layer |
| 3 | **Domain has no Spring dependencies** | Entities don't depend on services, controllers, or config |
| 4 | **Repositories must be interfaces** | JPA repositories cannot be concrete classes |
| 5 | **Controller naming** | All classes in `..controller..` must end with `Controller` |
| 6 | **Exception hierarchy** | All classes in `..exception..` ending with `Exception` must extend `RuntimeException` |

### Testcontainers Setup

The `AbstractIntegrationTest` base class automatically starts:
- **PostgreSQL 16 (Alpine)** — real database, Flyway runs migrations
- **Redis 7 (Alpine)** — real Redis for cache tests

Containers are shared across all test methods within a class. To share them across multiple test classes, use the singleton pattern with a static field and `@BeforeAll(static)`.

```java
// To write a new integration test, just extend:
class MyNewIT extends AbstractIntegrationTest {
    @Test
    void myTest() {
        // PostgreSQL and Redis are already running
        // Connection properties are auto-injected via @DynamicPropertySource
    }
}
```

---

## ✅ Phase 1 Detailed Checklist

| # | Category | Task | Status | Files |
|---|----------|------|--------|-------|
| 1 | **Project** | Initialize Spring Boot 3.4 + Java 21 | ✅ | `pom.xml` |
| 2 | **Project** | Configure Maven with all 25+ dependencies | ✅ | `pom.xml` |
| 3 | **Project** | MapStruct + Lombok annotation processor setup | ✅ | `pom.xml` |
| 4 | **Infra** | Docker Compose (PostgreSQL 16 + Redis 7) | ✅ | `docker/docker-compose.yml` |
| 5 | **Config** | Common application config | ✅ | `application.yml` |
| 6 | **Config** | Dev profile (localhost, debug logging) | ✅ | `application-dev.yml` |
| 7 | **Config** | Test profile (Testcontainers) | ✅ | `application-test.yml` |
| 8 | **Config** | Prod profile (env vars) | ✅ | `application-prod.yml` |
| 9 | **Config** | JPA auditing enabled | ✅ | `JpaConfig.java` |
| 10 | **Config** | Spring Security (permit-all) | ✅ | `SecurityConfig.java` |
| 11 | **Config** | Redis template with JSON serialization | ✅ | `RedisConfig.java` |
| 12 | **Config** | OpenAPI / Swagger UI metadata | ✅ | `OpenApiConfig.java` |
| 13 | **Config** | CORS configuration | ✅ | `WebConfig.java` |
| 14 | **Config** | Virtual thread executor + async/scheduling | ✅ | `AsyncConfig.java` |
| 15 | **Domain** | `UserTier` enum (FREE, PRO, ENTERPRISE) | ✅ | `UserTier.java` |
| 16 | **Domain** | `DeviceType` enum (DESKTOP, MOBILE, etc.) | ✅ | `DeviceType.java` |
| 17 | **Domain** | `User` entity with auditing | ✅ | `User.java` |
| 18 | **Domain** | `ShortUrl` entity with business methods | ✅ | `ShortUrl.java` |
| 19 | **Domain** | `UrlGroup` entity | ✅ | `UrlGroup.java` |
| 20 | **Domain** | `ClickEvent` entity (geo + device fields) | ✅ | `ClickEvent.java` |
| 21 | **Domain** | `DailyStat` entity | ✅ | `DailyStat.java` |
| 22 | **Repo** | `UserRepository` (findByEmail, findByApiKey) | ✅ | `UserRepository.java` |
| 23 | **Repo** | `ShortUrlRepository` (search, incrementCount) | ✅ | `ShortUrlRepository.java` |
| 24 | **Repo** | `UrlGroupRepository` | ✅ | `UrlGroupRepository.java` |
| 25 | **Repo** | `ClickEventRepository` (6 analytics queries) | ✅ | `ClickEventRepository.java` |
| 26 | **Repo** | `DailyStatRepository` | ✅ | `DailyStatRepository.java` |
| 27 | **DTO** | `ErrorResponse` record with `ValidationError` | ✅ | `ErrorResponse.java` |
| 28 | **Exception** | `GlobalExceptionHandler` (8 handlers) | ✅ | `GlobalExceptionHandler.java` |
| 29 | **Exception** | 6 custom exception classes | ✅ | `exception/*.java` |
| 30 | **API** | Health check endpoint | ✅ | `HealthController.java` |
| 31 | **Migration** | V1 — users table | ✅ | `V1__create_users_table.sql` |
| 32 | **Migration** | V2 — url_groups table | ✅ | `V2__create_url_groups_table.sql` |
| 33 | **Migration** | V3 — short_urls table | ✅ | `V3__create_short_urls_table.sql` |
| 34 | **Migration** | V4 — click_events table | ✅ | `V4__create_click_events_table.sql` |
| 35 | **Migration** | V5 — daily_stats table | ✅ | `V5__create_daily_stats_table.sql` |
| 36 | **Migration** | V6 — rate_limit_config table | ✅ | `V6__create_rate_limit_config_table.sql` |
| 37 | **Migration** | V7 — seed rate limit tiers | ✅ | `V7__seed_rate_limit_tiers.sql` |
| 38 | **Test** | Context load smoke test | ✅ | `UrlShortenerApplicationTests.java` |
| 39 | **Test** | Testcontainers base class | ✅ | `AbstractIntegrationTest.java` |
| 40 | **Test** | Health endpoint integration test | ✅ | `HealthControllerIT.java` |
| 41 | **Test** | Database connection + Flyway test | ✅ | `DatabaseConnectionIT.java` |
| 42 | **Test** | ArchUnit rules (6 rules) | ✅ | `ArchitectureTest.java` |
| 43 | **DevOps** | `.gitignore` | ✅ | `.gitignore` |
| 44 | **DevOps** | `.editorconfig` | ✅ | `.editorconfig` |
| 45 | **DevOps** | `Makefile` with 12 commands | ✅ | `Makefile` |
| 46 | **Docs** | Comprehensive README | ✅ | `README.md` |

**Total: 46 items completed ✅**

---

## ☕ Java 21 Features Used

| Feature | Where Used | Example |
|---------|-----------|---------|
| **Records** | `ErrorResponse`, `ValidationError` | Immutable DTOs with auto-generated `equals`, `hashCode`, `toString` |
| **Virtual Threads** | `application.yml` + `AsyncConfig.java` | `spring.threads.virtual.enabled: true` — lightweight threads for async click processing |
| **Text Blocks** | `OpenApiConfig.java` | Multi-line API description with `"""..."""` |
| **Enhanced `instanceof`** | Planned for service layer | `if (ex instanceof ShortUrlNotFoundException snfe)` |
| **Switch Expressions** | Planned for mappers/services | `return switch(tier) { case FREE -> 60; ... };` |
| **Sealed Interfaces** | Planned for error types | `sealed interface AppError permits NotFound, Expired, ...` |
| **`toList()` on Stream** | `GlobalExceptionHandler` | `.stream().map(...).toList()` (unmodifiable list) |

---

## 🗺️ Development Roadmap

| Phase | Name | Key Deliverables | Estimated Duration |
|-------|------|------------------|--------------------|
| **1** ✅ | **Foundation** | Project skeleton, entities, migrations, testing infra | Week 1 |
| **2** ⬜ | **Core Shortening** | Base62 encoder, slug generator, URL CRUD, redirect, validation | Week 2 |
| **3** ⬜ | **Caching** | Redis cache-aside for redirects, cache invalidation, benchmarks | Week 3 |
| **4** ⬜ | **Analytics** | MaxMind GeoIP, Yauaa UA parser, async pipeline, aggregation queries, time series | Weeks 4–5 |
| **5** ⬜ | **Authentication** | JWT (access+refresh), API key, Spring Security filter chain, ownership | Week 6 |
| **6** ⬜ | **Rate Limiting** | Bucket4j + Redis, per-tier limits, rate limit headers, 429 responses | Week 7 |
| **7** ⬜ | **Advanced** | Link expiration, password protection, QR codes, bulk create, CSV export | Week 8 |
| **8** ⬜ | **Production** | Dockerfile, Prometheus/Grafana, GitHub Actions CI/CD, load testing | Week 9 |

### Phase 2 Preview (Next)

The next phase will implement the core URL shortening functionality:

1. **Base62 encoder** utility + unit tests
2. **SlugGeneratorService** (Base62 + NanoID)
3. **Slug validation** (reserved words, format rules)
4. **UrlValidationService** (format check, URL safety)
5. **ShortUrlService** (create, read, update, delete)
6. **ShortUrlController** with full CRUD endpoints
7. **RedirectController** (`GET /{slug}` → 302 redirect)
8. **Request/Response DTOs** as Java records
9. **MapStruct mappers** for entity ↔ DTO conversion
10. **Comprehensive unit + integration tests**

---

## 📚 What I Learned

### Phase 1 Learnings

#### Spring Boot 3.x
- **Auto-configuration magic** — how Spring Boot starters wire hundreds of beans automatically by scanning classpath dependencies
- **Profile-based config** — `application-{profile}.yml` files override `application.yml` properties. Profile activation order matters.
- **`spring.jpa.open-in-view: false`** — disabled to prevent lazy loading issues and accidental N+1 queries outside transactions
- **`spring.threads.virtual.enabled: true`** — a single property enables Java 21 virtual threads for all request handling
- **Security in Spring Boot 3** — even permit-all requires explicit `SecurityFilterChain` bean since auto-config changed in SB 3.x
- **`spring.jpa.hibernate.ddl-auto: validate`** — Hibernate only validates schema against entities. Flyway handles actual DDL.

#### JPA / Hibernate 6
- **`@EntityListeners(AuditingEntityListener.class)`** — enables `@CreatedDate` and `@LastModifiedDate` auto-population
- **`equals` and `hashCode` for entities** — use only the `id` field and return `getClass().hashCode()` for hash (Vlad Mihalcea pattern)
- **`@Builder.Default`** — Lombok builder doesn't use field initializers by default — this annotation fixes it
- **Lazy loading with `FetchType.LAZY`** — all `@ManyToOne` and `@OneToMany` should be lazy. Eager is almost never correct.
- **Business methods on entities** — `isExpired()`, `isAccessible()`, `incrementClickCount()` — keep logic close to data

#### Flyway Migrations
- **Naming convention** — `V{number}__{description}.sql` — double underscore is required
- **Idempotent seeds** — `ON CONFLICT DO NOTHING` prevents failures on re-run
- **`baseline-on-migrate: true`** — allows Flyway to work with existing databases
- **Partial indexes** — `CREATE INDEX ... WHERE condition` — PostgreSQL-specific, very efficient for sparse data
- **`GENERATED ALWAYS AS IDENTITY`** — PostgreSQL 10+ best practice, replaces `SERIAL`

#### Testing
- **Testcontainers** — real databases in tests, no H2 compromises. Tests catch real SQL compatibility issues.
- **`@DynamicPropertySource`** — inject container connection URLs at runtime before Spring context loads
- **ArchUnit** — architecture rules as executable tests. If someone violates layering, the build fails.
- **`@SpringBootTest(webEnvironment = RANDOM_PORT)`** — starts full server on random port, avoids conflicts

#### Database Design
- **UUID vs auto-increment** — UUIDs for entities (globally unique), auto-increment for high-volume event tables (faster inserts)
- **Denormalized counters** — `click_count` on `short_urls` avoids `COUNT(*)` on millions of rows
- **Pre-aggregated stats** — `daily_stats` table avoids scanning raw click events for dashboard queries
- **Check constraints** — database-level validation is the last line of defense

---

## 🔧 Makefile Commands

```bash
make help              # Show all available commands
make build             # Build project (skip tests)
make test              # Run all tests
make verify            # Run all tests including integration tests
make compile           # Compile without packaging
make run               # Start app with dev profile
make docker-up         # Start PostgreSQL & Redis containers
make docker-down       # Stop containers
make docker-restart    # Restart containers
make docker-clean      # Stop containers and delete all data volumes
make clean             # Clean build artifacts
make swagger           # Open Swagger UI in browser
make wrapper           # Generate Maven wrapper
```

---

## 🔍 Troubleshooting

### Port 5432/6379 already in use

```bash
# Find what's using the port
lsof -i :5432
lsof -i :6379

# Kill the process or stop existing containers
docker stop $(docker ps -q)

# Then retry
make docker-up
```

### Flyway migration checksum mismatch

```bash
# If you modified a migration file after it was applied:
docker exec -it urlshortener-postgres psql -U postgres -d urlshortener \
  -c "DELETE FROM flyway_schema_history WHERE version = '1';"

# Then restart the app
make run
```

### Testcontainers: Docker not found

```bash
# Ensure Docker Desktop is running
docker info

# If using Colima (macOS alternative):
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export DOCKER_HOST="unix://${HOME}/.colima/docker.sock"
```

### Context fails to load in tests

```bash
# Verify Docker is running (Testcontainers requires it)
docker ps

# Run with debug output
./mvnw test -X -Dtest=UrlShortenerApplicationTests
```

### Redis connection refused

```bash
# Check Redis is running
docker exec -it urlshortener-redis redis-cli ping
# Expected: PONG

# If not running:
make docker-restart
```

---

## 🔮 Future Improvements

Beyond the 8 planned phases, these are potential enhancements:

- **Frontend** — React/Next.js dashboard with analytics charts (Chart.js or Recharts)
- **WebSocket** — real-time click notifications
- **Kafka** — replace Spring Events with Kafka for true distributed event processing
- **Read replicas** — separate read/write datasources for analytics queries
- **Table partitioning** — partition `click_events` by month for improved query performance
- **URL safety scanning** — Google Safe Browsing API integration
- **Custom domains** — allow users to use their own domain for short URLs
- **A/B testing** — split traffic between multiple destination URLs
- **Link-in-bio** — profile page with all user's links
- **Webhook notifications** — notify users when links reach click milestones
- **GraphQL API** — alternative to REST for flexible analytics queries
- **gRPC** — internal service communication (if going microservices)
- **GraalVM Native Image** — compile to native for instant startup
- **Kubernetes manifests** — Helm chart for K8s deployment

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vlad Mihalcea's JPA Best Practices](https://vladmihalcea.com/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [MaxMind GeoLite2](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)
- [Yauaa — Yet Another UserAgent Analyzer](https://yauaa.basjes.nl/)

---

<p align="center">
  <b>Built for learning. Built for the portfolio. Built properly.</b> 🚀
  <br><br>
  <i>If you found this project helpful, consider giving it a ⭐</i>
</p>
