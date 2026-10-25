package br.com.forjacode.shortifyapi.adapter.in.web.advice;

import br.com.forjacode.shortifyapi.adapter.in.web.ShortUrlController;
import br.com.forjacode.shortifyapi.adapter.in.web.dto.request.ShortUrlRequest;
import br.com.forjacode.shortifyapi.domain.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.domain.exception.ShortUrlNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MethodArgumentNotValidException validationException(FieldError... fieldErrors) throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new ShortUrlRequest(null), "shortUrlRequest");
        for (FieldError fieldError : fieldErrors) {
            bindingResult.addError(fieldError);
        }
        MethodParameter parameter = new MethodParameter(
                ShortUrlController.class.getDeclaredMethod("shorten", ShortUrlRequest.class), 0);
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    @Nested
    @DisplayName("HandleNotFound Tests")
    class HandleNotFound {

        @Nested
        @DisplayName("Should build 404 response successfully")
        class Success {

            @Test
            void shouldReturnNotFoundWithMessageAndTimestamp() {
                ProblemDetail problem = handler.handleNotFound(new ShortUrlNotFoundException("abc1234"));

                assertThat(problem.getStatus()).isEqualTo(404);
                assertThat(problem.getDetail()).isEqualTo("Short URL not found: abc1234");
                assertThat(problem.getProperties()).containsKey("timestamp");
            }
        }
    }

    @Nested
    @DisplayName("HandleInvalidUrl Tests")
    class HandleInvalidUrl {

        @Nested
        @DisplayName("Should build 400 response successfully")
        class Success {

            @Test
            void shouldReturnBadRequestWithMessageAndTimestamp() {
                ProblemDetail problem = handler.handleInvalidUrl(new InvalidUrlException("ftp://x"));

                assertThat(problem.getStatus()).isEqualTo(400);
                assertThat(problem.getDetail()).isEqualTo("Invalid URL: ftp://x");
                assertThat(problem.getProperties()).containsKey("timestamp");
            }
        }
    }

    @Nested
    @DisplayName("HandleMethodArgumentNotValidException Tests")
    class HandleMethodArgumentNotValid {

        @Nested
        @DisplayName("Should list field errors successfully")
        class Success {

            @Test
            @SuppressWarnings("unchecked")
            void shouldListEachFieldError() throws Exception {
                FieldError fieldError = new FieldError("shortUrlRequest", "url", "A URL é obrigatória");

                ProblemDetail problem = handler.handleMethodArgumentNotValidException(validationException(fieldError));

                assertThat(problem.getStatus()).isEqualTo(400);
                assertThat(problem.getProperties()).containsKey("timestamp");
                assertThat((List<Map<String, String>>) problem.getProperties().get("errors"))
                        .containsExactly(Map.of("field", "url", "message", "A URL é obrigatória"));
            }
        }

        @Nested
        @DisplayName("Should fall back when error has no message")
        class WithError {

            @Test
            @SuppressWarnings("unchecked")
            void shouldUseDefaultMessageWhenFieldErrorHasNone() throws Exception {
                FieldError fieldError = new FieldError("shortUrlRequest", "url", null);

                ProblemDetail problem = handler.handleMethodArgumentNotValidException(validationException(fieldError));

                assertThat((List<Map<String, String>>) problem.getProperties().get("errors"))
                        .containsExactly(Map.of("field", "url", "message", "valor inválido"));
            }
        }
    }
}
