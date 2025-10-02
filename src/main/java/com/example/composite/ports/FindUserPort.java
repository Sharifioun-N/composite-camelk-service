package com.example.composite.ports;

import java.util.List;

public interface FindUserPort {
    List<String> findUserBySerial(String serialNumber, int matchType, int matchWith);
}
