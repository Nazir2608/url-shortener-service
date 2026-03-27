package com.nazir.urlshortener.unit.util;

import com.nazir.urlshortener.util.SlugValidator;
import com.nazir.urlshortener.util.SlugValidator.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlugValidator")
class SlugValidatorTest {

    private static final int MIN = 3;
    private static final int MAX = 20;

    @Nested
    @DisplayName("valid slugs")
    class ValidSlugs {

        @ParameterizedTest
        @ValueSource(strings = {"abc", "my-link", "Link123", "a1b", "hello-world-2025"})
        void shouldAcceptValidSlugs(String slug) {
            ValidationResult result = SlugValidator.validate(slug, MIN, MAX);
            assertThat(result.isValid()).isTrue();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        void shouldAcceptSingleCharWhenMinIs1() {
            ValidationResult result = SlugValidator.validate("a", 1, 20);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("invalid slugs")
    class InvalidSlugs {

        @Test
        void shouldRejectNull() {
            ValidationResult result = SlugValidator.validate(null, MIN, MAX);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        void shouldRejectBlank() {
            ValidationResult result = SlugValidator.validate("  ", MIN, MAX);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        void shouldRejectTooShort() {
            ValidationResult result = SlugValidator.validate("ab", MIN, MAX);
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).contains("at least 3");
        }

        @Test
        void shouldRejectTooLong() {
            String longSlug = "a".repeat(21);
            ValidationResult result = SlugValidator.validate(longSlug, MIN, MAX);
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).contains("not exceed 20");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-abc", "abc-", "-abc-", "--abc"})
        void shouldRejectLeadingOrTrailingHyphens(String slug) {
            ValidationResult result = SlugValidator.validate(slug, MIN, MAX);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        void shouldRejectConsecutiveHyphens() {
            ValidationResult result = SlugValidator.validate("ab--cd", MIN, MAX);
            assertThat(result.isValid()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab cd", "ab@cd", "ab.cd", "ab/cd", "ab?cd"})
        void shouldRejectSpecialCharacters(String slug) {
            ValidationResult result = SlugValidator.validate(slug, MIN, MAX);
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("reserved words")
    class ReservedWords {

        @ParameterizedTest
        @ValueSource(strings = {"api", "admin", "login", "swagger", "actuator", "health"})
        void shouldRejectReservedWords(String slug) {
            ValidationResult result = SlugValidator.validate(slug, MIN, MAX);
            assertThat(result.isValid()).isFalse();
            assertThat(result.errorMessage()).contains("reserved");
        }

        @ParameterizedTest
        @ValueSource(strings = {"API", "Admin", "LOGIN", "Swagger"})
        void shouldRejectReservedWordsCaseInsensitive(String slug) {
            assertThat(SlugValidator.isReserved(slug)).isTrue();
        }

        @Test
        void shouldAllowNonReservedWords() {
            assertThat(SlugValidator.isReserved("my-custom-slug")).isFalse();
        }
    }
}
