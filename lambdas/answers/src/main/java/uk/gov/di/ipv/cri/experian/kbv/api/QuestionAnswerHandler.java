package uk.gov.di.ipv.cri.experian.kbv.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.tracing.CaptureMode;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVServiceFactory;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KeyStoreService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVSystemProperty;
import uk.gov.di.ipv.cri.experian.kbv.api.validation.InputValidationExecutor;
import javax.validation.Validation;
import java.util.Map;

public class QuestionAnswerHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionAnswerHandler.class);
    private final InputValidationExecutor inputValidationExecutor;
    private final KBVService kbvService;
    private final ObjectMapper objectMapper;

    public QuestionAnswerHandler() {
        this(
                new ObjectMapper(),
                new KBVSystemProperty(
                    new KeyStoreService(ParamManager.getSecretsProvider())
                ),
                new KBVServiceFactory(),
                new InputValidationExecutor(Validation.buildDefaultValidatorFactory().getValidator())
        );
    }

    public QuestionAnswerHandler(
            ObjectMapper objectMapper,
            KBVSystemProperty systemProperty,
            KBVServiceFactory kbvServiceFactory,
            InputValidationExecutor inputValidationExecutor
    )  {
        this.inputValidationExecutor = inputValidationExecutor;
        this.objectMapper = objectMapper;
        this.kbvService = kbvServiceFactory.create();
        this.objectMapper.registerModule(new JavaTimeModule());

        systemProperty.save();
    }

    @Tracing(captureMode = CaptureMode.DISABLED)
    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.withHeaders(Map.of("Content-Type", "application/json"));

        try {
            QuestionAnswerRequest questionsAnswersRequest =
                    objectMapper.readValue(input.getBody(), QuestionAnswerRequest.class);

            ValidationResult validationResult =
                    this.inputValidationExecutor.performInputValidation(questionsAnswersRequest);

            if (validationResult.isValid()) {
                response.withStatusCode(HttpStatus.SC_OK);
                response.withBody(
                        objectMapper.writeValueAsString(
                                this.kbvService.submitAnswers(questionsAnswersRequest)));
            } else {
                response.withStatusCode(HttpStatus.SC_BAD_REQUEST);
                response.withBody(objectMapper.writeValueAsString(validationResult));
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred whilst attempting to submit", e);
            response.withBody("{\"error\": \"" + e.getMessage() + "\"}");
            response.withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}
