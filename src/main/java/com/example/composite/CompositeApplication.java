package com.example.composite;

import com.example.composite.adapters.stub.FindCertsStubAdapter;
import com.example.composite.adapters.stub.FindUserStubAdapter;
import com.example.composite.routes.CompositeRoutes;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.main.Main;
import org.apache.camel.component.properties.PropertiesComponent;

public class CompositeApplication {
    public static void main(String[] args) throws Exception {
        Main main = new Main();

        // Bind adapters
        main.bind("findUserPort", new FindUserStubAdapter());
        main.bind("findCertsPort", new FindCertsStubAdapter());

        // Add properties component to Camel context to handle properties file
        CamelContext context = main.getCamelContext();
        PropertiesComponent propertiesComponent = new PropertiesComponent();
        propertiesComponent.setLocation("classpath:application.properties");
        context.addComponent("properties", (Component) propertiesComponent);

        // Add routes
        main.configure().addRoutesBuilder(new CompositeRoutes());

        main.run(args);
    }
}
