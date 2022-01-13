package uk.gov.di.cri.experian.kbv.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerResponse;
import uk.gov.di.cri.experian.kbv.api.gateway.KBVGateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KBVServiceTest {
    private KBVService kbvService;
    private KBVGateway mockKbvGateway = mock(KBVGateway.class);

    @BeforeEach
    void setUp() {
        this.kbvService = new KBVService(mockKbvGateway);
    }

    @Test
    void shouldReturnAResultWhenKbvServiceIsInvokedSuccessfully() throws InterruptedException {
        QuestionAnswerResponse answerResponseResult = mock(QuestionAnswerResponse.class);
        QuestionAnswerRequest mockQuestionAnswerRequest = mock(QuestionAnswerRequest.class);
        when(mockKbvGateway.submitAnswers(mockQuestionAnswerRequest))
                .thenReturn(answerResponseResult);

        QuestionAnswerResponse result = kbvService.submitAnswers(mockQuestionAnswerRequest);
        verify(mockKbvGateway).submitAnswers(mockQuestionAnswerRequest);
        assertEquals(answerResponseResult, result);
    }

    @Test
    void shouldReturnNullWhenAnExceptionOccursWhenInvokingKbvService() throws InterruptedException {
        QuestionAnswerRequest mockQuestionAnswerRequest = mock(QuestionAnswerRequest.class);

        when(mockKbvGateway.submitAnswers(mockQuestionAnswerRequest))
                .thenThrow(new InterruptedException());

        QuestionAnswerResponse result = kbvService.submitAnswers(mockQuestionAnswerRequest);

        assertNull(result);
    }
}
