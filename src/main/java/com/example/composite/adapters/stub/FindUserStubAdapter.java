package com.example.composite.adapters.stub;

import com.example.composite.ports.FindUserPort;

import java.util.List;

public class FindUserStubAdapter implements FindUserPort {
    @Override
    public List<String> findUserBySerial(String serialNumber, int matchType, int matchWith) {
        // Pretend this comes from SOAP findUser(MatchType=2, MatchWith=7, MatchValue=serialNumber)
        return List.of("alice", "bob");
    }
}
