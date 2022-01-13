package uk.gov.di.cri.experian.kbv.api.domain;

import com.experian.uk.schema.experian.identityiq.services.webservice.ArrayOfAlerts;
import com.experian.uk.schema.experian.identityiq.services.webservice.ArrayOfString;
import com.experian.uk.schema.experian.identityiq.services.webservice.ResultsQuestions;

public class QuestionAnswerResponse {

    protected String outcome;
    protected String authenticationResult;
    protected ResultsQuestions questions;
    protected ArrayOfAlerts alerts;
    protected ArrayOfString nextTransId;
    protected String caseFoundFlag;
    protected String confirmationCode;

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String value) {
        this.outcome = value;
    }

    public String getAuthenticationResult() {
        return authenticationResult;
    }

    public void setAuthenticationResult(String value) {
        this.authenticationResult = value;
    }

    public ResultsQuestions getQuestions() {
        return questions;
    }

    public void setQuestions(ResultsQuestions value) {
        this.questions = value;
    }

    public ArrayOfAlerts getAlerts() {
        return alerts;
    }

    public void setAlerts(ArrayOfAlerts value) {
        this.alerts = value;
    }

    public ArrayOfString getNextTransId() {
        return nextTransId;
    }

    public void setNextTransId(ArrayOfString value) {
        this.nextTransId = value;
    }

    public String getCaseFoundFlag() {
        return caseFoundFlag;
    }

    public void setCaseFoundFlag(String value) {
        this.caseFoundFlag = value;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String value) {
        this.confirmationCode = value;
    }
}
