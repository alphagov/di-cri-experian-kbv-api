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
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionRequest;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.ipv.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVServiceFactory;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KeyStoreService;
import uk.gov.di.ipv.cri.experian.kbv.api.service.KBVSystemProperty;
import uk.gov.di.ipv.cri.experian.kbv.api.validation.InputValidationExecutor;
import javax.validation.Validation;
import java.util.Map;

public class QuestionHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionHandler.class);
    private final InputValidationExecutor inputValidationExecutor;
    private final KBVService kbvService;
    private final ObjectMapper objectMapper;

    public QuestionHandler() {
        this(
                new ObjectMapper(),
                new KBVSystemProperty(
                    new KeyStoreService(ParamManager.getSecretsProvider())
                ),
                new KBVServiceFactory(),
                new InputValidationExecutor(Validation.buildDefaultValidatorFactory().getValidator())
        );
    }

    public QuestionHandler(
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

    @Override
    @Tracing(captureMode = CaptureMode.DISABLED)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.withHeaders(Map.of("Content-Type", "application/json"));
        try {
            QuestionRequest questionRequest =
                    this.objectMapper.readValue(input.getBody(), QuestionRequest.class);
            ValidationResult validationResult =
                    this.inputValidationExecutor.performInputValidation(questionRequest.getPersonIdentity());

            if (validationResult.isValid()) {
                QuestionsResponse questionsResponse = this.kbvService.getQuestions(questionRequest);
                response.withStatusCode(HttpStatus.SC_OK);
                response.withBody(objectMapper.writeValueAsString(questionsResponse));
            } else {
                response.withStatusCode(HttpStatus.SC_BAD_REQUEST);
                response.withBody(objectMapper.writeValueAsString(validationResult));
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred when attempting to retrieve questions", e);
            response.withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }

        return response;
    }
}
