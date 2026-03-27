package com.nazir.urlshortener.unit.util;

import com.nazir.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Base62Encoder")
class Base62EncoderTest {

    @Nested
    @DisplayName("encode()")
    class Encode {

        @Test
        void shouldEncodeZero() {
            assertThat(Base62Encoder.encode(0)).isEqualTo("a");
        }

        @ParameterizedTest
        @CsvSource({
            "1, b",
            "61, 9",
            "62, ba",
            "12345, dnh"
        })
        void shouldEncodeKnownValues(long input, String expected) {
            assertThat(Base62Encoder.encode(input)).isEqualTo(expected);
        }

        @Test
        void shouldThrowForNegativeNumber() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Base62Encoder.encode(-1))
                .withMessageContaining("non-negative");
        }
    }

    @Nested
    @DisplayName("decode()")
    class Decode {

        @Test
        void shouldDecodeBackToOriginal() {
            long original = 123456789L;
            String encoded = Base62Encoder.encode(original);
            assertThat(Base62Encoder.decode(encoded)).isEqualTo(original);
        }

        @Test
        void shouldThrowForEmptyString() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Base62Encoder.decode(""));
        }

        @Test
        void shouldThrowForInvalidCharacter() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Base62Encoder.decode("abc!"))
                .withMessageContaining("Invalid Base62 character");
        }
    }

    @Nested
    @DisplayName("generateRandom()")
    class GenerateRandom {

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 7, 10, 20})
        void shouldGenerateCorrectLength(int length) {
            String result = Base62Encoder.generateRandom(length);
            assertThat(result).hasSize(length);
        }

        @Test
        void shouldGenerateOnlyBase62Characters() {
            String result = Base62Encoder.generateRandom(100);
            assertThat(result).matches("^[a-zA-Z0-9]+$");
        }

        @Test
        void shouldGenerateUniqueValues() {
            Set<String> generated = new HashSet<>();
            for (int i = 0; i < 10_000; i++) {
                generated.add(Base62Encoder.generateRandom(7));
            }
            // With 62^7 = 3.5 trillion combos, 10K should all be unique
            assertThat(generated).hasSize(10_000);
        }

        @Test
        void shouldThrowForNonPositiveLength() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Base62Encoder.generateRandom(0));
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Base62Encoder.generateRandom(-1));
        }
    }

    @Nested
    @DisplayName("isValidBase62()")
    class IsValidBase62 {

        @Test
        void shouldReturnTrueForValidStrings() {
            assertThat(Base62Encoder.isValidBase62("abc123XYZ")).isTrue();
        }

        @Test
        void shouldReturnFalseForInvalidStrings() {
            assertThat(Base62Encoder.isValidBase62("abc-123")).isFalse();
            assertThat(Base62Encoder.isValidBase62("abc 123")).isFalse();
            assertThat(Base62Encoder.isValidBase62("")).isFalse();
            assertThat(Base62Encoder.isValidBase62(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("encode-decode roundtrip")
    class Roundtrip {

        @ParameterizedTest
        @ValueSource(longs = {0, 1, 62, 1000, 999999, 123456789L, Long.MAX_VALUE / 2})
        void shouldRoundtrip(long value) {
            String encoded = Base62Encoder.encode(value);
            long decoded = Base62Encoder.decode(encoded);
            assertThat(decoded).isEqualTo(value);
        }
    }
}
