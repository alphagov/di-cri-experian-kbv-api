package uk.gov.di.cri.experian.kbv.api.gateway;

import com.experian.uk.schema.experian.identityiq.services.webservice.Question;
import com.experian.uk.schema.experian.identityiq.services.webservice.SAAResponse2;
import uk.gov.di.cri.experian.kbv.api.domain.PersonIdentity;
import uk.gov.di.cri.experian.kbv.api.domain.Questions;
import uk.gov.di.cri.experian.kbv.api.domain.QuestionsResponse;
import uk.gov.di.cri.experian.kbv.api.gateway.dto.SAARequestDto;

import java.util.ArrayList;
import java.util.List;

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
        questionsResponse.setAuthRefNo(sAAResponse2.getControl().getAuthRefNo());
        questionsResponse.setUrn(sAAResponse2.getControl().getURN());
        com.experian.uk.schema.experian.identityiq.services.webservice.Questions questions = sAAResponse2.getQuestions();
        List<Question> question =
                questions != null && questions.getQuestion() != null
                        ? questions.getQuestion()
                        : new ArrayList<>();
        List<Questions> questionsPersonIdentities = new ArrayList<>();

        for (Question q : question) {
            Questions question1 =
                    new Questions();
            question1.setQuestionID(q.getQuestionID());
            question1.setText(q.getText());
            question1.setTooltip(q.getTooltip());
            //question.setAnswers(q.getAnswerFormat());
            questionsPersonIdentities.add(question1);
        }
        questionsResponse.setQuestions(questionsPersonIdentities);

        //        sAAResponse2.getControl();
        //        sAAResponse2.getQuestions();
        //        sAAResponse2.getResults();

        return questionsResponse;
    }
}
