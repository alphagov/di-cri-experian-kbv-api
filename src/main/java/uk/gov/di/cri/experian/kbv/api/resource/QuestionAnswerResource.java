package uk.gov.di.cri.experian.kbv.api.resource;

import spark.Request;
import spark.Response;
import spark.Route;

public class QuestionAnswerResource {
    public final Route submitQuestionAnswers =
            (Request request, Response response) -> {
                return response.body();
            };
}
