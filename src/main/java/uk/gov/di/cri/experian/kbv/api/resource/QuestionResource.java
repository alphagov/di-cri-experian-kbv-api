package uk.gov.di.cri.experian.kbv.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.servlet.http.HttpServletResponse;

public class QuestionResource {

    private InputValidationExecutor inputValidationExecutor;
    private KBVService kbvService;
    private ObjectMapper objectMapper;

    public QuestionResource(
            KBVService kbvService,
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor) {
        this.objectMapper = objectMapper;
        this.inputValidationExecutor = inputValidationExecutor;
        this.kbvService = kbvService;
    }

    public final Route getQuestions =
            (Request request, Response response) -> {
                PersonIdentity personIdentity =
                        this.objectMapper.readValue(request.body(), PersonIdentity.class);

                ValidationResult validationResult =
                        this.inputValidationExecutor.performInputValidation(personIdentity);

                String responseBody;
                int responseStatusCode;

                if (validationResult.isValid()) {
                    QuestionsResponse questionsResponse =
                            this.kbvService.getQuestions(personIdentity);
                    responseStatusCode = HttpServletResponse.SC_OK;
                    responseBody = objectMapper.writeValueAsString(questionsResponse);
                } else {
                    responseStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                    responseBody = objectMapper.writeValueAsString(validationResult);
                }

                response.header("Content-Type", "application/json");
                response.status(responseStatusCode);
                response.body(responseBody);

                return response.body();
            };
}
