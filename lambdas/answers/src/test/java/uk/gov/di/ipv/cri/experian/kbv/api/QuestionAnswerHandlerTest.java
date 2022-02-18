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
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVServiceFactory;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVSystemProperty;
import uk.gov.di.ipv.cri.experian.kbv.api.validation.InputValidationExecutor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionAnswerHandlerTest {
    private QuestionAnswerHandler questionAnswerHandler;
    @Mock private KBVServiceFactory kbvServiceFactoryMock;
    @Mock private KBVService kbvServiceMock;
    @Mock private ObjectMapper objectMapperMock;
    @Mock private KBVSystemProperty systemPropertyMock;
    @Mock private InputValidationExecutor inputValidationExecutorMock;

    @BeforeEach
    void setUp() {
        AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.standard().withContextMissingStrategy(new LogErrorContextMissingStrategy()).build());
        when(kbvServiceFactoryMock.create()).thenReturn(kbvServiceMock);
        doNothing().when(systemPropertyMock).save();
        questionAnswerHandler = new QuestionAnswerHandler(
                objectMapperMock,
                systemPropertyMock,
                kbvServiceFactoryMock,
                inputValidationExecutorMock
        );
    }

    @Test
    void shouldReturn200OkWhenInputIsValid() throws JsonProcessingException {
        final String goodResponseBody = "answer-response";
        QuestionAnswerRequest questionAnswerRequest = mock(QuestionAnswerRequest.class);
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        ValidationResult validationResultMock = mock(ValidationResult.class);
        QuestionsResponse questionsResponse = new QuestionsResponse();

        when(objectMapperMock.readValue(input.getBody(), QuestionAnswerRequest.class)).thenReturn(questionAnswerRequest);
        when(kbvServiceMock.submitAnswers(questionAnswerRequest)).thenReturn(questionsResponse);
        when(objectMapperMock.writeValueAsString(questionsResponse)).thenReturn(goodResponseBody);
        when(inputValidationExecutorMock.performInputValidation(questionAnswerRequest)).thenReturn(validationResultMock);
        when(validationResultMock.isValid()).thenReturn(true);
        APIGatewayProxyResponseEvent result = questionAnswerHandler
                .handleRequest(input, mock(Context.class));

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        assertEquals(goodResponseBody, result.getBody());
    }

    @Test
    void shouldReturn400BadRequestWhenInputInValid() throws JsonProcessingException {
        final String badResponseBody = "bad-response";
        QuestionAnswerRequest questionAnswerRequest = mock(QuestionAnswerRequest.class);
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);
        ValidationResult validationResultMock = mock(ValidationResult.class);

        when(inputValidationExecutorMock.performInputValidation(questionAnswerRequest)).thenReturn(validationResultMock);
        when(objectMapperMock.readValue(input.getBody(), QuestionAnswerRequest.class)).thenReturn(questionAnswerRequest);
        when(objectMapperMock.writeValueAsString(validationResultMock)).thenReturn(badResponseBody);
        when(validationResultMock.isValid()).thenReturn(false);


        APIGatewayProxyResponseEvent result = questionAnswerHandler
                .handleRequest(input, mock(Context.class));

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_BAD_REQUEST,result.getStatusCode()) ;
        assertEquals(badResponseBody, result.getBody());
    }

    @Test
    void shouldReturn500ErrorWhenGeneratedAtTheServer() {
        Context contextMock = mock(Context.class);
        APIGatewayProxyResponseEvent result = questionAnswerHandler
                .handleRequest(mock(APIGatewayProxyRequestEvent.class), contextMock);

        assertEquals(Map.of("Content-Type", "application/json"), result.getHeaders());
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().contains("error"));
    }
}