package com.example.composite.adapters.cxf;

import com.example.composite.ports.FindUserPort;

import java.util.List;

public class FindUserCxfAdapter implements FindUserPort {
    @Override
    public List<String> findUserBySerial(String serialNumber, int matchType, int matchWith) {
        throw new UnsupportedOperationException("CXF adapter not yet wired: provide TLS materials and enable codegen");
    }
}


