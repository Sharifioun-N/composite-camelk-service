package com.example.composite;

import com.example.composite.adapters.stub.FindCertsStubAdapter;
import com.example.composite.adapters.stub.FindUserStubAdapter;
import com.example.composite.routes.CompositeRoutes;
import org.apache.camel.main.Main;

public class CompositeApplication {
    public static void main(String[] args) throws Exception {
        Main main = new Main();

        // Register stub adapters in the Camel registry
        main.bind("findUserPort", new FindUserStubAdapter());
        main.bind("findCertsPort", new FindCertsStubAdapter());

        // Register routes
        main.configure().addRoutesBuilder(new CompositeRoutes());

        main.run(args);
    }
}
