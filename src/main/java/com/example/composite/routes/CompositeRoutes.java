package com.example.composite.routes;

import com.example.composite.api.*;
import com.example.composite.domain.Certificate;
import com.example.composite.ports.FindCertsPort;
import com.example.composite.ports.FindUserPort;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import java.util.ArrayList;
import java.util.List;

public class CompositeRoutes extends RouteBuilder {

    @Override
    public void configure() {
        // --- Error mapping ---
        onException(IllegalArgumentException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody().simple("{\"error\":\"${exception.message}\"}");

        // --- REST config ---
        restConfiguration()
                .component("platform-http")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

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

                    // Pull constants from properties (matches SOAP inputs)
                    String mt = exchange.getContext().resolvePropertyPlaceholders("{{composite.finduser.matchType}}");
                    String mw = exchange.getContext().resolvePropertyPlaceholders("{{composite.finduser.matchWith}}");
                    int matchType = Integer.parseInt(mt);
                    int matchWith = Integer.parseInt(mw);

                    // Lookup ports
                    FindUserPort findUser = exchange.getContext().getRegistry()
                            .lookupByNameAndType("findUserPort", FindUserPort.class);
                    FindCertsPort findCerts = exchange.getContext().getRegistry()
                            .lookupByNameAndType("findCertsPort", FindCertsPort.class);

                    // 1) Find usernames by serial number
                    List<String> usernames = findUser.findUserBySerial(req.serialNumber(), matchType, matchWith);

                    // 2) For each username, get valid/invalid certs (sequential for now)
                    List<UserEntry> userEntries = new ArrayList<>();
                    for (String username : usernames) {
                        List<Certificate> valid = findCerts.findCerts(username, true);
                        List<Certificate> invalid = findCerts.findCerts(username, false);
                        userEntries.add(new UserEntry(username, new UserCertificates(valid, invalid)));
                    }

                    // 3) Build response
                    UserCertResponse resp = new UserCertResponse(req.serialNumber(), userEntries);
                    exchange.getMessage().setBody(resp);
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
                });
    }
}
