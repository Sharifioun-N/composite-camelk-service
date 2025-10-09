package com.example.composite.adapters.stub;

import com.example.composite.domain.Certificate;
import com.example.composite.ports.FindCertsPort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FindCertsStubAdapter implements FindCertsPort {
    @Override
    public List<Certificate> findCerts(String username, boolean active) {
        List<Certificate> list = new ArrayList<>();
        if (active) {
            list.add(new Certificate(username + "-VAL-001", "CN=" + username,
                    LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), true));
        } else {
            list.add(new Certificate(username + "-INV-001", "CN=" + username,
                    LocalDate.now().minusYears(3), LocalDate.now().minusMonths(6), false));
        }
        return list;
    }
}
