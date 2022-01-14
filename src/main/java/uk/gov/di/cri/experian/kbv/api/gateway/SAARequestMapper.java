package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.SAAResponse2;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.gateway.dto.SAARequestDto;

public class SAARequestMapper {

    public SAARequestDto mapPersonIdentity(PersonIdentity personIdentity) {
        SAARequestDto apiRequest = new SAARequestDto();
        apiRequest.setTitle(personIdentity.getTitle());
        apiRequest.setFirstName(personIdentity.getFirstName());
        apiRequest.setSurname(personIdentity.getSurname());
        apiRequest.setHouseNo(personIdentity.getAddresses().get(0).getHouseNameNumber());
        apiRequest.setPostcode(personIdentity.getAddresses().get(0).getPostcode());
        apiRequest.setDobDD(personIdentity.getDateOfBirth().getDayOfMonth() + "");
        apiRequest.setDobMM(personIdentity.getDateOfBirth().getMonth().getValue() + "");
        apiRequest.setDobCCYY(personIdentity.getDateOfBirth().getYear() + "");

        return apiRequest;
    }

    public QuestionsResponse mapSAAResponse2ToQuestionsResponse(SAAResponse2 sAAResponse2) {
        QuestionsResponse questionsResponse = new QuestionsResponse();
        questionsResponse.setQuestions(sAAResponse2.getQuestions());
        questionsResponse.setControl(sAAResponse2.getControl());
        questionsResponse.setError(sAAResponse2.getError());
        questionsResponse.setResults(sAAResponse2.getResults());
        return questionsResponse;
    }
}
