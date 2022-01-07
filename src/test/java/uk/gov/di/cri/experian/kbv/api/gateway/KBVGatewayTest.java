package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.RTQRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.di.cri.experian.kbv.api.config.KbvApiConfig;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.util.TestDataCreator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KBVGatewayTest {

    private class KbvGatewayContructorArgs {
        private final HttpClient httpClient;
        private final RTQRequestMapper rtqRequestMapper;
        private final ObjectMapper objectMapper;
        private final KbvApiConfig kbvApiConfig;
        private final SAARequestMapper saaRequestMapper;

        public KbvGatewayContructorArgs(
                SAARequestMapper saaRequestMapper,
                RTQRequestMapper rtqRequestMapper,
                HttpClient httpClient,
                ObjectMapper objectMapper,
                KbvApiConfig kbvApiConfig) {

            this.httpClient = httpClient;
            this.saaRequestMapper = saaRequestMapper;
            this.rtqRequestMapper = rtqRequestMapper;
            this.objectMapper = objectMapper;
            this.kbvApiConfig = kbvApiConfig;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private KBVGateway kbvGateway;
    private SAARequestMapper mockSAARequestMapper = mock(SAARequestMapper.class);
    private RTQRequestMapper mockRtqRequestMapper = mock(RTQRequestMapper.class);
    private HttpClient mockHttpClient = mock(HttpClient.class);
    private ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
    private KbvApiConfig mockKbvApiConfig = mock(KbvApiConfig.class);

    @BeforeEach
    void setUp() {
        this.kbvGateway =
                new KBVGateway(
                        mockSAARequestMapper,
                        mockRtqRequestMapper,
                        mockHttpClient,
                        mockObjectMapper,
                        mockKbvApiConfig);
    }

    @Test
    void shouldCallSubmitAnswersSuccessfully() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedKbvApiRequest";
        final String testEndpointUri = "https://test-endpoint";
        final RTQRequest testApiRequest = new RTQRequest();
        QuestionAnswerRequest questionAnswerRequest =
                TestDataCreator.createTestQuestionAnswerRequest();
        when(mockRtqRequestMapper.mapQuestionAnswersRtqRequest(questionAnswerRequest))
                .thenReturn(testApiRequest);
        when(this.mockKbvApiConfig.getEndpointUri()).thenReturn(testEndpointUri);
        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse<String> createMockApiResponse =
                (HttpResponse<String>) mock(HttpResponse.class);
        when(createMockApiResponse.body()).thenReturn(TEST_API_RESPONSE_BODY);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse);

        String questionAnswerRequestResult = kbvGateway.submitAnswers(questionAnswerRequest);

        assertEquals(TEST_API_RESPONSE_BODY, questionAnswerRequestResult);
        verify(mockRtqRequestMapper).mapQuestionAnswersRtqRequest(questionAnswerRequest);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockKbvApiConfig).getEndpointUri();
        verify(mockHttpClient)
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertEquals(testEndpointUri, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsAreProvided() {
        Map<String, KbvGatewayContructorArgs> testCases =
                Map.of(
                        "httpClient must not be null",
                        new KbvGatewayContructorArgs(null, null, null, null, null),
                        "rtqRequestMapper must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null, null, mock(HttpClient.class), null, null),
                        "objectMapper must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null,
                                mock(RTQRequestMapper.class),
                                mock(HttpClient.class),
                                null,
                                null),
                        "kbvApiConfig must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null,
                                mock(RTQRequestMapper.class),
                                mock(HttpClient.class),
                                mock(ObjectMapper.class),
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) -> {
                    assertThrows(
                            NullPointerException.class,
                            () -> {
                                new KBVGateway(
                                        constructorArgs.saaRequestMapper,
                                        constructorArgs.rtqRequestMapper,
                                        constructorArgs.httpClient,
                                        constructorArgs.objectMapper,
                                        constructorArgs.kbvApiConfig);
                            },
                            errorMessage);
                });
    }
}
