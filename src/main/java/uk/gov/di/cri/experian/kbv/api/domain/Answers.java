package uk.gov.di.cri.experian.kbv.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Answers {

    @JsonProperty private String identifier;

    @JsonProperty private FieldType fieldType;

    @JsonProperty private List<String> answerList;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public List<String> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<String> answerList) {
        this.answerList = answerList;
    }
}
