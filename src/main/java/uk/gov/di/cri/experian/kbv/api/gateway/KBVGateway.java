package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.IdentityIQWebServiceSoap;
import com.experian.uk.schema.experian.identityiq.services.webservice.RTQRequest;
import com.experian.uk.schema.experian.identityiq.services.webservice.RTQResponse2;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerResponse;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionResponse;
import uk.gov.di.cri.experian.kbv.api.security.KbvSoapWebServiceClient;

import java.util.Objects;

public class KBVGateway {

    private final SAARequestMapper saaRequestMapper;
    private final ResponseToQuestionMapper responseToQuestionMapper;
    private final KbvSoapWebServiceClient kbvSoapWebServiceClient;

    public KBVGateway(
            SAARequestMapper saaRequestMapper,
            ResponseToQuestionMapper responseToQuestionMapper,
            KbvSoapWebServiceClient kbvSoapWebServiceClient) {
        Objects.requireNonNull(kbvSoapWebServiceClient, "httpClient must not be null");
        Objects.requireNonNull(saaRequestMapper, "saaRequestMapper must not be null");
        Objects.requireNonNull(responseToQuestionMapper, "rtqRequestMapper must not be null");
        this.saaRequestMapper = saaRequestMapper;
        this.responseToQuestionMapper = responseToQuestionMapper;
        this.kbvSoapWebServiceClient = kbvSoapWebServiceClient;
    }

    public QuestionResponse getQuestions(PersonIdentity personIdentity) {
        Object saaRequest = this.saaRequestMapper.mapPersonIdentity(personIdentity);

        SAARequestDto apiRequest = saaRequestMapper.mapPersonIdentity(personIdentity);

        IdentityIQWebServiceSoap identityIQWebServiceSoap =
                kbvSoapWebServiceClient.getIdentityIQWebServiceSoapEndpoint();
        SAAResponse2 saaResponse2 = getSaaResponse2(identityIQWebServiceSoap, apiRequest);
        System.out.println(saaResponse2);
        QuestionsResponse result =
                saaRequestMapper.mapSAAResponse2ToQuestionsResponse(saaResponse2);

        return result;
    }

    private SAAResponse2 getSaaResponse2(
            IdentityIQWebServiceSoap soapEndpoint, SAARequestDto saaRequestDto) {
        SAARequest saaRequest = createRequest(saaRequestDto);
        SAAResponse2 result = soapEndpoint.saa(saaRequest);
        return result;
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

    private void setApplicationData(SAARequest saaRequest) {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setApplicationType("IG");
        applicationData.setChannel("IN");
        applicationData.setSearchConsent("Y");
        applicationData.setProduct("3 out of 4");
        saaRequest.setApplicationData(applicationData);
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

    private void setApplicant(SAARequest saaRequest, SAARequestDto dto) {
        Applicant applicant = new Applicant();
        applicant.setApplicantIdentifier("1");
        ApplicantName name = new ApplicantName();

        if (StringUtils.isNotBlank(dto.getFirstName())) {
            name.setForename(dto.getFirstName());
        }

        if (StringUtils.isNotBlank(dto.getSurname())) {
            name.setSurname(dto.getSurname());
        }

        if (StringUtils.isNotBlank(dto.getTitle())) {
            name.setTitle(dto.getTitle());
        }

        applicant.setName(name);
        ApplicantDateOfBirth dateOfBirth = new ApplicantDateOfBirth();

        if (StringUtils.isNotBlank(dto.getDobDD())) {
            dateOfBirth.setDD(Integer.parseInt(dto.getDobDD()));
        }

        if (StringUtils.isNotBlank(dto.getDobMM())) {
            dateOfBirth.setMM(Integer.parseInt(dto.getDobMM()));
        }

        if (StringUtils.isNotBlank(dto.getDobCCYY())) {
            dateOfBirth.setCCYY(Integer.parseInt(dto.getDobCCYY()));
        }
        applicant.setDateOfBirth(dateOfBirth);

        saaRequest.setApplicant(applicant);
    }

    private void setLocationDetails(SAARequest saaRequest, SAARequestDto dto) {
        LocationDetails locationDetails = new LocationDetails();
        locationDetails.setLocationIdentifier(1);
        LocationDetailsUKLocation ukLocation = new LocationDetailsUKLocation();

        if (StringUtils.isNotBlank(dto.getPostcode())) {
            ukLocation.setPostcode(dto.getPostcode());
        }

        if (StringUtils.isNotBlank(dto.getDistrict())) {
            ukLocation.setDistrict(dto.getDistrict());
        }

        if (StringUtils.isNotBlank(dto.getFlat())) {
            ukLocation.setFlat(dto.getFlat());
        }

        if (StringUtils.isNotBlank(dto.getPostTown())) {
            ukLocation.setPostTown(dto.getPostTown());
        }

        if (StringUtils.isNotBlank(dto.getStreet())) {
            ukLocation.setStreet(dto.getStreet());
        }

        if (StringUtils.isNotBlank(dto.getHouseNo())) {
            ukLocation.setHouseNumber(dto.getHouseNo());
        }

        locationDetails.setUKLocation(ukLocation);
        //        locationDetails.setClientLocationID("1");
        saaRequest.getLocationDetails().add(locationDetails);
    }

    public QuestionAnswerResponse submitAnswers(QuestionAnswerRequest questionAnswerRequest)
            throws InterruptedException {
        RTQRequest rtqRequest =
                this.responseToQuestionMapper.mapQuestionAnswersRtqRequest(questionAnswerRequest);

        IdentityIQWebServiceSoap identityIQWebServiceSoap =
                kbvSoapWebServiceClient.getIdentityIQWebServiceSoapEndpoint();
        RTQResponse2 rtqResponse2 = identityIQWebServiceSoap.rtq(rtqRequest);
        return this.responseToQuestionMapper.mapResultsToMapQuestionAnswersResponse(rtqResponse2);
    }
}
