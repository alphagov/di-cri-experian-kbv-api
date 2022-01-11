package uk.gov.di.ipv.cri.kbv;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.experian.uk.schema.experian.identityiq.services.webservice.Applicant;
import com.experian.uk.schema.experian.identityiq.services.webservice.ApplicantDateOfBirth;
import com.experian.uk.schema.experian.identityiq.services.webservice.ApplicantName;
import com.experian.uk.schema.experian.identityiq.services.webservice.ApplicationData;
import com.experian.uk.schema.experian.identityiq.services.webservice.Control;
import com.experian.uk.schema.experian.identityiq.services.webservice.IdentityIQWebService;
import com.experian.uk.schema.experian.identityiq.services.webservice.IdentityIQWebServiceSoap;
import com.experian.uk.schema.experian.identityiq.services.webservice.LocationDetails;
import com.experian.uk.schema.experian.identityiq.services.webservice.LocationDetailsUKLocation;
import com.experian.uk.schema.experian.identityiq.services.webservice.Parameters;
import com.experian.uk.schema.experian.identityiq.services.webservice.Question;
import com.experian.uk.schema.experian.identityiq.services.webservice.Residency;
import com.experian.uk.schema.experian.identityiq.services.webservice.Response;
import com.experian.uk.schema.experian.identityiq.services.webservice.SAARequest;
import com.experian.uk.schema.experian.identityiq.services.webservice.SAAResponse2;
import com.experian.uk.wasp.TokenService;
import com.experian.uk.wasp.TokenServiceSoap;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.cxf.common.util.StringUtils;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GetQuestionsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    SecretsProvider secretsProvider = ParamManager.getSecretsProvider();
    String value = secretsProvider.get(System.getenv("KEYSTORE"));

    public GetQuestionsHandler() throws IOException {

        String keystoreBase64 = System.getenv("KEYSTORE_BASE64");
        if (keystoreBase64 != null) {
            Path tempFile = Files.createTempFile(null, null);
            Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));
            System.setProperty("javax.net.ssl.keyStore", tempFile.toString());
        } else {
            System.err.println("env var KEYSTORE_BASE64 is not present");
        }

        String keystorePassword = System.getenv("KEYSTORE_PASSWORD");
        if (keystorePassword != null) {
            System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
        } else {
            System.err.println("env var KEYSTORE_PASSWORD is not present");

        }
    }

    @Override
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        String jsonString = input.getBody();

        Gson gson = new Gson();
        SAARequestDto saaRequestDto = gson.fromJson(jsonString, SAARequestDto.class);

        String token = getToken();
        IdentityIQWebServiceSoap soapEndpoint = getIdentityIQWebServiceSoapEndpoint(token);
        SAAResponse2 saaResponse2 = getSaaResponse2(soapEndpoint, saaRequestDto);

        String json = gson.toJson(saaResponse2);

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setBody(json);
        return responseEvent;


    }

    private SAAResponse2 getSaaResponse2(IdentityIQWebServiceSoap soapEndpoint, SAARequestDto saaRequestDto) {
        SAARequest saaRequest = createRequest(saaRequestDto);
        SAAResponse2 result = soapEndpoint.saa(saaRequest);
        return result;
    }

    private IdentityIQWebServiceSoap getIdentityIQWebServiceSoapEndpoint(String token) {
        IdentityIQWebService service = new IdentityIQWebService();
        service.setHandlerResolver(new HeaderHandlerResolver(token));
        IdentityIQWebServiceSoap soapEndpoint = service.getIdentityIQWebServiceSoap();
        return soapEndpoint;
    }

    private Response getRtqResponses(LinkedTreeMap person, Question question) {
        Response rtQResponse = new Response();
        String spreedSheetQuestion = yank2MiddleZeroes(question.getQuestionID());

        rtQResponse.setQuestionID(question.getQuestionID());
        rtQResponse.setAnswerGiven((String) person.get(spreedSheetQuestion));
        rtQResponse.setCustResponseFlag(0);
        rtQResponse.setAnswerActionFlag("U");
        return rtQResponse;
    }

    private String yank2MiddleZeroes(String questionId) {
        char[] question = questionId.toCharArray();
        return IntStream.range(0, question.length)
                .filter(index -> index != 2 && index != 3)
                .mapToObj(index -> question[index])
                .map(String::valueOf)
                .collect(Collectors.joining())
                .toLowerCase();
    }

    private List<LinkedTreeMap> filterExperianDataSample(SAARequestDto saaRequestDto) {
        return ((List<LinkedTreeMap>) getExperianData().get("people")).stream()
                .filter(e -> ((String) e.get("name")).equalsIgnoreCase(saaRequestDto.getFirstName()))
                .filter(e -> ((String) e.get("surname")).equalsIgnoreCase(saaRequestDto.getSurname()))
                .filter(e -> ((String) e.get("postcode")).equalsIgnoreCase(saaRequestDto.getPostcode()))
                .collect(Collectors.toList());
    }


    private SAARequest createRequest(SAARequestDto dto) {
        SAARequest saaRequest = new SAARequest();
        setApplicant(saaRequest, dto);
        setApplicationData(saaRequest);
        setControl(saaRequest);
        setLocationDetails(saaRequest, dto);
        setResidency(saaRequest);
        return saaRequest;
    }

    private void setResidency(SAARequest saaRequest) {
        Residency residency = new Residency();
        residency.setApplicantIdentifier(1);
        residency.setLocationIdentifier(1);
        saaRequest.getResidency().add(residency);
    }

    private void setControl(SAARequest saaRequest) {
        saaRequest.setControl(createControl());
    }

    private Control createControl() {
        Control control = new Control();
        control.setTestDatabase("A");
        Parameters parameters = new Parameters();
        parameters.setOneShotAuthentication("N");
        parameters.setStoreCaseData("P");
        control.setParameters(parameters);
        control.setURN(UUID.randomUUID().toString());
        control.setOperatorID("GDSCABINETUIIQ01U");
        return control;
    }

    private void setApplicationData(SAARequest saaRequest) {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setApplicationType("IG");
        applicationData.setChannel("IN");
        applicationData.setSearchConsent("Y");
        applicationData.setProduct("ALL");
        saaRequest.setApplicationData(applicationData);
    }

    private void setApplicant(SAARequest saaRequest, SAARequestDto dto) {
        Applicant applicant = new Applicant();
        applicant.setApplicantIdentifier("1");
        ApplicantName name = new ApplicantName();

        if (StringUtils.isEmpty(dto.getFirstName())) {
            name.setForename(dto.getFirstName());
        }

        if (StringUtils.isEmpty(dto.getSurname())) {
            name.setSurname(dto.getSurname());
        }

        if (StringUtils.isEmpty(dto.getTitle())) {
            name.setTitle(dto.getTitle());
        }

        applicant.setName(name);
        ApplicantDateOfBirth dateOfBirth = new ApplicantDateOfBirth();

        if (StringUtils.isEmpty(dto.getDobDD())) {
            dateOfBirth.setDD(Integer.parseInt(dto.getDobDD()));
        }

        if (StringUtils.isEmpty(dto.getDobMM())) {
            dateOfBirth.setMM(Integer.parseInt(dto.getDobMM()));
        }

        if (StringUtils.isEmpty(dto.getDobCCYY())) {
            dateOfBirth.setCCYY(Integer.parseInt(dto.getDobCCYY()));
        }
        applicant.setDateOfBirth(dateOfBirth);

        saaRequest.setApplicant(applicant);
    }

    private void setLocationDetails(SAARequest saaRequest, SAARequestDto dto) {
        LocationDetails locationDetails = new LocationDetails();
        locationDetails.setLocationIdentifier(1);
        LocationDetailsUKLocation ukLocation = new LocationDetailsUKLocation();

        if (StringUtils.isEmpty(dto.getPostcode())) {
            ukLocation.setPostcode(dto.getPostcode());
        }

        if (StringUtils.isEmpty(dto.getDistrict())) {
            ukLocation.setDistrict(dto.getDistrict());
        }

        if (StringUtils.isEmpty(dto.getFlat())) {
            ukLocation.setFlat(dto.getFlat());
        }

        if (StringUtils.isEmpty(dto.getPostTown())) {
            ukLocation.setPostTown(dto.getPostTown());
        }

        if (StringUtils.isEmpty(dto.getStreet())) {
            ukLocation.setStreet(dto.getStreet());
        }

        if (StringUtils.isEmpty(dto.getHouseNo())) {
            ukLocation.setHouseNumber(dto.getHouseNo());
        }

        locationDetails.setUKLocation(ukLocation);
//        locationDetails.setClientLocationID("1");
        saaRequest.getLocationDetails().add(locationDetails);
    }

    private String getToken() {
        TokenService tokenService = new TokenService();
        TokenServiceSoap tokenServiceSoap = tokenService.getTokenServiceSoap();
        String token = tokenServiceSoap.loginWithCertificate("GDS DI", true);
        if (token == null || token.contains("Error")) {
            throw new RuntimeException(token);
        }
        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private Map getExperianData() {
        try {
            try (Reader reader = Files.newBufferedReader(Paths.get("src/main/resources/data/experian_sample.json"))) {
                Gson gson = new Gson();
                return gson.fromJson(reader, Map.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
