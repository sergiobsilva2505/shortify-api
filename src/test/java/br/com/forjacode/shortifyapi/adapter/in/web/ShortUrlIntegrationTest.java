package br.com.forjacode.shortifyapi.adapter.in.web;

import br.com.forjacode.shortifyapi.support.IntegrationTestSupport;
import br.com.forjacode.shortifyapi.adapter.out.persistence.ShortUrlJpaEntity;
import br.com.forjacode.shortifyapi.adapter.out.persistence.ShortUrlJpaRepository;
import br.com.forjacode.shortifyapi.application.port.out.ShortCodeGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShortUrlIntegrationTest extends IntegrationTestSupport {

    private static final String API = "/api/shorten";
    private static final String CODE = "abc1234";
    private static final String UNKNOWN_CODE = "zzz9999";
    private static final String URL = "https://example.com";
    private static final String NEW_URL = "https://example.org/new";
    private static final String COMPLEX_URL = "https://example.com/caf%C3%A9/path?q=a%20b&page=2#section-1";
    private static final String COLLIDING_CODE = "dup0001";
    private static final int MAX_URL_LENGTH = 2048;
    private static final String PAST_TIMESTAMP = "2020-01-01 00:00:00";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlJpaRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ShortCodeGeneratorPort codeGenerator;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private ShortUrlJpaEntity persist() {
        return repository.save(newEntity(URL, CODE));
    }

    private static ShortUrlJpaEntity newEntity(String url, String code) {
        Instant now = Instant.now();
        return new ShortUrlJpaEntity(null, url, code, 0L, now, now);
    }

    private static String body(String url) {
        return "{\"url\":\"" + url + "\"}";
    }

    private List<Integer> runConcurrently(int threads, Callable<Integer> request) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        return request.call();
                    } catch (Exception e) {
                        return 500;
                    }
                }));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statuses.add(future.get(30, TimeUnit.SECONDS));
            }
            return statuses;
        } finally {
            pool.shutdownNow();
        }
    }

    @Nested
    @DisplayName("Create ShortUrl Tests (POST /api/shorten)")
    class Create {

        @Nested
        @DisplayName("Should create ShortUrl successfully")
        class Success {

            @Test
            void shouldReturnCreatedWithGeneratedCode() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(URL)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.url").value(URL))
                        .andExpect(jsonPath("$.shortCode").value(matchesPattern("[0-9A-Za-z]{7}")))
                        .andExpect(jsonPath("$.shortUrl").value(matchesPattern(".+/[0-9A-Za-z]{7}")))
                        .andExpect(jsonPath("$.createdAt").exists())
                        .andExpect(jsonPath("$.updatedAt").exists());

                assertThat(repository.count()).isEqualTo(1);
            }

            @Test
            void shouldGenerateDifferentCodesForSameUrl() throws Exception {
                for (int i = 0; i < 2; i++) {
                    mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(URL)))
                            .andExpect(status().isCreated());
                }

                assertThat(repository.findAll()).extracting(ShortUrlJpaEntity::getShortCode).doesNotHaveDuplicates();
            }

            @Test
            void shouldAcceptUrlWithMaxLength() throws Exception {
                String maxLengthUrl = "https://example.com/" + "a".repeat(MAX_URL_LENGTH - "https://example.com/".length());
                assertThat(maxLengthUrl).hasSize(MAX_URL_LENGTH);

                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(maxLengthUrl)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.url").value(maxLengthUrl));
            }

            @Test
            void shouldGenerateDistinctCodesWhenGeneratorCollidesConcurrently() throws Exception {
                int threads = 5;
                CyclicBarrier barrier = new CyclicBarrier(threads);
                AtomicInteger calls = new AtomicInteger();
                doAnswer(invocation -> {
                    if (calls.getAndIncrement() < threads) {
                        barrier.await(10, TimeUnit.SECONDS);
                        return COLLIDING_CODE;
                    }
                    return invocation.callRealMethod();
                }).when(codeGenerator).generate();

                List<Integer> statuses = runConcurrently(threads, () ->
                        mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(URL)))
                                .andReturn().getResponse().getStatus());

                assertThat(statuses).containsOnly(201);
                assertThat(repository.findAll()).extracting(ShortUrlJpaEntity::getShortCode)
                        .hasSize(threads)
                        .doesNotHaveDuplicates();
            }
        }

        @Nested
        @DisplayName("Should return error for invalid request")
        class WithError {

            @ParameterizedTest
            @ValueSource(strings = {"", "   ", "not-a-url", "example.com"})
            void shouldReturnBadRequestForInvalidUrl(String invalidUrl) throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(invalidUrl)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("url"));

                assertThat(repository.count()).isZero();
            }

            @Test
            void shouldReturnBadRequestWhenUrlIsMissing() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors", hasSize(1)));
            }

            @Test
            void shouldReturnBadRequestWhenUrlExceedsMaxLength() throws Exception {
                String tooLong = "https://example.com/" + "a".repeat(2048);

                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body(tooLong)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("url"));

                assertThat(repository.count()).isZero();
            }

            @Test
            void shouldReturnBadRequestForUnsupportedScheme() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body("ftp://example.com")))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400));

                assertThat(repository.count()).isZero();
            }

            @Test
            void shouldReturnBadRequestForMalformedJson() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content("{url"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            void shouldReturnUnsupportedMediaTypeForNonJsonContent() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.TEXT_PLAIN).content(URL))
                        .andExpect(status().isUnsupportedMediaType());

                assertThat(repository.count()).isZero();
            }

            @Test
            void shouldReturnUnsupportedMediaTypeWhenContentTypeIsMissing() throws Exception {
                mockMvc.perform(post(API).content(body(URL)))
                        .andExpect(status().isUnsupportedMediaType());
            }
        }
    }

    @Nested
    @DisplayName("Find ShortUrl Tests (GET /api/shorten/{code})")
    class FindByCode {

        @Nested
        @DisplayName("Should find ShortUrl successfully")
        class Success {

            @Test
            void shouldReturnShortUrlData() throws Exception {
                ShortUrlJpaEntity saved = persist();

                mockMvc.perform(get(API + "/" + CODE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(saved.getId()))
                        .andExpect(jsonPath("$.url").value(URL))
                        .andExpect(jsonPath("$.shortCode").value(CODE))
                        .andExpect(jsonPath("$.shortUrl").value(endsWith("/" + CODE)));
            }
        }

        @Nested
        @DisplayName("Should return error when code is unknown or malformed")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                mockMvc.perform(get(API + "/" + UNKNOWN_CODE))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(404));
            }

            @Test
            void shouldReturnNotFoundForMalformedCode() throws Exception {
                mockMvc.perform(get(API + "/abc"))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("Redirect Tests (GET /{code})")
    class Redirect {

        @Nested
        @DisplayName("Should redirect successfully")
        class Success {

            @Test
            void shouldRedirectToOriginalUrl() throws Exception {
                persist();

                mockMvc.perform(get("/" + CODE))
                        .andExpect(status().isFound())
                        .andExpect(header().string(HttpHeaders.LOCATION, URL));
            }

            @Test
            void shouldIncrementAccessCountOnEachRedirect() throws Exception {
                persist();

                for (int i = 0; i < 3; i++) {
                    mockMvc.perform(get("/" + CODE)).andExpect(status().isFound());
                }

                assertThat(repository.findAccessCountByShortCode(CODE)).contains(3L);
            }

            @Test
            void shouldCountEveryAccessWhenRedirectsAreConcurrent() throws Exception {
                persist();
                int threads = 50;

                List<Integer> statuses = runConcurrently(threads, () ->
                        mockMvc.perform(get("/" + CODE)).andReturn().getResponse().getStatus());

                assertThat(statuses).containsOnly(302);
                assertThat(repository.findAccessCountByShortCode(CODE)).contains((long) threads);
            }

            @Test
            void shouldPreserveQueryStringAndFragmentInLocation() throws Exception {
                repository.save(newEntity(COMPLEX_URL, CODE));

                mockMvc.perform(get("/" + CODE))
                        .andExpect(status().isFound())
                        .andExpect(header().string(HttpHeaders.LOCATION, COMPLEX_URL));
            }
        }

        @Nested
        @DisplayName("Should return error when code is unknown or malformed")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                mockMvc.perform(get("/" + UNKNOWN_CODE))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldReturnNotFoundForMalformedCode() throws Exception {
                mockMvc.perform(get("/abc"))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("Update ShortUrl Tests (PUT /api/shorten/{code})")
    class Update {

        @Nested
        @DisplayName("Should update ShortUrl successfully")
        class Success {

            @Test
            void shouldUpdateOriginalUrlAndKeepCode() throws Exception {
                persist();

                mockMvc.perform(put(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.url").value(NEW_URL))
                        .andExpect(jsonPath("$.shortCode").value(CODE));

                assertThat(repository.findByShortCode(CODE).orElseThrow().getOriginalUrl()).isEqualTo(NEW_URL);
            }

            @Test
            void shouldPersistUpdatedAtAndKeepCreatedAt() throws Exception {
                persist();
                jdbcTemplate.update("update short_url set created_at = ?, updated_at = ? where short_code = ?",
                        PAST_TIMESTAMP, PAST_TIMESTAMP, CODE);

                mockMvc.perform(put(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isOk());

                String createdAt = jdbcTemplate.queryForObject(
                        "select cast(created_at as char) from short_url where short_code = ?", String.class, CODE);
                String updatedAt = jdbcTemplate.queryForObject(
                        "select cast(updated_at as char) from short_url where short_code = ?", String.class, CODE);
                assertThat(createdAt).startsWith(PAST_TIMESTAMP);
                assertThat(updatedAt).doesNotStartWith(PAST_TIMESTAMP);
            }

            @Test
            void shouldRedirectToNewUrlAfterUpdate() throws Exception {
                persist();
                mockMvc.perform(put(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/" + CODE))
                        .andExpect(status().isFound())
                        .andExpect(header().string(HttpHeaders.LOCATION, NEW_URL));
            }
        }

        @Nested
        @DisplayName("Should return error for invalid request")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                mockMvc.perform(put(API + "/" + UNKNOWN_CODE).contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldReturnBadRequestForInvalidUrlAndKeepOriginal() throws Exception {
                persist();

                mockMvc.perform(put(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body("not-a-url")))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.errors[0].field").value("url"));

                assertThat(repository.findByShortCode(CODE).orElseThrow().getOriginalUrl()).isEqualTo(URL);
            }

            @Test
            void shouldReturnBadRequestForUnsupportedSchemeAndKeepOriginal() throws Exception {
                persist();

                mockMvc.perform(put(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body("ftp://example.com")))
                        .andExpect(status().isBadRequest());

                assertThat(repository.findByShortCode(CODE).orElseThrow().getOriginalUrl()).isEqualTo(URL);
            }

            @Test
            void shouldReturnNotFoundForMalformedCode() throws Exception {
                mockMvc.perform(put(API + "/abc").contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("Delete ShortUrl Tests (DELETE /api/shorten/{code})")
    class Delete {

        @Nested
        @DisplayName("Should delete ShortUrl successfully")
        class Success {

            @Test
            void shouldDeleteAndStopResolvingCode() throws Exception {
                persist();

                mockMvc.perform(delete(API + "/" + CODE))
                        .andExpect(status().isNoContent());

                assertThat(repository.existsByShortCode(CODE)).isFalse();
                mockMvc.perform(get("/" + CODE)).andExpect(status().isNotFound());
            }
        }

        @Nested
        @DisplayName("Should return error when code is unknown or malformed")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                mockMvc.perform(delete(API + "/" + UNKNOWN_CODE))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldReturnNotFoundOnSecondDelete() throws Exception {
                persist();
                mockMvc.perform(delete(API + "/" + CODE)).andExpect(status().isNoContent());

                mockMvc.perform(delete(API + "/" + CODE)).andExpect(status().isNotFound());
            }

            @Test
            void shouldReturnNotFoundForMalformedCode() throws Exception {
                mockMvc.perform(delete(API + "/abc"))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("Stats Tests (GET /api/shorten/{code}/stats)")
    class Stats {

        @Nested
        @DisplayName("Should return stats successfully")
        class Success {

            @Test
            void shouldReturnZeroForNeverAccessedCode() throws Exception {
                persist();

                mockMvc.perform(get(API + "/" + CODE + "/stats"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(CODE))
                        .andExpect(jsonPath("$.accessCount").value(0));
            }

            @Test
            void shouldReflectRedirectsInAccessCount() throws Exception {
                persist();
                for (int i = 0; i < 2; i++) {
                    mockMvc.perform(get("/" + CODE)).andExpect(status().isFound());
                }

                MvcResult result = mockMvc.perform(get(API + "/" + CODE + "/stats"))
                        .andExpect(status().isOk())
                        .andReturn();

                assertThat(result.getResponse().getContentAsString()).contains("\"accessCount\":2");
            }
        }

        @Nested
        @DisplayName("Should return error when code is unknown or malformed")
        class WithError {

            @Test
            void shouldReturnNotFoundForUnknownCode() throws Exception {
                mockMvc.perform(get(API + "/" + UNKNOWN_CODE + "/stats"))
                        .andExpect(status().isNotFound());
            }

            @Test
            void shouldReturnNotFoundForMalformedCode() throws Exception {
                mockMvc.perform(get(API + "/abc/stats"))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("Unsupported Request Tests")
    class UnsupportedRequest {

        @Nested
        @DisplayName("Should return error for unsupported methods")
        class WithError {

            @Test
            void shouldReturnMethodNotAllowedForPostOnRedirectRoute() throws Exception {
                persist();

                mockMvc.perform(post("/" + CODE))
                        .andExpect(status().isMethodNotAllowed());
            }

            @Test
            void shouldReturnMethodNotAllowedForPatchOnCodeRoute() throws Exception {
                persist();

                mockMvc.perform(patch(API + "/" + CODE).contentType(MediaType.APPLICATION_JSON).content(body(NEW_URL)))
                        .andExpect(status().isMethodNotAllowed());
            }

            @Test
            void shouldReturnMethodNotAllowedForPostOnStatsRoute() throws Exception {
                persist();

                mockMvc.perform(post(API + "/" + CODE + "/stats"))
                        .andExpect(status().isMethodNotAllowed());
            }
        }
    }

    @Nested
    @DisplayName("Error Response Contract Tests")
    class ErrorResponseContract {

        @Nested
        @DisplayName("Should return ProblemDetail with expected fields")
        class WithError {

            @Test
            void shouldDescribeNotFoundError() throws Exception {
                mockMvc.perform(get(API + "/" + UNKNOWN_CODE))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.status").value(404))
                        .andExpect(jsonPath("$.title").value("Not Found"))
                        .andExpect(jsonPath("$.detail").value("Short URL not found: " + UNKNOWN_CODE))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @Test
            void shouldDescribeInvalidUrlError() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body("ftp://example.com")))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.title").value("Bad Request"))
                        .andExpect(jsonPath("$.detail").value("Invalid URL: ftp://example.com"))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty());
            }

            @Test
            void shouldDescribeValidationErrors() throws Exception {
                mockMvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.timestamp").isNotEmpty())
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("url"))
                        .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
            }
        }
    }
}
