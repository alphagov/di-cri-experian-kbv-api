package uk.gov.di.ipv.cri.experian.kbv.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionRequest;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVServiceFactory;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KeyStoreService;
import uk.gov.di.ipv.cri.experian.kbv.api.validation.InputValidationExecutor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionHandlerTest {
    private QuestionHandler questionHandler;
    @Mock private KBVServiceFactory kbvServiceFactoryMock;
    @Mock private KBVService kbvServiceMock;
    @Mock private ObjectMapper objectMapperMock;
    @Mock private KeyStoreService keyStoreServiceMock;
    @Mock private InputValidationExecutor inputValidationExecutorMock;

    @BeforeEach
    void setUp() {
        AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.standard().withContextMissingStrategy(new LogErrorContextMissingStrategy()).build());
        when(kbvServiceFactoryMock.create()).thenReturn(kbvServiceMock);
        when(keyStoreServiceMock.getKeyStorePath()).thenReturn("keystore-value");
        when(keyStoreServiceMock.getPassword()).thenReturn("keystore-password");
        questionHandler = new QuestionHandler(
                objectMapperMock,
                keyStoreServiceMock,
                kbvServiceFactoryMock,
                inputValidationExecutorMock
        );
    }

    @Test
    void shouldReturn200OkWhenInputIsValid() throws JsonProcessingException {
        final String goodResponseBody = "question-response";
        QuestionRequest questionRequest = mock(QuestionRequest.class);
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        ValidationResult validationResultMock = mock(ValidationResult.class);
        QuestionsResponse questionsResponse = new QuestionsResponse();

        when(objectMapperMock.readValue(input.getBody(), QuestionRequest.class)).thenReturn(questionRequest);
        when(kbvServiceMock.getQuestions(questionRequest)).thenReturn(questionsResponse);
        when(objectMapperMock.writeValueAsString(questionsResponse)).thenReturn(goodResponseBody);
        when(inputValidationExecutorMock.performInputValidation(questionRequest.getPersonIdentity())).thenReturn(validationResultMock);
        when(validationResultMock.isValid()).thenReturn(true);
        APIGatewayProxyResponseEvent result = questionHandler
                .handleRequest(input, mock(Context.class));

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        assertEquals(goodResponseBody, result.getBody());
    }

    @Test
    void shouldReturn400BadRequestWhenInputInValid() throws JsonProcessingException {
        final String badResponseBody = "bad-response";
        QuestionRequest questionRequest = mock(QuestionRequest.class);
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        ValidationResult validationResultMock = mock(ValidationResult.class);

        when(inputValidationExecutorMock.performInputValidation(questionRequest.getUrn())).thenReturn(validationResultMock);
        when(objectMapperMock.readValue(input.getBody(), QuestionRequest.class)).thenReturn(questionRequest);
        when(objectMapperMock.writeValueAsString(validationResultMock)).thenReturn(badResponseBody);

        APIGatewayProxyResponseEvent result = questionHandler
                .handleRequest(input, mock(Context.class));

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_BAD_REQUEST,result.getStatusCode()) ;
        assertEquals(badResponseBody, result.getBody());
    }

    @Test
    void shouldReturn500ErrorWhenAServerErrorOccurs() {
        Context contextMock = mock(Context.class);

        APIGatewayProxyResponseEvent result = questionHandler
                .handleRequest(mock(APIGatewayProxyRequestEvent.class), contextMock);

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().contains("error"));
    }
}
