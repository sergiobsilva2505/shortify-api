package br.com.forjacode.shortifyapi.adapter.out.codegen;

import br.com.forjacode.shortifyapi.domain.model.ShortCodeFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Base62ShortCodeGeneratorTest {

    private final Base62ShortCodeGenerator generator = new Base62ShortCodeGenerator();

    @Nested
    @DisplayName("Generate Tests")
    class Generate {

        @Nested
        @DisplayName("Should generate code successfully")
        class Success {

            @Test
            void shouldGenerateCodeWithExpectedLength() {
                assertThat(generator.generate()).hasSize(ShortCodeFormat.LENGTH);
            }

            @Test
            void shouldGenerateOnlyBase62Characters() {
                for (int i = 0; i < 1000; i++) {
                    assertThat(generator.generate()).matches(ShortCodeFormat.REGEX);
                }
            }

            @Test
            void shouldGenerateDifferentCodes() {
                Set<String> codes = new HashSet<>();
                for (int i = 0; i < 1000; i++) {
                    codes.add(generator.generate());
                }

                assertThat(codes).hasSizeGreaterThan(1);
            }
        }

        @Nested
        @DisplayName("Should propagate random source failures")
        class WithError {

            @Test
            void shouldPropagateExceptionWhenRandomSourceFails() {
                SecureRandom failingRandom = mock(SecureRandom.class);
                when(failingRandom.nextInt(anyInt())).thenThrow(new IllegalStateException("entropy unavailable"));
                ReflectionTestUtils.setField(generator, "random", failingRandom);

                assertThatThrownBy(generator::generate)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("entropy unavailable");
            }

            @Test
            void shouldNotReturnPartialCodeWhenRandomSourceFailsMidway() {
                SecureRandom failingRandom = mock(SecureRandom.class);
                when(failingRandom.nextInt(anyInt()))
                        .thenReturn(0, 1, 2)
                        .thenThrow(new IllegalStateException("entropy unavailable"));
                ReflectionTestUtils.setField(generator, "random", failingRandom);

                assertThatThrownBy(generator::generate).isInstanceOf(IllegalStateException.class);
            }
        }
    }
}
