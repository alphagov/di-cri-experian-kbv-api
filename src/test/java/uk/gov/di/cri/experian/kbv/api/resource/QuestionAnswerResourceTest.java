package uk.gov.di.cri.experian.kbv.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.cri.experian.kbv.api.util.TestDataCreator.createTestQuestionAnswerRequest;

@ExtendWith(MockitoExtension.class)
class QuestionAnswerResourceTest {
    private QuestionAnswerResource questionAnswerResource;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private InputValidationExecutor mockInputValidationExecutor;
    @Mock private KBVService mockKbvService;

    @BeforeEach
    void setUp() {
        this.questionAnswerResource =
                new QuestionAnswerResource(
                        mockKbvService, mockObjectMapper, mockInputValidationExecutor);
    }

    @Test
    void returns200OkWhenSubmitQuestionAnswerHasValidInput() throws Exception {
        final String theRequestBody = "request-body";
        final QuestionsResponse submitAnswerResponseBody = mock(QuestionsResponse.class);

        spark.Request mockRequest = mock(spark.Request.class);
        spark.Response mockResponse = mock(spark.Response.class);
        QuestionAnswerRequest testAnswerRequest = createTestQuestionAnswerRequest();

        when(mockObjectMapper.readValue(theRequestBody, QuestionAnswerRequest.class))
                .thenReturn(testAnswerRequest);
        when(mockInputValidationExecutor.performInputValidation(testAnswerRequest))
                .thenReturn(new ValidationResult(Collections.emptyList()));
        when(mockKbvService.submitAnswers(testAnswerRequest)).thenReturn(submitAnswerResponseBody);
        when(mockRequest.body()).thenReturn(theRequestBody);
        when(mockObjectMapper.writeValueAsString(submitAnswerResponseBody))
                .thenReturn("submitAnswerResponseBody");

        questionAnswerResource.submitQuestionsAnswers.handle(mockRequest, mockResponse);
        verify(mockResponse).status(HttpServletResponse.SC_OK);
        verify(mockResponse).header("Content-Type", "application/json");
        verify(mockResponse).body("submitAnswerResponseBody");
    }

    @Test
    void returns400BadRequestWhenSubmitQuestionAnswerHasInValid() throws Exception {
        final String theRequestBody = "request-body";
        final String errorMessage = "urn must not be null or empty";
        final String submitAnswerResponseBody = "{\"errors\":[\"" + errorMessage + "\"]}";

        QuestionAnswerRequest testAnswerRequest = createTestQuestionAnswerRequest();
        spark.Request mockRequest = mock(spark.Request.class);
        spark.Response mockResponse = mock(spark.Response.class);

        when(mockRequest.body()).thenReturn(theRequestBody);
        when(mockObjectMapper.readValue(theRequestBody, QuestionAnswerRequest.class))
                .thenReturn(testAnswerRequest);
        when(mockObjectMapper.writeValueAsString(any(ValidationResult.class)))
                .thenReturn(submitAnswerResponseBody);
        when(mockInputValidationExecutor.performInputValidation(testAnswerRequest))
                .thenReturn(new ValidationResult(List.of("urn must not be null or empty")));

        questionAnswerResource.submitQuestionsAnswers.handle(mockRequest, mockResponse);

        verify(mockObjectMapper).readValue(theRequestBody, QuestionAnswerRequest.class);
        verify(mockKbvService, never()).submitAnswers(testAnswerRequest);
        verify(mockResponse).status(HttpServletResponse.SC_BAD_REQUEST);
        verify(mockResponse).header("Content-Type", "application/json");
        verify(mockResponse).body(submitAnswerResponseBody);
    }
}
