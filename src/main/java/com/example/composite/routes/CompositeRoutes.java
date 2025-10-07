package com.example.composite.routes;

import com.example.composite.api.*;
import com.example.composite.domain.Certificate;
import com.example.composite.ports.FindCertsPort;
import com.example.composite.ports.FindUserPort;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.BindToRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;

public class CompositeRoutes extends RouteBuilder {

    @BindToRegistry("findUserPort")
    public FindUserPort findUserPort() {
        // Default to stub for POC; replace with CXF adapter when available
        return new com.example.composite.adapters.stub.FindUserStubAdapter();
    }

    @BindToRegistry("findCertsPort")
    public FindCertsPort findCertsPort() {
        // Default to stub for POC; replace with CXF adapter when available
        return new com.example.composite.adapters.stub.FindCertsStubAdapter();
    }

    @BindToRegistry("jsonObjectMapper")
    public ObjectMapper jsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public void configure() {
        // --- Error mapping ---
        onException(IllegalArgumentException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody().simple("{\"error\":\"${exception.message}\"}");

        // --- REST config: explicit, programmatic host/port (reads placeholders) ---
        // Read properties (with defaults) — placeholders are resolved by Camel
        String resolvedHost = getContext().resolvePropertyPlaceholders("{{app.http.host}}");
        String resolvedPort = getContext().resolvePropertyPlaceholders("{{app.http.port}}");
        String restComponent = getContext().resolvePropertyPlaceholders("{{rest.component}}");

        // Component is configurable to allow local runs (undertow) and Camel K (platform-http)
        restConfiguration()
            .component(restComponent)
            .host(resolvedHost)
            .port(Integer.parseInt(resolvedPort))
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .dataFormatProperty("objectMapper", "#jsonObjectMapper");

        // --- REST endpoint (POST /composite/user-certificates) ---
        rest("/composite")
                .post("/user-certificates")
                .consumes("application/json").produces("application/json")
                .type(UserCertRequest.class).outType(UserCertResponse.class)
                .to("direct:compositeUserCerts");

        // --- Route implementation (sequential for now; EIPs in the next step) ---
        from("direct:compositeUserCerts")
            .routeId("composite-user-certs")
            .log("Received composite request")
            .process(exchange -> {
                UserCertRequest req = exchange.getIn().getBody(UserCertRequest.class);
                if (req == null || req.serialNumber() == null || req.serialNumber().isBlank()) {
                    throw new IllegalArgumentException("serialNumber is required");
                }

                String mt = exchange.getContext().resolvePropertyPlaceholders("{{composite.finduser.matchType}}");
                String mw = exchange.getContext().resolvePropertyPlaceholders("{{composite.finduser.matchWith}}");
                int matchType = Integer.parseInt(mt);
                int matchWith = Integer.parseInt(mw);

                FindUserPort findUser = exchange.getContext().getRegistry()
                        .lookupByNameAndType("findUserPort", FindUserPort.class);
                FindCertsPort findCerts = exchange.getContext().getRegistry()
                        .lookupByNameAndType("findCertsPort", FindCertsPort.class);

                List<String> usernames = findUser.findUserBySerial(req.serialNumber(), matchType, matchWith);

                List<UserEntry> userEntries = new ArrayList<>();
                for (String username : usernames) {
                    List<Certificate> valid = findCerts.findCerts(username, true);
                    List<Certificate> invalid = findCerts.findCerts(username, false);
                    userEntries.add(new UserEntry(username, new UserCertificates(valid, invalid)));
                }

                UserCertResponse resp = new UserCertResponse(req.serialNumber(), userEntries);
                exchange.getMessage().setBody(resp);
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
            });
    }
}
