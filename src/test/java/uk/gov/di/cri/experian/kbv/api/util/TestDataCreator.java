package uk.gov.di.cri.experian.kbv.api.util;

import uk.gov.di.cri.experian.kbv.api.domain.AddressType;
import uk.gov.di.cri.experian.kbv.api.domain.PersonAddress;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswer;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionAnswerRequest;

import java.time.LocalDate;
import java.util.List;

public class TestDataCreator {
    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();
        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        PersonAddress address = new PersonAddress();
        address.setAddressType(addressType);
        address.setPostcode("Postcode");
        address.setStreet("Street Name");
        address.setTownCity("PostTown");
        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(AddressType.CURRENT);
    }

    public static QuestionAnswerRequest createTestQuestionAnswerRequest() {
        QuestionAnswerRequest answerRequest = new QuestionAnswerRequest();
        answerRequest.setUrn("urn");
        answerRequest.setAuthRefNo("auth-ref-no");
        answerRequest.setQuestionAnswers(List.of(new QuestionAnswer(), new QuestionAnswer()));
        return answerRequest;
    }
}
