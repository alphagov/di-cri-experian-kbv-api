package uk.gov.di.cri.experian.kbv.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SecretsProvider;
import software.amazon.lambda.powertools.tracing.CaptureMode;
import software.amazon.lambda.powertools.tracing.Tracing;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.domain.ValidationResult;
import uk.gov.di.cri.experian.kbv.api.gateway.KBVGateway;
import uk.gov.di.cri.experian.kbv.api.gateway.ResponseToQuestionMapper;
import uk.gov.di.cri.experian.kbv.api.gateway.SAARequestMapper;
import uk.gov.di.cri.experian.kbv.api.security.KBVClientFactory;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public class QuestionHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionHandler.class);
    private static final SecretsProvider SECRETS_PROVIDER = ParamManager.getSecretsProvider();

    private final InputValidationExecutor inputValidationExecutor;
    private final KBVService kbvService;
    private final ObjectMapper objectMapper;

    static {
        try {
            String keystoreBase64 =
                    SECRETS_PROVIDER.get("/dev/di-ipv-cri-experian-kbv-api/keystore");
            Path tempFile = Files.createTempFile(null, null);
            Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));

            System.setProperty("javax.net.ssl.keyStore", tempFile.toString());
            System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
            System.setProperty(
                    "javax.net.ssl.keyStorePassword",
                    SECRETS_PROVIDER.get("/dev/di-ipv-cri-experian-kbv-api/keystore-password"));
        } catch (IOException e) {
            LOGGER.error("Static initialisation failed", e);
        }
    }

    public QuestionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        this.inputValidationExecutor = new InputValidationExecutor(validator);

        this.kbvService =
                new KBVService(
                        new KBVGateway(
                                new SAARequestMapper(),
                                new ResponseToQuestionMapper(),
                                KBVClientFactory.createClient("GDS DI", true)));
    }

    public QuestionHandler(
            KBVService kbvService,
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor) {
        this.inputValidationExecutor = inputValidationExecutor;
        this.kbvService = kbvService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Tracing(captureMode = CaptureMode.DISABLED)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
        int responseStatusCode;
        String responseBody;

        try {
            PersonIdentity personIdentity =
                    this.objectMapper.readValue(input.getBody(), PersonIdentity.class);

            ValidationResult validationResult =
                    this.inputValidationExecutor.performInputValidation(personIdentity);

            if (validationResult.isValid()) {
                QuestionsResponse questionsResponse = this.kbvService.getQuestions(personIdentity);
                responseStatusCode = 200;
                responseBody = objectMapper.writeValueAsString(questionsResponse);
            } else {
                responseStatusCode = 400;
                responseBody = objectMapper.writeValueAsString(validationResult);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred when attempting to retrieve questions", e);
            responseStatusCode = 500;
            responseBody = "{\"error\": \"" + e + "\"}";
        }

        return createResponseEvent(responseStatusCode, responseBody, responseHeaders);
    }

    private static APIGatewayProxyResponseEvent createResponseEvent(
            int statusCode, String body, Map<String, String> headers) {
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent =
                new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setHeaders(headers);
        apiGatewayProxyResponseEvent.setStatusCode(statusCode);
        apiGatewayProxyResponseEvent.setBody(body);

        return apiGatewayProxyResponseEvent;
    }
}
