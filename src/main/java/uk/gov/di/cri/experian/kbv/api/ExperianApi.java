package uk.gov.di.cri.experian.kbv.api;

import com.experian.uk.schema.experian.identityiq.services.webservice.IdentityIQWebService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import spark.Spark;
import uk.gov.di.cri.experian.kbv.api.gateway.KBVGateway;
import uk.gov.di.cri.experian.kbv.api.gateway.ResponseToQuestionMapper;
import uk.gov.di.cri.experian.kbv.api.gateway.SAARequestMapper;
import uk.gov.di.cri.experian.kbv.api.resource.HealthCheckResource;
import uk.gov.di.cri.experian.kbv.api.resource.QuestionAnswerResource;
import uk.gov.di.cri.experian.kbv.api.resource.QuestionResource;
import uk.gov.di.cri.experian.kbv.api.security.Base64TokenCacheLoader;
import uk.gov.di.cri.experian.kbv.api.security.HeaderHandler;
import uk.gov.di.cri.experian.kbv.api.security.HeaderHandlerResolver;
import uk.gov.di.cri.experian.kbv.api.security.KBVClientFactory;
import uk.gov.di.cri.experian.kbv.api.service.KBVService;
import uk.gov.di.cri.experian.kbv.api.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ExperianApi {
    private final HealthCheckResource healthCheckResource;
    private final QuestionResource questionResource;
    private final QuestionAnswerResource questionAnswerResource;

    public ExperianApi() {
        try {
            Spark.port(8080);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            InputValidationExecutor inputValidationExecutor =
                    new InputValidationExecutor(validator);

            this.healthCheckResource = new HealthCheckResource();

            KBVService kbvService = createKbvService();
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

    private KBVService createKbvService() {
        var headerHandler = new HeaderHandler(new Base64TokenCacheLoader());
        return new KBVService(
                new KBVGateway(
                        new SAARequestMapper(),
                        new ResponseToQuestionMapper(),
                        new KBVClientFactory(
                                        new IdentityIQWebService(),
                                        new HeaderHandlerResolver(headerHandler))
                                .createClient()));
    }
}
