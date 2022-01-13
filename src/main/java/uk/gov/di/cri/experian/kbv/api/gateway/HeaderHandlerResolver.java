package uk.gov.di.cri.experian.kbv.api.gateway;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import java.util.Collections;
import java.util.List;

public class HeaderHandlerResolver implements HandlerResolver {

    private final String token;

    public HeaderHandlerResolver(String token) {
        this.token = token;
    }

    public List<Handler> getHandlerChain(PortInfo portInfo) {
        return Collections.singletonList(new HeaderHandler(token));
    }
}
