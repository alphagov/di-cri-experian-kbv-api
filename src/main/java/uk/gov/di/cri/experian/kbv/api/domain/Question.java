package uk.gov.di.cri.experian.kbv.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {

    @JsonProperty private String questionID;

    @JsonProperty private String text;

    @JsonProperty private String tooltip;

    @JsonProperty private Answers answers;

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public Answers getAnswerFormat() {
        return answers;
    }

    public void setAnswerFormat(Answers answers) {
        this.answers = answers;
    }
}
