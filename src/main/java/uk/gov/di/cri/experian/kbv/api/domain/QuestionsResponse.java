package uk.gov.di.cri.experian.kbv.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionsResponse {

    @JsonProperty private String urn;

    @JsonProperty private String authRefNo;

    @JsonProperty List<Questions> questions;

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getAuthRefNo() {
        return authRefNo;
    }

    public void setAuthRefNo(String authRefNo) {
        this.authRefNo = authRefNo;
    }

    public List<Questions> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Questions> questions) {
        this.questions = questions;
    }
}
