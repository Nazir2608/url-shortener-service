package com.nazir.urlshortener.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class SlugValidator {

    private static final Pattern SLUG_PATTERN =
        Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9]|(-(?!-)))*[a-zA-Z0-9]$|^[a-zA-Z0-9]$");

    private static final Set<String> RESERVED_WORDS = Set.of(
        "api", "admin", "health", "swagger", "swagger-ui", "actuator",
        "api-docs", "v1", "v2", "v3",
        "login", "logout", "register", "signup", "signin", "auth",
        "oauth", "callback", "token", "refresh",
        "static", "assets", "css", "js", "img", "images", "fonts",
        "favicon", "favicon.ico", "robots", "robots.txt", "sitemap",
        "sitemap.xml",
        "about", "help", "support", "terms", "privacy", "contact",
        "dashboard", "settings", "profile", "account", "billing",
        "null", "undefined", "true", "false", "error", "status",
        "ping", "info", "debug", "test", "preview"
    );

    private SlugValidator() {}

    public static ValidationResult validate(String slug, int minLength, int maxLength) {
        if (slug == null || slug.isBlank()) {
            return ValidationResult.failure("Slug must not be blank");           // ← RENAMED
        }

        if (slug.length() < minLength) {
            return ValidationResult.failure(                                      // ← RENAMED
                "Slug must be at least %d characters, got %d".formatted(minLength, slug.length()));
        }

        if (slug.length() > maxLength) {
            return ValidationResult.failure(                                      // ← RENAMED
                "Slug must not exceed %d characters, got %d".formatted(maxLength, slug.length()));
        }

        if (!SLUG_PATTERN.matcher(slug).matches()) {
            return ValidationResult.failure(                                      // ← RENAMED
                "Slug must contain only letters, numbers, and hyphens. " +
                    "Cannot start/end with hyphen or have consecutive hyphens.");
        }

        if (isReserved(slug)) {
            return ValidationResult.failure(                                      // ← RENAMED
                "Slug '%s' is reserved and cannot be used".formatted(slug));
        }

        return ValidationResult.success();                                        // ← RENAMED
    }

    public static boolean isReserved(String slug) {
        return RESERVED_WORDS.contains(slug.toLowerCase());
    }

    public static boolean isValidFormat(String slug) {
        return slug != null && SLUG_PATTERN.matcher(slug).matches();
    }

    /**
     * ┌─────────────────────────────────────────────────────────┐
     * │  FIX: Renamed static factories to avoid collision       │
     * │  with the record component accessor valid()             │
     * │                                                         │
     * │  BEFORE (broken):                                       │
     * │    record ValidationResult(boolean valid, ...)           │
     * │      static ValidationResult valid()    ← CLASH!        │
     * │      // auto-generated: boolean valid() ← CLASH!        │
     * │                                                         │
     * │  AFTER (fixed):                                         │
     * │    record ValidationResult(boolean isValid, ...)        │
     * │      static ValidationResult success()  ← no clash     │
     * │      static ValidationResult failure()  ← no clash     │
     * │      // auto-generated: boolean isValid() ← clean      │
     * └─────────────────────────────────────────────────────────┘
     */
    public record ValidationResult(boolean isValid, String errorMessage) {

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}
