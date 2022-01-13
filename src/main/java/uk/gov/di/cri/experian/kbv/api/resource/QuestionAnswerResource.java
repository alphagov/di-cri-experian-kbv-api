package uk.gov.di.cri.experian.kbv.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.servlet.http.HttpServletResponse;

public class QuestionAnswerResource {
    private InputValidationExecutor inputValidationExecutor;
    private KBVService kbvService;
    private ObjectMapper objectMapper;

    public QuestionAnswerResource(
            KBVService kbvService,
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor) {
        this.objectMapper = objectMapper;
        this.inputValidationExecutor = inputValidationExecutor;
        this.kbvService = kbvService;
    }

    public final Route submitQuestionsAnswers =
            (Request request, Response response) -> {
                QuestionAnswerRequest questionsAnswersRequest =
                        objectMapper.readValue(request.body(), QuestionAnswerRequest.class);

                String responseBody;
                int responseStatusCode;

                ValidationResult validationResult =
                        this.inputValidationExecutor.performInputValidation(
                                questionsAnswersRequest);

                if (validationResult.isValid()) {
                    responseStatusCode = HttpServletResponse.SC_OK;
                    responseBody =
                            objectMapper.writeValueAsString(
                                    this.kbvService.submitAnswers(questionsAnswersRequest));
                } else {
                    responseStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                    responseBody = objectMapper.writeValueAsString(validationResult);
                }
                response.header("Content-Type", "application/json");
                response.status(responseStatusCode);
                response.body(responseBody);
                return response.body();
            };

    public QuestionAnswerResource() {}
}
