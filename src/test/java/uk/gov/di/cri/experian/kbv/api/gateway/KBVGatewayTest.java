package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.IdentityIQWebServiceSoap;
import com.experian.uk.schema.experian.identityiq.services.webservice.RTQRequest;
import com.experian.uk.schema.experian.identityiq.services.webservice.RTQResponse2;
import com.experian.uk.schema.experian.identityiq.services.webservice.Results;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.security.KbvSoapWebServiceClient;
import uk.gov.di.cri.experian.kbv.api.util.TestDataCreator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KBVGatewayTest {

    private class KbvGatewayContructorArgs {
        private final KbvSoapWebServiceClient kbvSoapWebServiceClient;
        private final ResponseToQuestionMapper responseToQuestionMapper;
        private final SAARequestMapper saaRequestMapper;

        public KbvGatewayContructorArgs(
                SAARequestMapper saaRequestMapper,
                ResponseToQuestionMapper responseToQuestionMapper,
                KbvSoapWebServiceClient kbvSoapWebServiceClient) {

            this.kbvSoapWebServiceClient = kbvSoapWebServiceClient;
            this.saaRequestMapper = saaRequestMapper;
            this.responseToQuestionMapper = responseToQuestionMapper;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private KBVGateway kbvGateway;
    private SAARequestMapper mockSAARequestMapper = mock(SAARequestMapper.class);
    private ResponseToQuestionMapper mockResponseToQuestionMapper =
            mock(ResponseToQuestionMapper.class);
    private KbvSoapWebServiceClient mockKbvSoapWebServiceClient =
            mock(KbvSoapWebServiceClient.class);

    @BeforeEach
    void setUp() {
        this.kbvGateway =
                new KBVGateway(
                        mockSAARequestMapper,
                        mockResponseToQuestionMapper,
                        mockKbvSoapWebServiceClient);
    }

    @Test
    void shouldCallSubmitAnswersSuccessfully() throws InterruptedException {
        // final String testRequestBody = "serialisedKbvApiRequest";
        QuestionAnswerRequest questionAnswerRequest =
                TestDataCreator.createTestQuestionAnswerRequest();

        // when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        // ArgumentCaptor<HttpRequest> httpRequestCaptor =
        // ArgumentCaptor.forClass(HttpRequest.class);
        IdentityIQWebServiceSoap mockIdentityIQWebServiceSoap =
                mock(IdentityIQWebServiceSoap.class);

        RTQResponse2 mockRtqResponse = mock(RTQResponse2.class);
        RTQRequest mockRtqRequest = mock(RTQRequest.class);
        Results mockResults = mock(Results.class);

        when(mockResponseToQuestionMapper.mapQuestionAnswersRtqRequest(questionAnswerRequest))
                .thenReturn(mockRtqRequest);
        when(mockKbvSoapWebServiceClient.getIdentityIQWebServiceSoapEndpoint())
                .thenReturn(mockIdentityIQWebServiceSoap);
        when(mockIdentityIQWebServiceSoap.rtq(mockRtqRequest)).thenReturn(mockRtqResponse);
        when(mockRtqResponse.getResults()).thenReturn(mockResults);

        QuestionsResponse questionAnswerRequestResult =
                kbvGateway.submitAnswers(questionAnswerRequest);

        // assertEquals(TEST_API_RESPONSE_BODY, questionAnswerRequestResult);
        verify(mockResponseToQuestionMapper).mapQuestionAnswersRtqRequest(questionAnswerRequest);
        // verify(mockObjectMapper).writeValueAsString(mockRtqRequest);
        // verify(mockKbvApiConfig).getEndpointUri();
        verify(mockKbvSoapWebServiceClient).getIdentityIQWebServiceSoapEndpoint();
        // assertEquals(testEndpointUri, httpRequestCaptor.getValue().uri().toString());
        // assertEquals("POST", httpRequestCaptor.getValue().method());
        // HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        // assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        // assertEquals(
        //         "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsAreProvided() {
        Map<String, KbvGatewayContructorArgs> testCases =
                Map.of(
                        "httpClient must not be null",
                        new KbvGatewayContructorArgs(null, null, null),
                        "rtqRequestMapper must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null, null, mock(KbvSoapWebServiceClient.class)),
                        "objectMapper must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null,
                                mock(ResponseToQuestionMapper.class),
                                mock(KbvSoapWebServiceClient.class)),
                        "kbvApiConfig must not be null",
                        new KBVGatewayTest.KbvGatewayContructorArgs(
                                null,
                                mock(ResponseToQuestionMapper.class),
                                mock(KbvSoapWebServiceClient.class)));

        testCases.forEach(
                (errorMessage, constructorArgs) -> {
                    assertThrows(
                            NullPointerException.class,
                            () -> {
                                new KBVGateway(
                                        constructorArgs.saaRequestMapper,
                                        constructorArgs.responseToQuestionMapper,
                                        constructorArgs.kbvSoapWebServiceClient);
                            },
                            errorMessage);
                });
    }
}
