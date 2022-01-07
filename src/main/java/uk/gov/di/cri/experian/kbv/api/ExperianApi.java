package uk.gov.di.cri.experian.kbv.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import spark.Spark;
import uk.gov.di.cri.experian.kbv.api.config.ApiConfig;
import uk.gov.di.cri.experian.kbv.api.config.KbvApiConfig;
import uk.gov.di.cri.experian.kbv.api.gateway.KBVGateway;
import uk.gov.di.cri.experian.kbv.api.gateway.RTQRequestMapper;
import uk.gov.di.cri.experian.kbv.api.gateway.SAARequestMapper;
import uk.gov.di.cri.experian.kbv.api.resource.HealthCheckResource;
import uk.gov.di.cri.experian.kbv.api.resource.QuestionAnswerResource;
import uk.gov.di.cri.experian.kbv.api.resource.QuestionResource;
import uk.gov.di.cri.experian.kbv.api.security.SSLContextFactory;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.net.ssl.SSLContext;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.net.http.HttpClient;
import java.time.Duration;

public class ExperianApi {
    private final HealthCheckResource healthCheckResource;
    private final QuestionResource questionResource;
    private final QuestionAnswerResource questionAnswerResource;

    public ExperianApi() {
        try {
            Spark.port(5008);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            InputValidationExecutor inputValidationExecutor =
                    new InputValidationExecutor(validator);

            this.healthCheckResource = new HealthCheckResource();

            KBVService kbvService = createKbvService(objectMapper);
            this.questionResource =
                    new QuestionResource(kbvService, objectMapper, inputValidationExecutor);
            this.questionAnswerResource =
                    new QuestionAnswerResource(kbvService, objectMapper, inputValidationExecutor);

            mapRoutes();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not initialise API", e); // TODO: create a dedicated Exception class
        }
    }

    private void mapRoutes() {
        Spark.get("/healthcheck", this.healthCheckResource.getCurrentHealth);
        Spark.post("/question-request", this.questionResource.getQuestions);
        Spark.post("/question-answer", this.questionAnswerResource.submitQuestionsAnswers);
    }

    private HttpClient getHttpClient(ApiConfig apiConfig) {
        SSLContext sslContext =
                new SSLContextFactory()
                        .getSSLContext(
                                apiConfig.getKeystorePath(), apiConfig.getKeystorePassword());
        return java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .sslContext(sslContext)
                .build();
    }

    private KBVService createKbvService(ObjectMapper objectMapper) {
        KbvApiConfig kbvApiConfig = new KbvApiConfig();
        HttpClient httpClient = getHttpClient(kbvApiConfig);
        return new KBVService(
                new KBVGateway(
                        new SAARequestMapper(),
                        new RTQRequestMapper(),
                        httpClient,
                        objectMapper,
                        kbvApiConfig));
    }
}
