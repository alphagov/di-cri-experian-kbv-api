package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.RTQRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.cri.experian.kbv.api.config.KbvApiConfig;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class KBVGateway {

    private final SAARequestMapper saaRequestMapper;
    private final RTQRequestMapper rtqRequestMapper;
    private final ObjectMapper objectMapper;
    private final KbvApiConfig kbvApiConfig;
    private final HttpClient httpClient;

    public KBVGateway(
            SAARequestMapper saaRequestMapper,
            RTQRequestMapper rtqRequestMapper,
            HttpClient httpClient,
            ObjectMapper objectMapper,
            KbvApiConfig kbvApiConfig) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(saaRequestMapper, "saaRequestMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(rtqRequestMapper, "rtqRequestMapper must not be null");
        Objects.requireNonNull(kbvApiConfig, "crossCoreApiConfig must not be null");
        this.saaRequestMapper = saaRequestMapper;
        this.rtqRequestMapper = rtqRequestMapper;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.kbvApiConfig = kbvApiConfig;
    }

    public QuestionResponse getQuestions(PersonIdentity personIdentity) {
        Object saaRequest = this.saaRequestMapper.mapPersonIdentity(personIdentity);

        // TODO: make the call to the experian SAA endpoint

        return null;
    }

    public String submitAnswers(QuestionAnswerRequest questionAnswers)
            throws IOException, InterruptedException {
        RTQRequest rtqRequest = this.rtqRequestMapper.mapQuestionAnswersRtqRequest(questionAnswers);
        String requestBody = objectMapper.writeValueAsString(rtqRequest);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(kbvApiConfig.getEndpointUri()))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
