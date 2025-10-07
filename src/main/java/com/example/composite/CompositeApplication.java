package com.example.composite;

import com.example.composite.routes.CompositeRoutes;
import org.apache.camel.main.Main;

public class CompositeApplication {
    public static void main(String[] args) throws Exception {
        Main main = new Main();

        // Register routes
        main.configure().addRoutesBuilder(new CompositeRoutes());

        main.run(args);
    }
}
