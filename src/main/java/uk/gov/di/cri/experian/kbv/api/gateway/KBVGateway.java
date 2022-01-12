package uk.gov.di.cri.experian.kbv.api.gateway;

import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;

public class KBVGateway {

    private final SAARequestMapper saaRequestMapper;

    public KBVGateway(SAARequestMapper saaRequestMapper) {
        this.saaRequestMapper = saaRequestMapper;
    }

    public QuestionsResponse getQuestions(PersonIdentity personIdentity) {
        Object saaRequest = this.saaRequestMapper.mapPersonIdentity(personIdentity);

        // TODO: make the call to the experian SAA endpoint

        return null;
    }

    public Object submitAnswers(Object questionAnswers) {
        return null;
    }
}
