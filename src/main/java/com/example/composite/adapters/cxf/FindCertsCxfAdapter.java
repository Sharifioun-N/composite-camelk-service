package com.example.composite.adapters.cxf;

import com.example.composite.domain.Certificate;
import com.example.composite.ports.FindCertsPort;

import java.util.List;

public class FindCertsCxfAdapter implements FindCertsPort {
    @Override
    public List<Certificate> findCerts(String username, boolean active) {
        throw new UnsupportedOperationException("CXF adapter not yet wired: provide TLS materials and enable codegen");
    }
}


